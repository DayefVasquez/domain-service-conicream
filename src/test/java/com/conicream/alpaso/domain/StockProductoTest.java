package com.conicream.alpaso.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import com.conicream.alpaso.domain.model.StockProducto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Dominio: StockProducto en unidades puras")
class StockProductoTest {

    @Test
    @DisplayName("Crea stock inicial en unidades y valida umbral mínimo")
    void crearStockInicial() {
        StockProducto s = StockProducto.crear(1L, 100, 20);
        assertEquals(100, s.unidadesDisponibles());
        assertEquals(20, s.getStockMinimo());
        assertFalse(s.estaBajoDeStock());
    }

    @Test
    @DisplayName("Alerta cuando las existencias caen al umbral mínimo o por debajo")
    void alertaStockBajo() {
        StockProducto s = StockProducto.crear(1L, 15, 20);
        assertTrue(s.estaBajoDeStock());
    }

    @Test
    @DisplayName("Repone unidades correctamente y rechaza cantidades negativas")
    void reponerUnidades() {
        StockProducto s = StockProducto.crear(1L, 50, 10);
        s.reponer(30);
        assertEquals(80, s.unidadesDisponibles());

        assertThrows(ValorInvalidoException.class, () -> s.reponer(0));
        assertThrows(ValorInvalidoException.class, () -> s.reponer(-5));
    }

    @Test
    @DisplayName("Reintegra sobrantes de la liquidación al inventario disponible")
    void reintegrarSobrantes() {
        StockProducto s = StockProducto.crear(1L, 50, 10);
        s.descontar(20); // Salieron 20 a la calle -> Quedan 30
        assertEquals(30, s.unidadesDisponibles());

        s.reintegrar(5); // Regresaron 5 no vendidos -> Quedan 35
        assertEquals(35, s.unidadesDisponibles());
    }

    @Test
    @DisplayName("Ajuste físico manual fija el inventario real")
    void ajusteFisico() {
        StockProducto s = StockProducto.crear(1L, 50, 10);
        s.ajustarFisico(42);
        assertEquals(42, s.unidadesDisponibles());

        assertThrows(ValorInvalidoException.class, () -> s.ajustarFisico(-1));
    }

    @Test
    @DisplayName("Descuenta unidades y rechaza si no hay suficiente")
    void descontarUnidades() {
        StockProducto s = StockProducto.crear(1L, 10, 5);
        s.descontar(8);
        assertEquals(2, s.unidadesDisponibles());

        assertThrows(ValorInvalidoException.class, () -> s.descontar(5));
    }
}
