package com.conicream.alpaso.contract;

import static org.junit.jupiter.api.Assertions.*;

import com.conicream.alpaso.application.port.out.StockRepositoryPort;
import com.conicream.alpaso.domain.model.StockProducto;
import org.junit.jupiter.api.Test;

public abstract class StockRepositoryContractTest {

    protected abstract StockRepositoryPort repo();
    protected abstract long crearProducto(String nombre);

    @Test
    void guardarYBuscarStock() {
        long pId = crearProducto("Helado de Prueba");
        StockProducto s = StockProducto.crear(pId, 50, 10);
        repo().guardar(s);

        StockProducto encontrado = repo().buscarPorProducto(pId).orElseThrow();
        assertEquals(50, encontrado.unidadesDisponibles());
        assertEquals(10, encontrado.getStockMinimo());
    }
}
