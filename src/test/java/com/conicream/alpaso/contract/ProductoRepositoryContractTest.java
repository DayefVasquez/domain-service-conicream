package com.conicream.alpaso.contract;

import static org.junit.jupiter.api.Assertions.*;

import com.conicream.alpaso.application.port.out.ProductoRepositoryPort;
import com.conicream.alpaso.domain.model.Producto;
import com.conicream.alpaso.domain.vo.Money;
import org.junit.jupiter.api.Test;

public abstract class ProductoRepositoryContractTest {

    protected abstract ProductoRepositoryPort repo();

    @Test
    void guardarYBuscar() {
        Producto p = Producto.registrar("PAL-01", "Paleta", "Crem", Money.de(1500));
        Producto guardado = repo().guardar(p);

        assertNotNull(guardado.getId());
        assertEquals("PAL-01", guardado.getCodigo());
        assertEquals("Paleta", guardado.getNombre());

        Producto encontrado = repo().buscarPorId(guardado.getId()).orElseThrow();
        assertEquals("Paleta", encontrado.getNombre());
        assertEquals(Money.de(1500), encontrado.getPrecio());

        encontrado.editar("PAL-02", "Paleta Doble", "Crem", Money.de(2000));
        repo().guardar(encontrado);

        Producto actualizado = repo().buscarPorId(guardado.getId()).orElseThrow();
        assertEquals("PAL-02", actualizado.getCodigo());
        assertEquals("Paleta Doble", actualizado.getNombre());
        assertEquals(Money.de(2000), actualizado.getPrecio());
    }
}
