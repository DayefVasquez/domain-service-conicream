package com.conicream.alpaso.contract;

import static org.junit.jupiter.api.Assertions.*;

import com.conicream.alpaso.application.port.out.ProductoRepositoryPort;
import com.conicream.alpaso.domain.model.Producto;
import com.conicream.alpaso.domain.vo.Money;
import com.conicream.alpaso.testsupport.Datos;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Toda implementación del puerto (memoria, JPA…) debe pasar estas mismas pruebas. */
public abstract class ProductoRepositoryContractTest {

    protected abstract ProductoRepositoryPort repo();

    @Test
    @DisplayName("guardar asigna id y buscarPorId devuelve los mismos datos")
    void guardaYRecupera() {
        Producto g = repo().guardar(Datos.cono());
        assertNotNull(g.getId());
        Producto r = repo().buscarPorId(g.getId().longValue()).orElseThrow();
        assertEquals("Cono Vainilla", r.getNombre());
        assertEquals(Money.de(2500), r.getPrecio());
        assertEquals(24, r.getUnidadesPorCaja());
        assertTrue(r.isActivo());
    }

    @Test
    void actualizarConservaElIdYPersisteElRetiro() {
        Producto g = repo().guardar(Datos.cono());
        g.editar("Nuevo", "Marca", Money.de(1));
        g.retirar();
        Producto u = repo().guardar(g);
        assertEquals(g.getId(), u.getId());
        Producto r = repo().buscarPorId(g.getId().longValue()).orElseThrow();
        assertEquals("Nuevo", r.getNombre());
        assertFalse(r.isActivo());
    }

    @Test
    void buscaActivoPorNombreYMarcaSinImportarMayusculas() {
        Producto g = repo().guardar(Datos.cono());
        assertTrue(repo().buscarActivoPorNombreYMarca("CONO vainilla", "crem HELADO").isPresent());
        assertTrue(repo().buscarActivoPorNombreYMarca("Cono Vainilla", "Otra").isEmpty());
        g.retirar();
        repo().guardar(g);
        assertTrue(repo().buscarActivoPorNombreYMarca("Cono Vainilla", "Crem Helado").isEmpty());
    }

    @Test
    void listaPorIdYFiltraActivos() {
        Producto a = repo().guardar(Datos.cono());
        Producto b = repo().guardar(Producto.registrar("Paleta", "Crem", Money.de(1500), 12));
        b.retirar();
        repo().guardar(b);
        assertEquals(2, repo().listar(false).size());
        assertEquals(1, repo().listar(true).size());
        assertEquals(a.getId(), repo().listar(false).get(0).getId());
        assertEquals(2, repo().buscarPorIds(List.of(a.getId(), b.getId())).size());
        assertEquals(1, repo().buscarPorIds(List.of(a.getId(), 9999L)).size());
    }

    @Test
    void inexistenteDevuelveVacio() {
        assertTrue(repo().buscarPorId(12345).isEmpty());
    }
}
