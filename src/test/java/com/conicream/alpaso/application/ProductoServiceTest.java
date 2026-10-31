package com.conicream.alpaso.application;

import static org.junit.jupiter.api.Assertions.*;

import com.conicream.alpaso.application.port.in.command.EditarProductoCommand;
import com.conicream.alpaso.application.port.in.command.RegistrarProductoCommand;
import com.conicream.alpaso.application.port.in.result.ProductoConStock;
import com.conicream.alpaso.domain.exception.*;
import com.conicream.alpaso.domain.vo.Money;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductoServiceTest {

    private final Escenario e = new Escenario();

    @Test
    @DisplayName("Registrar crea el producto y su stock inicial en cajas")
    void registraConStockInicial() {
        ProductoConStock pc = e.producto("Cono Vainilla", "Crem Helado", 2500, 24, 10);
        assertNotNull(pc.producto().getId());
        assertEquals(240, pc.stock().unidadesDisponibles());
        assertEquals(10, pc.stock().cajasCompletas());
        assertEquals(pc.producto().getId().longValue(), pc.stock().getProductoId());
    }

    @Test
    @DisplayName("Si faltan datos, el mensaje lista TODOS los campos faltantes y no se guarda nada")
    void listaTodosLosCamposFaltantes() {
        ValorInvalidoException ex = assertThrows(ValorInvalidoException.class, () -> e.productoService.registrar(
                new RegistrarProductoCommand(" ", null, null, 24, 1)));
        assertTrue(ex.getMessage().startsWith("Faltan campos obligatorios"));
        assertTrue(ex.getMessage().contains("nombre"));
        assertTrue(ex.getMessage().contains("marca"));
        assertTrue(ex.getMessage().contains("precio"));
        assertTrue(e.productos.listar(false).isEmpty());
    }

    @Test
    void validaPrecioUnidadesYCajas() {
        assertThrows(ValorInvalidoException.class, () -> e.producto("A", "B", 0, 24, 1));
        assertThrows(ValorInvalidoException.class, () -> e.producto("A", "B", 100, 0, 1));
        assertThrows(ValorInvalidoException.class, () -> e.producto("A", "B", 100, 24, -1));
        assertTrue(e.productos.listar(false).isEmpty());
        assertTrue(e.stocks.buscarPorProductos(List.of(0L, 1L)).isEmpty());
    }

    @Test
    void noRepiteNombreYMarcaSinImportarMayusculas() {
        e.producto("Cono Vainilla", "Crem Helado", 2500, 24, 1);
        assertThrows(ProductoDuplicadoException.class, () -> e.producto("cono vainilla", "CREM HELADO", 1, 1, 0));
        e.producto("Cono Vainilla", "Otra Marca", 2500, 12, 1);   // otra marca: sí
        assertEquals(2, e.productos.listar(false).size());
    }

    @Test
    void editarCambiaDatosSinChocarConsigoMismo() {
        long id = e.producto("Cono", "Crem", 2500, 24, 1).producto().getId().longValue();
        ProductoConStock ed = e.productoService.editar(new EditarProductoCommand(id, "Cono", "Crem", BigDecimal.valueOf(2800)));
        assertEquals(Money.de(2800), ed.producto().getPrecio());
        assertEquals(24, ed.stock().getUnidadesPorCaja());
    }

    @Test
    void editarHaciaUnNombreOcupadoOInexistente() {
        e.producto("Cono", "Crem", 2500, 24, 1);
        long otro = e.producto("Paleta", "Crem", 1500, 12, 1).producto().getId().longValue();
        assertThrows(ProductoDuplicadoException.class,
                () -> e.productoService.editar(new EditarProductoCommand(otro, "cono", "crem", BigDecimal.TEN)));
        assertThrows(ProductoNoEncontradoException.class,
                () -> e.productoService.editar(new EditarProductoCommand(99, "X", "Y", BigDecimal.TEN)));
        assertThrows(ValorInvalidoException.class,
                () -> e.productoService.editar(new EditarProductoCommand(otro, "", null, null)));
    }

    @Test
    @DisplayName("Retirar es lógico: el producto sigue existiendo pero sale del catálogo activo")
    void retirar() {
        long id = e.producto("Cono", "Crem", 2500, 24, 1).producto().getId().longValue();
        e.producto("Paleta", "Crem", 1500, 12, 1);
        assertFalse(e.productoService.retirar(id).producto().isActivo());
        assertEquals(1, e.productoService.listar(true).size());
        assertEquals(2, e.productoService.listar(false).size());
        assertFalse(e.productoService.porId(id).producto().isActivo());
        assertThrows(ProductoInactivoException.class, () -> e.productoService.retirar(id));
        e.producto("Cono", "Crem", 2600, 24, 1);   // el nombre queda libre tras retirar
    }

    @Test
    void listarTrae(){
        e.producto("Cono", "Crem", 2500, 24, 2);
        List<ProductoConStock> lista = e.productoService.listar(true);
        assertEquals(1, lista.size());
        assertEquals(48, lista.get(0).stock().unidadesDisponibles());
        assertThrows(ProductoNoEncontradoException.class, () -> e.productoService.porId(5));
    }
}
