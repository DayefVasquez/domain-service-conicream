package com.conicream.alpaso.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import com.conicream.alpaso.domain.model.DetallePedido;
import com.conicream.alpaso.domain.model.EstadoPedido;
import com.conicream.alpaso.domain.model.LineaLiquidacion;
import com.conicream.alpaso.domain.model.Liquidacion;
import com.conicream.alpaso.domain.model.Pedido;
import com.conicream.alpaso.domain.service.LiquidacionPorVendidas;
import com.conicream.alpaso.domain.service.ReglaLiquidacion;
import com.conicream.alpaso.domain.vo.Money;
import com.conicream.alpaso.testsupport.Datos;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LiquidacionTest {

    private Pedido pedido() {
        return Pedido.reconstruir(5, "111", Datos.HOY, EstadoPedido.CONFIRMADO, List.of(
                new DetallePedido(1, "Cono", 100, Money.de(2500)),
                new DetallePedido(2, "Paleta", 40, Money.de(1500))));
    }

    @Test
    @DisplayName("Valor de cada línea = (despachadas − sobrantes) × precio")
    void formulaDeLaLinea() {
        LineaLiquidacion l = new LineaLiquidacion(1, "Cono", 100, 15, Money.de(2500));
        assertEquals(85, l.vendidas());
        assertEquals(Money.de(212500), l.valor());
    }

    @Test
    void lineaValidaSobrantes() {
        assertThrows(ValorInvalidoException.class, () -> new LineaLiquidacion(1, "Cono", 10, 11, Money.de(1)));
        assertThrows(ValorInvalidoException.class, () -> new LineaLiquidacion(1, "Cono", 10, -1, Money.de(1)));
        assertEquals(Money.CERO, new LineaLiquidacion(1, "Cono", 10, 10, Money.de(1)).valor());
    }

    @Test
    void reglaPorVendidasLiquidaTodoElPedido() {
        Liquidacion l = new LiquidacionPorVendidas().calcular(pedido(), Map.of(1L, 10, 2L, 40));
        assertEquals(2, l.getLineas().size());
        // (100-10)*2500 + (40-40)*1500
        assertEquals(Money.de(225000), l.total());
        assertEquals(5L, l.getPedidoId());
        assertEquals("111", l.getComercianteId());
    }

    @Test
    void sinSobrantesSeCobraTodoLoDespachado() {
        Liquidacion l = new LiquidacionPorVendidas().calcular(pedido(), Map.of());
        assertEquals(Money.de(310000), l.total());
        assertEquals(Money.de(310000), new LiquidacionPorVendidas().calcular(pedido(), null).total());
    }

    @Test
    @DisplayName("Un sobrante mayor que lo despachado, o de un producto ajeno al pedido, se rechaza")
    void sobrantesInvalidos() {
        LiquidacionPorVendidas regla = new LiquidacionPorVendidas();
        assertThrows(ValorInvalidoException.class, () -> regla.calcular(pedido(), Map.of(1L, 101)));
        assertThrows(ValorInvalidoException.class, () -> regla.calcular(pedido(), Map.of(1L, -1)));
        assertThrows(ValorInvalidoException.class, () -> regla.calcular(pedido(), Map.of(99L, 1)));
    }

    /** OCP: una regla nueva se agrega con una clase nueva, sin tocar LiquidacionPorVendidas ni el caso de uso. */
    @Test
    @DisplayName("OCP: se puede agregar otra regla de liquidación sin modificar la existente")
    void segundaReglaSinEditarLaPrimera() {
        ReglaLiquidacion conDescuento = (pedido, sobrantes) -> {
            Liquidacion base = new LiquidacionPorVendidas().calcular(pedido, sobrantes);
            List<LineaLiquidacion> rebajadas = base.getLineas().stream()
                    .map(l -> new LineaLiquidacion(l.productoId(), l.nombreProducto(), l.despachadas(), l.sobrantes(),
                            Money.de(l.precioUnitario().valor().multiply(new java.math.BigDecimal("0.90")))))
                    .toList();
            return Liquidacion.nueva(base.getPedidoId(), base.getComercianteId(), rebajadas);
        };
        assertEquals(Money.de(279000), conDescuento.calcular(pedido(), Map.of()).total());
        assertEquals(Money.de(310000), new LiquidacionPorVendidas().calcular(pedido(), Map.of()).total());
    }
}
