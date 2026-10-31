package com.conicream.alpaso.contract;

import static org.junit.jupiter.api.Assertions.*;

import com.conicream.alpaso.application.port.out.LiquidacionRepositoryPort;
import com.conicream.alpaso.domain.model.LineaLiquidacion;
import com.conicream.alpaso.domain.model.Liquidacion;
import com.conicream.alpaso.domain.vo.Money;
import java.util.List;
import org.junit.jupiter.api.Test;

public abstract class LiquidacionRepositoryContractTest {

    protected abstract LiquidacionRepositoryPort repo();

    /** Crea un pedido real (si la implementación lo necesita) y devuelve su id. */
    protected abstract long crearPedido();

    private Liquidacion nueva(long pedidoId) {
        return Liquidacion.nueva(pedidoId, "111", List.of(
                new LineaLiquidacion(1, "Cono", 100, 15, Money.de(2500)),
                new LineaLiquidacion(2, "Paleta", 40, 0, Money.de(1500))));
    }

    @Test
    void guardaConSusLineasYTotal() {
        long pedido = crearPedido();
        Liquidacion g = repo().guardar(nueva(pedido));
        assertNotNull(g.getId());
        Liquidacion r = repo().buscarPorPedido(pedido).orElseThrow();
        assertEquals(2, r.getLineas().size());
        assertEquals(15, r.getLineas().get(0).sobrantes());
        assertEquals(Money.de(272500), r.total());
        assertEquals("111", r.getComercianteId());
    }

    @Test
    void buscaPorVariosPedidos() {
        long a = crearPedido();
        long b = crearPedido();
        repo().guardar(nueva(a));
        repo().guardar(nueva(b));
        assertEquals(2, repo().buscarPorPedidos(List.of(a, b)).size());
        assertEquals(1, repo().buscarPorPedidos(List.of(a, 9999L)).size());
        assertTrue(repo().buscarPorPedidos(List.of()).isEmpty());
    }

    @Test
    void pedidoSinLiquidacionDevuelveVacio() {
        assertTrue(repo().buscarPorPedido(crearPedido()).isEmpty());
    }
}
