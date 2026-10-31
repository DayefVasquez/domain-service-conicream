package com.conicream.alpaso.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import com.conicream.alpaso.domain.model.StockProducto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockProductoTest {

    @Test
    @DisplayName("Las cajas se convierten a unidades según las unidades por caja")
    void cajasAUnidades() {
        StockProducto s = StockProducto.crear(1, 24, 10);
        assertEquals(240, s.unidadesDisponibles());
        assertEquals(10, s.cajasCompletas());
        assertEquals(0, s.unidadesSueltas());
    }

    @Test
    void descontarDejaCajasYSueltas() {
        StockProducto s = StockProducto.crear(1, 24, 10);
        s.descontar(50);
        assertEquals(190, s.unidadesDisponibles());
        assertEquals(7, s.cajasCompletas());
        assertEquals(22, s.unidadesSueltas());
    }

    @Test
    @DisplayName("El stock nunca queda por debajo de cero")
    void noBajaDeCero() {
        StockProducto s = StockProducto.crear(1, 24, 1);
        assertThrows(ValorInvalidoException.class, () -> s.descontar(25));
        assertEquals(24, s.unidadesDisponibles());
        s.descontar(24);
        assertEquals(0, s.unidadesDisponibles());
        assertThrows(ValorInvalidoException.class, () -> s.descontar(0));
    }

    @Test
    void reponerCajas() {
        StockProducto s = StockProducto.crear(1, 12, 0);
        s.reponerCajas(3);
        assertEquals(36, s.unidadesDisponibles());
        assertThrows(ValorInvalidoException.class, () -> s.reponerCajas(0));
        assertThrows(ValorInvalidoException.class, () -> s.reponerCajas(-1));
        assertThrows(ValorInvalidoException.class, () -> s.reponerCajas(100_001));
    }

    @Test
    void conteoManualFijaCajasYSueltas() {
        StockProducto s = StockProducto.crear(1, 24, 10);
        s.corregir(2, 5);
        assertEquals(53, s.unidadesDisponibles());
        assertThrows(ValorInvalidoException.class, () -> s.corregir(2, 24));
        assertThrows(ValorInvalidoException.class, () -> s.corregir(-1, 0));
        assertThrows(ValorInvalidoException.class, () -> s.corregir(1, -1));
        assertEquals(53, s.unidadesDisponibles());
    }

    @Test
    void alcanzaPara() {
        StockProducto s = StockProducto.crear(1, 10, 1);
        assertTrue(s.alcanzaPara(10));
        assertFalse(s.alcanzaPara(11));
    }

    @Test
    void validaCajasInicialesYUnidadesPorCaja() {
        assertThrows(ValorInvalidoException.class, () -> StockProducto.crear(1, 24, -1));
        assertThrows(ValorInvalidoException.class, () -> StockProducto.crear(1, 0, 1));
    }
}
