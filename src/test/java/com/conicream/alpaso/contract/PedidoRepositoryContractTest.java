package com.conicream.alpaso.contract;

import static org.junit.jupiter.api.Assertions.*;

import com.conicream.alpaso.application.port.out.PedidoRepositoryPort;
import com.conicream.alpaso.domain.model.DetallePedido;
import com.conicream.alpaso.domain.model.EstadoPedido;
import com.conicream.alpaso.domain.model.Pedido;
import com.conicream.alpaso.domain.vo.Money;
import com.conicream.alpaso.testsupport.Datos;
import java.util.List;
import org.junit.jupiter.api.Test;

public abstract class PedidoRepositoryContractTest {

    protected abstract PedidoRepositoryPort repo();

    /** Crea un producto real (si la implementación lo necesita) y devuelve su id. */
    protected abstract long crearProducto(String nombre);

    private Pedido nuevo(String comerciante, java.time.LocalDate fecha, long... productos) {
        List<DetallePedido> lineas = new java.util.ArrayList<>();
        for (long p : productos) {
            lineas.add(new DetallePedido(p, "Producto " + p, 10, Money.de(2500)));
        }
        return Pedido.crear(comerciante, fecha, lineas);
    }

    @Test
    void guardaPedidoConSusLineas() {
        long a = crearProducto("A");
        long b = crearProducto("B");
        Pedido g = repo().guardar(nuevo("111", Datos.HOY, a, b));
        assertNotNull(g.getId());
        Pedido r = repo().buscarPorId(g.getId().longValue()).orElseThrow();
        assertEquals("111", r.getComercianteId());
        assertEquals(2, r.getDetalles().size());
        assertEquals(Money.de(50000), r.total());
        assertEquals(EstadoPedido.BORRADOR, r.getEstado());
    }

    @Test
    void persisteLosCambiosDeEstado() {
        long a = crearProducto("A");
        Pedido g = repo().guardar(nuevo("111", Datos.HOY, a));
        g.confirmar();
        repo().guardar(g);
        assertEquals(EstadoPedido.CONFIRMADO, repo().buscarPorId(g.getId().longValue()).orElseThrow().getEstado());
        assertEquals(1, repo().buscarPorId(g.getId().longValue()).orElseThrow().getDetalles().size());
    }

    @Test
    void buscaPorFecha() {
        long a = crearProducto("A");
        repo().guardar(nuevo("111", Datos.HOY, a));
        repo().guardar(nuevo("222", Datos.HOY, a));
        repo().guardar(nuevo("111", Datos.HOY.minusDays(1), a));
        assertEquals(2, repo().buscarPorFecha(Datos.HOY).size());
        assertEquals(1, repo().buscarPorFecha(Datos.HOY.minusDays(1)).size());
        assertTrue(repo().buscarPorFecha(Datos.HOY.plusDays(5)).isEmpty());
    }

    @Test
    void inexistenteDevuelveVacio() {
        assertTrue(repo().buscarPorId(4321).isEmpty());
    }
}
