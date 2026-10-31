package com.conicream.alpaso.contract;

import static org.junit.jupiter.api.Assertions.*;

import com.conicream.alpaso.application.port.out.StockRepositoryPort;
import com.conicream.alpaso.domain.model.StockProducto;
import java.util.List;
import org.junit.jupiter.api.Test;

public abstract class StockRepositoryContractTest {

    protected abstract StockRepositoryPort repo();

    /** Crea el producto real (si la implementación lo necesita) y devuelve su id, con 24 unidades por caja. */
    protected abstract long crearProducto(String nombre);

    @Test
    void guardaYRecuperaPorProducto() {
        long id = crearProducto("Cono");
        repo().guardar(StockProducto.crear(id, 24, 10));
        StockProducto r = repo().buscarPorProducto(id).orElseThrow();
        assertEquals(240, r.unidadesDisponibles());
        assertEquals(24, r.getUnidadesPorCaja());
    }

    @Test
    void persisteElDescuento() {
        long id = crearProducto("Cono");
        StockProducto s = repo().guardar(StockProducto.crear(id, 24, 10));
        s.descontar(50);
        repo().guardar(s);
        assertEquals(190, repo().buscarPorProducto(id).orElseThrow().unidadesDisponibles());
    }

    @Test
    void buscaVariosProductos() {
        long a = crearProducto("A");
        long b = crearProducto("B");
        repo().guardar(StockProducto.crear(a, 24, 1));
        repo().guardar(StockProducto.crear(b, 24, 2));
        assertEquals(2, repo().buscarPorProductos(List.of(a, b)).size());
        assertEquals(1, repo().buscarPorProductos(List.of(a, 9999L)).size());
        assertTrue(repo().buscarPorProductos(List.of()).isEmpty());
    }

    @Test
    void productoInexistenteDevuelveVacio() {
        assertTrue(repo().buscarPorProducto(999).isEmpty());
    }
}
