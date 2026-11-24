package com.conicream.alpaso.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.conicream.alpaso.domain.exception.StockInsuficienteException;
import com.conicream.alpaso.domain.model.DetallePedido;
import com.conicream.alpaso.domain.model.StockProducto;
import com.conicream.alpaso.domain.service.ValidadorStock;
import com.conicream.alpaso.domain.vo.Money;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Dominio: ValidadorStock")
class ValidadorStockYReporteTest {

    @Test
    @DisplayName("El validador informa TODOS los productos que no alcanzan y no modifica el stock")
    void informaTodosLosFaltantes() {
        StockProducto a = StockProducto.reconstruir(1, 10, 5);  // Disponible: 10, Pedido: 11 (Falta)
        StockProducto b = StockProducto.reconstruir(2, 100, 10); // Disponible: 100, Pedido: 50 (Alcanza)
        List<DetallePedido> lineas = List.of(
                new DetallePedido(1, "A", 11, Money.de(1)),
                new DetallePedido(2, "B", 50, Money.de(1)),
                new DetallePedido(3, "C", 1, Money.de(1)) // No registrado (Falta)
        );

        StockInsuficienteException ex = assertThrows(StockInsuficienteException.class,
                () -> new ValidadorStock().validar(lineas, Map.of(1L, a, 2L, b)));

        assertEquals(2, ex.getFaltantes().size());
        assertEquals(10, ex.getFaltantes().get(0).disponible());
        assertEquals(0, ex.getFaltantes().get(1).disponible()); // C no tiene stock registrado
        assertEquals(10, a.unidadesDisponibles());
    }

    @Test
    void stockExactoAlcanza() {
        new ValidadorStock().validar(List.of(new DetallePedido(1, "A", 10, Money.de(1))),
                Map.of(1L, StockProducto.reconstruir(1, 10, 5)));
    }
}
