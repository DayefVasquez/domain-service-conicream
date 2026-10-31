package com.conicream.alpaso.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.conicream.alpaso.domain.exception.StockInsuficienteException;
import com.conicream.alpaso.domain.model.DetallePedido;
import com.conicream.alpaso.domain.model.EstadoPedido;
import com.conicream.alpaso.domain.model.LineaLiquidacion;
import com.conicream.alpaso.domain.model.Liquidacion;
import com.conicream.alpaso.domain.model.Pedido;
import com.conicream.alpaso.domain.model.ReporteDiario;
import com.conicream.alpaso.domain.model.StockProducto;
import com.conicream.alpaso.domain.service.ValidadorStock;
import com.conicream.alpaso.domain.vo.Money;
import com.conicream.alpaso.testsupport.Datos;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ValidadorStockYReporteTest {

    @Test
    @DisplayName("El validador informa TODOS los productos que no alcanzan y no modifica el stock")
    void informaTodosLosFaltantes() {
        StockProducto a = StockProducto.reconstruir(1, 24, 10);
        StockProducto b = StockProducto.reconstruir(2, 24, 100);
        List<DetallePedido> lineas = List.of(new DetallePedido(1, "A", 11, Money.de(1)),
                new DetallePedido(2, "B", 50, Money.de(1)), new DetallePedido(3, "C", 1, Money.de(1)));

        StockInsuficienteException e = assertThrows(StockInsuficienteException.class,
                () -> new ValidadorStock().validar(lineas, Map.of(1L, a, 2L, b)));

        assertEquals(2, e.getFaltantes().size());
        assertEquals(10, e.getFaltantes().get(0).disponible());
        assertEquals(0, e.getFaltantes().get(1).disponible());   // C no tiene stock registrado
        assertEquals(10, a.unidadesDisponibles());
    }

    @Test
    void stockExactoAlcanza() {
        new ValidadorStock().validar(List.of(new DetallePedido(1, "A", 10, Money.de(1))),
                Map.of(1L, StockProducto.reconstruir(1, 24, 10)));
    }

    private Pedido pedido(long id, EstadoPedido estado, LocalDate fecha) {
        return Pedido.reconstruir(id, "111", fecha, estado, List.of(new DetallePedido(1, "Cono", 10, Money.de(2500))));
    }

    @Test
    @DisplayName("El reporte ignora borradores y otras fechas, ordena por pedido y cuenta pendientes")
    void armaElReporteDelDia() {
        Pedido liquidado = pedido(2, EstadoPedido.LIQUIDADO, Datos.HOY);
        Pedido pendiente = pedido(1, EstadoPedido.CONFIRMADO, Datos.HOY);
        Pedido borrador = pedido(3, EstadoPedido.BORRADOR, Datos.HOY);
        Pedido ayer = pedido(4, EstadoPedido.LIQUIDADO, Datos.HOY.minusDays(1));
        Liquidacion liq = Liquidacion.reconstruir(1, 2, "111", List.of(new LineaLiquidacion(1, "Cono", 10, 2, Money.de(2500))));

        ReporteDiario r = ReporteDiario.armar(Datos.HOY, List.of(liquidado, pendiente, borrador, ayer), List.of(liq));

        assertEquals(2, r.totalPedidos());
        assertEquals(1L, r.entradas().get(0).pedido().getId().longValue());
        assertFalse(r.entradas().get(0).estaLiquidado());
        assertEquals(1, r.pedidosLiquidados());
        assertEquals(Money.de(20000), r.totalLiquidado());
    }

    @Test
    void sinPedidosElReporteEstaEnCero() {
        ReporteDiario r = ReporteDiario.armar(Datos.HOY, List.of(), List.of());
        assertEquals(0, r.totalPedidos());
        assertEquals(Money.CERO, r.totalLiquidado());
    }
}
