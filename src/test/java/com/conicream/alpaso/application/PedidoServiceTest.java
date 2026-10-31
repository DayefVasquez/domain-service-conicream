package com.conicream.alpaso.application;

import static com.conicream.alpaso.application.Escenario.*;
import static org.junit.jupiter.api.Assertions.*;

import com.conicream.alpaso.application.port.in.command.CrearPedidoCommand;
import com.conicream.alpaso.application.port.in.command.CrearPedidoCommand.Linea;
import com.conicream.alpaso.domain.exception.*;
import com.conicream.alpaso.domain.model.EstadoPedido;
import com.conicream.alpaso.domain.model.Pedido;
import com.conicream.alpaso.domain.vo.Money;
import com.conicream.alpaso.testsupport.Datos;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PedidoServiceTest {

    private final Escenario e = new Escenario();

    private long cono() {
        return e.producto("Cono", "Crem", 2500, 24, 10).producto().getId().longValue();   // 240 unidades
    }

    @Test
    @DisplayName("Crear deja el pedido en BORRADOR, del comerciante, con precio congelado y sin tocar el stock")
    void creaBorrador() {
        long id = cono();
        Pedido p = e.pedido(ANA, id, 100);
        assertEquals(EstadoPedido.BORRADOR, p.getEstado());
        assertEquals(ANA.id(), p.getComercianteId());
        assertEquals(Datos.HOY, p.getFecha());
        assertEquals(Money.de(250000), p.total());
        assertEquals("Cono (Crem)", p.getDetalles().get(0).nombreProducto());
        assertEquals(240, e.stockDe(id));
    }

    @Test
    void sumaUnProductoRepetidoAntesDeValidarElStock() {
        long id = cono();
        Pedido p = e.pedidoService.crear(new CrearPedidoCommand(ANA, List.of(new Linea(id, 100), new Linea(id, 100))));
        assertEquals(200, p.getDetalles().get(0).cantidad());
        assertThrows(StockInsuficienteException.class,
                () -> e.pedidoService.crear(new CrearPedidoCommand(ANA, List.of(new Linea(id, 150), new Linea(id, 100)))));
    }

    @Test
    @DisplayName("No se pide más que el stock: responde por producto lo disponible y lo solicitado")
    void noSuperaElStock() {
        long id = cono();
        StockInsuficienteException ex = assertThrows(StockInsuficienteException.class, () -> e.pedido(ANA, id, 241));
        assertEquals(240, ex.getFaltantes().get(0).disponible());
        assertEquals(241, ex.getFaltantes().get(0).solicitado());
        assertEquals(0, e.pedidos.buscarPorFecha(Datos.HOY).size());
        e.pedido(ANA, id, 240);   // justo lo que hay, sí
    }

    @Test
    void validaLineasCantidadesYProductos() {
        long id = cono();
        assertThrows(ValorInvalidoException.class, () -> e.pedidoService.crear(new CrearPedidoCommand(ANA, List.of())));
        assertThrows(ValorInvalidoException.class, () -> e.pedido(ANA, id, 0));
        assertThrows(ValorInvalidoException.class, () -> e.pedido(ANA, id, -3));
        assertThrows(ProductoNoEncontradoException.class, () -> e.pedido(ANA, 999, 1));
        e.productoService.retirar(id);
        assertThrows(ProductoInactivoException.class, () -> e.pedido(ANA, id, 1));
    }

    @Test
    @DisplayName("Confirmar descuenta el stock y deja el pedido CONFIRMADO")
    void confirmaYDescuenta() {
        long id = cono();
        long pedidoId = e.pedido(ANA, id, 100).getId().longValue();
        Pedido c = e.pedidoService.confirmar(ANA, pedidoId);
        assertEquals(EstadoPedido.CONFIRMADO, c.getEstado());
        assertEquals(140, e.stockDe(id));
        assertEquals(EstadoPedido.CONFIRMADO, e.pedidoService.porId(ANA, pedidoId).getEstado());
    }

    @Test
    void noSeConfirmaDosVeces() {
        long id = cono();
        long pedidoId = e.pedido(ANA, id, 100).getId().longValue();
        e.pedidoService.confirmar(ANA, pedidoId);
        assertThrows(PedidoEstadoInvalidoException.class, () -> e.pedidoService.confirmar(ANA, pedidoId));
        assertEquals(140, e.stockDe(id));   // no se descontó otra vez
    }

    @Test
    @DisplayName("Si al confirmar otro pedido ya se llevó el stock, no se descuenta NADA y el pedido sigue en BORRADOR")
    void confirmarSinStockNoDejaNadaAMedias() {
        long cono = cono();
        long paleta = e.producto("Paleta", "Crem", 1500, 10, 1).producto().getId().longValue();   // 10 unidades
        long pedidoAna = e.pedidoService.crear(new CrearPedidoCommand(ANA, List.of(new Linea(cono, 100), new Linea(paleta, 10))))
                .getId().longValue();
        long pedidoLuis = e.pedido(LUIS, paleta, 6).getId().longValue();
        e.pedidoService.confirmar(LUIS, pedidoLuis);   // deja 4 paletas

        StockInsuficienteException ex = assertThrows(StockInsuficienteException.class, () -> e.pedidoService.confirmar(ANA, pedidoAna));

        assertEquals(1, ex.getFaltantes().size());
        assertEquals(240, e.stockDe(cono));
        assertEquals(4, e.stockDe(paleta));
        assertEquals(EstadoPedido.BORRADOR, e.pedidoService.porId(ANA, pedidoAna).getEstado());
    }

    @Test
    void soloElDuenoOUnAdminVenElPedido() {
        long pedidoId = e.pedido(ANA, cono(), 10).getId().longValue();
        assertThrows(AccesoDenegadoException.class, () -> e.pedidoService.porId(LUIS, pedidoId));
        assertThrows(AccesoDenegadoException.class, () -> e.pedidoService.confirmar(LUIS, pedidoId));
        assertEquals(ANA.id(), e.pedidoService.porId(ADMIN, pedidoId).getComercianteId());
        e.pedidoService.confirmar(ADMIN, pedidoId);
    }

    @Test
    void pedidoInexistente() {
        assertThrows(PedidoNoEncontradoException.class, () -> e.pedidoService.porId(ADMIN, 99));
        assertThrows(PedidoNoEncontradoException.class, () -> e.pedidoService.confirmar(ADMIN, 99));
    }

    @Test
    void elProductoRetiradoEntreCrearYConfirmarImpideConfirmar() {
        long id = cono();
        long pedidoId = e.pedido(ANA, id, 10).getId().longValue();
        e.productoService.retirar(id);
        assertThrows(ProductoInactivoException.class, () -> e.pedidoService.confirmar(ANA, pedidoId));
        assertEquals(240, e.stockDe(id));
    }
}
