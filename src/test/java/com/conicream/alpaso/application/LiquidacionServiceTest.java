package com.conicream.alpaso.application;

import static com.conicream.alpaso.application.Escenario.*;
import static org.junit.jupiter.api.Assertions.*;

import com.conicream.alpaso.application.port.in.command.LiquidarPedidoCommand;
import com.conicream.alpaso.domain.exception.*;
import com.conicream.alpaso.domain.model.EstadoPedido;
import com.conicream.alpaso.domain.model.Liquidacion;
import com.conicream.alpaso.domain.vo.Money;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LiquidacionServiceTest {

    private final Escenario e = new Escenario();

    /** Pedido de 100 conos (2.500 c/u) de Ana, ya confirmado. Devuelve su id. */
    private long pedidoConfirmado() {
        long cono = e.producto("Cono", "Crem", 2500, 24, 10).producto().getId().longValue();
        long pedido = e.pedido(ANA, cono, 100).getId().longValue();
        e.pedidoService.confirmar(ANA, pedido);
        return pedido;
    }

    private LiquidarPedidoCommand liquidar(long pedidoId, Map<Long, Integer> sobrantes) {
        return new LiquidarPedidoCommand(ANA, pedidoId, sobrantes);
    }

    @Test
    @DisplayName("Liquidar calcula (despachadas − sobrantes) × precio, cierra el pedido y NO devuelve sobrantes al stock")
    void liquida() {
        long pedido = pedidoConfirmado();
        int stockAntes = e.stockDe(1);

        Liquidacion l = e.liquidacionService.liquidar(liquidar(pedido, Map.of(1L, 15)));

        assertNotNull(l.getId());
        assertEquals(Money.de(212500), l.total());
        assertEquals(85, l.getLineas().get(0).vendidas());
        assertEquals(15, l.getLineas().get(0).sobrantes());
        assertEquals(EstadoPedido.LIQUIDADO, e.pedidoService.porId(ANA, pedido).getEstado());
        assertEquals(stockAntes, e.stockDe(1));
    }

    @Test
    void sinSobrantesSeCobraTodo() {
        long pedido = pedidoConfirmado();
        assertEquals(Money.de(250000), e.liquidacionService.liquidar(liquidar(pedido, null)).total());
    }

    @Test
    void soloSeLiquidaUnPedidoConfirmado() {
        long cono = e.producto("Cono", "Crem", 2500, 24, 10).producto().getId().longValue();
        long borrador = e.pedido(ANA, cono, 10).getId().longValue();
        assertThrows(PedidoEstadoInvalidoException.class, () -> e.liquidacionService.liquidar(liquidar(borrador, Map.of())));
    }

    @Test
    @DisplayName("Un pedido se liquida una sola vez")
    void unaSolaVez() {
        long pedido = pedidoConfirmado();
        e.liquidacionService.liquidar(liquidar(pedido, Map.of()));
        assertThrows(PedidoEstadoInvalidoException.class, () -> e.liquidacionService.liquidar(liquidar(pedido, Map.of())));
        assertEquals(1, e.liquidaciones.cantidad());
    }

    @Test
    @DisplayName("Sobrantes inválidos se rechazan y el pedido sigue CONFIRMADO")
    void sobrantesInvalidos() {
        long pedido = pedidoConfirmado();
        assertThrows(ValorInvalidoException.class, () -> e.liquidacionService.liquidar(liquidar(pedido, Map.of(1L, 101))));
        assertThrows(ValorInvalidoException.class, () -> e.liquidacionService.liquidar(liquidar(pedido, Map.of(77L, 1))));
        assertEquals(EstadoPedido.CONFIRMADO, e.pedidoService.porId(ANA, pedido).getEstado());
        assertEquals(0, e.liquidaciones.cantidad());
    }

    @Test
    void permisosYNoEncontrados() {
        long pedido = pedidoConfirmado();
        assertThrows(AccesoDenegadoException.class, () -> e.liquidacionService.liquidar(new LiquidarPedidoCommand(LUIS, pedido, Map.of())));
        assertThrows(PedidoNoEncontradoException.class, () -> e.liquidacionService.liquidar(liquidar(99, Map.of())));
        e.liquidacionService.liquidar(new LiquidarPedidoCommand(ADMIN, pedido, Map.of(1L, 10)));   // un admin sí puede
    }

    @Test
    void consultarLaLiquidacion() {
        long pedido = pedidoConfirmado();
        assertThrows(LiquidacionNoEncontradaException.class, () -> e.liquidacionService.porPedido(ANA, pedido));
        e.liquidacionService.liquidar(liquidar(pedido, Map.of(1L, 10)));
        assertEquals(Money.de(225000), e.liquidacionService.porPedido(ANA, pedido).total());
        assertEquals(Money.de(225000), e.liquidacionService.porPedido(ADMIN, pedido).total());
        assertThrows(AccesoDenegadoException.class, () -> e.liquidacionService.porPedido(LUIS, pedido));
        assertThrows(PedidoNoEncontradoException.class, () -> e.liquidacionService.porPedido(ADMIN, 99));
    }
}
