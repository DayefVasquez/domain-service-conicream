package com.conicream.alpaso.infrastructure.adapters.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conicream.alpaso.domain.model.EstadoPedido;
import com.conicream.alpaso.domain.vo.Money;
import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "seguridad.jwt.secret-base64=Lu7e1SCfczRWZpilqgAQVZqNiJopaxOzxiu9UaLENKGTeGeBtIyfpBegmsIKQS3D6pmEiahNszQXjzxmhjabaw==")
class ConiCreamRestTest {

    private static final String SECRETO = "Lu7e1SCfczRWZpilqgAQVZqNiJopaxOzxiu9UaLENKGTeGeBtIyfpBegmsIKQS3D6pmEiahNszQXjzxmhjabaw==";
    private static final String CEDULA_ANA = "1090123456";
    private static final String CEDULA_LUIS = "1090999888";

    @Autowired
    private MockMvc mvc;

    private String token(String sub, String... roles) {
        Instant ahora = Instant.now();
        return Jwts.builder().subject(sub).claim("uid", 99).claim("roles", List.of(roles))
                .issuedAt(Date.from(ahora)).expiration(Date.from(ahora.plus(Duration.ofHours(1))))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRETO)), Jwts.SIG.HS384).compact();
    }

    private String admin() { return token("admin", "ADMINISTRADOR"); }
    private String ana() { return token(CEDULA_ANA, "COMERCIANTE"); }
    private String luis() { return token(CEDULA_LUIS, "COMERCIANTE"); }

    private MockHttpServletRequestBuilder con(MockHttpServletRequestBuilder peticion, String token, String json) {
        MockHttpServletRequestBuilder b = peticion.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return json == null ? b : b.contentType(MediaType.APPLICATION_JSON).content(json);
    }

    private long id(String cuerpo) {
        return ((Number) JsonPath.read(cuerpo, "$.id")).longValue();
    }

    private long crearProducto(String nombre, int unidadesPorCaja, int cajas) throws Exception {
        String cod = "PROD-" + Math.abs((nombre).hashCode() % 10000);
        int unidadesTotales = unidadesPorCaja * cajas;
        String json = "{\"codigo\":\"" + cod + "\",\"nombre\":\"" + nombre + "\",\"marca\":\"Crem Helado\",\"precio\":2500,\"unidadesIniciales\":"
                + unidadesTotales + ",\"stockMinimo\":5}";
        return id(mvc.perform(con(post("/api/v1/productos"), admin(), json)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    private long crearPedido(String token, long productoId, int cantidad) throws Exception {
        String json = "{\"lineas\":[{\"productoId\":" + productoId + ",\"cantidad\":" + cantidad + "}]}";
        return id(mvc.perform(con(post("/api/pedidos"), token, json)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Sin token o con token falso: 401")
    void sinCredencialesEs401() throws Exception {
        mvc.perform(get("/api/productos")).andExpect(status().isUnauthorized());
        mvc.perform(con(get("/api/productos"), "token.falso.aqui", null)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("El comerciante ve el catálogo pero no lo modifica; el ADMIN sí")
    void permisosDelCatalogo() throws Exception {
        mvc.perform(con(post("/api/productos"), ana(), "{\"codigo\":\"P-1\",\"nombre\":\"X\",\"marca\":\"Y\",\"precio\":1,\"unidadesIniciales\":1,\"stockMinimo\":1}"))
                .andExpect(status().isForbidden());
        long id = crearProducto("Cono Permisos", 24, 1);
        mvc.perform(con(get("/api/productos/" + id), ana(), null)).andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreCompleto").value("Cono Permisos (Crem Helado)"));
        mvc.perform(con(put("/api/productos/" + id), ana(), "{\"codigo\":\"P-1\",\"nombre\":\"Z\",\"marca\":\"Y\",\"precio\":5}"))
                .andExpect(status().isForbidden());
        mvc.perform(con(delete("/api/productos/" + id), ana(), null)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Registrar con datos faltantes responde 400 y lista todos los campos")
    void camposFaltantes() throws Exception {
        mvc.perform(con(post("/api/productos"), admin(), "{\"unidadesIniciales\":24}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("nombre")))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("marca")))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("precio")));
    }

    @Test
    @DisplayName("Mismo nombre y marca activos: 409")
    void productoDuplicado() throws Exception {
        crearProducto("Cono Repetido", 24, 1);
        mvc.perform(con(post("/api/productos"), admin(),
                "{\"codigo\":\"P-DUP\",\"nombre\":\"cono repetido\",\"marca\":\"CREM HELADO\",\"precio\":1,\"unidadesIniciales\":1,\"stockMinimo\":1}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Inventario: reponer unidades y corregir por conteo manual")
    void inventario() throws Exception {
        long id = crearProducto("Cono Inventario", 24, 2);
        mvc.perform(con(get("/api/productos/" + id + "/stock"), ana(), null)).andExpect(status().isOk())
                .andExpect(jsonPath("$.unidadesDisponibles").value(48));
        mvc.perform(con(post("/api/productos/" + id + "/stock/reposiciones"), admin(), "{\"unidades\":72}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.unidadesDisponibles").value(120));
        mvc.perform(con(put("/api/productos/" + id + "/stock"), admin(), "{\"unidadesReales\":29}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.unidadesDisponibles").value(29));
        mvc.perform(con(post("/api/productos/" + id + "/stock/reposiciones"), ana(), "{\"unidades\":24}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Flujo completo: pedir, confirmar (descuenta stock), liquidar con sobrantes y descargar el Excel")
    void flujoCompleto() throws Exception {
        long producto = crearProducto("Cono Flujo", 24, 10);   // 240 unidades
        long pedido = crearPedido(ana(), producto, 100);

        mvc.perform(con(get("/api/pedidos/" + pedido), ana(), null)).andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.comercianteId").value(CEDULA_ANA))
                .andExpect(jsonPath("$.total").value(250000));
        mvc.perform(con(get("/api/productos/" + producto + "/stock"), ana(), null))
                .andExpect(jsonPath("$.unidadesDisponibles").value(240));   // pedir no descuenta

        mvc.perform(con(post("/api/pedidos/" + pedido + "/confirmacion"), ana(), null)).andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"));
        mvc.perform(con(get("/api/productos/" + producto + "/stock"), ana(), null))
                .andExpect(jsonPath("$.unidadesDisponibles").value(140));   // confirmar sí

        mvc.perform(con(post("/api/pedidos/" + pedido + "/confirmacion"), ana(), null)).andExpect(status().isConflict());

        String liquidar = "{\"sobrantes\":[{\"productoId\":" + producto + ",\"cantidad\":15}]}";
        // La comerciante (ana) liquida su pedido y recibe 201 Created
        mvc.perform(con(post("/api/pedidos/" + pedido + "/liquidacion"), ana(), liquidar))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(212500))
                .andExpect(jsonPath("$.lineas[0].vendidas").value(85));

        mvc.perform(con(post("/api/pedidos/" + pedido + "/liquidacion"), ana(), liquidar)).andExpect(status().isConflict());
        mvc.perform(con(get("/api/pedidos/" + pedido + "/liquidacion"), ana(), null)).andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(212500));

        // Los 15 sobrantes regresan físicamente al stock (140 + 15 = 155)
        mvc.perform(con(get("/api/productos/" + producto + "/stock"), ana(), null))
                .andExpect(jsonPath("$.unidadesDisponibles").value(155));

        byte[] excel = mvc.perform(con(get("/api/reportes/diario"), admin(), null))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andReturn().getResponse().getContentAsByteArray();
        assertTrue(excel.length > 0);
        assertEquals('P', excel[0]);
    }

    @Test
    @DisplayName("Un comerciante no ve ni toca el pedido de otro; el ADMIN sí puede verlo")
    void aislamientoEntreComerciantes() throws Exception {
        long producto = crearProducto("Cono Aislado", 24, 5);
        long pedido = crearPedido(ana(), producto, 10);
        mvc.perform(con(get("/api/pedidos/" + pedido), luis(), null)).andExpect(status().isForbidden());
        mvc.perform(con(post("/api/pedidos/" + pedido + "/confirmacion"), luis(), null)).andExpect(status().isForbidden());
        mvc.perform(con(get("/api/pedidos/" + pedido), admin(), null)).andExpect(status().isOk());
        mvc.perform(con(get("/api/pedidos/999999"), ana(), null)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Pedir más de lo que hay: 409 con el detalle de disponibles y solicitadas")
    void stockInsuficiente() throws Exception {
        long producto = crearProducto("Cono Escaso", 10, 1);   // 10 unidades
        mvc.perform(con(post("/api/pedidos"), ana(), "{\"lineas\":[{\"productoId\":" + producto + ",\"cantidad\":11}]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detalles[0]").value(org.hamcrest.Matchers.containsString("disponibles 10, solicitadas 11")));
    }

    @Test
    @DisplayName("Sobrantes mayores a lo despachado: 400 y el pedido sigue confirmado")
    void sobrantesInvalidos() throws Exception {
        long producto = crearProducto("Cono Sobrante", 24, 5);
        long pedido = crearPedido(ana(), producto, 10);
        mvc.perform(con(post("/api/pedidos/" + pedido + "/confirmacion"), ana(), null)).andExpect(status().isOk());
        // ana llama a su propia liquidación con sobrantes mayores a los 10 despachados -> 400 Bad Request
        mvc.perform(con(post("/api/pedidos/" + pedido + "/liquidacion"), ana(),
                "{\"sobrantes\":[{\"productoId\":" + producto + ",\"cantidad\":11}]}")).andExpect(status().isBadRequest());
        mvc.perform(con(get("/api/pedidos/" + pedido), ana(), null)).andExpect(jsonPath("$.estado").value("CONFIRMADO"));
    }

    @Test
    @DisplayName("El reporte es solo para el ADMIN y rechaza fechas futuras")
    void reporte() throws Exception {
        mvc.perform(con(get("/api/reportes/diario"), ana(), null)).andExpect(status().isForbidden());
        mvc.perform(con(get("/api/reportes/diario?fecha=2999-01-01"), admin(), null)).andExpect(status().isBadRequest());
        mvc.perform(con(get("/api/reportes/diario?fecha=no-es-fecha"), admin(), null)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Todo lo que no es /api está denegado")
    void rutasDesconocidas() throws Exception {
        mvc.perform(con(get("/otra/cosa"), admin(), null)).andExpect(status().isForbidden());
    }
}
