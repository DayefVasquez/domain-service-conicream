package com.conicream.alpaso.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.conicream.alpaso.domain.model.DetallePedido;
import com.conicream.alpaso.domain.model.EstadoPedido;
import com.conicream.alpaso.domain.model.Pedido;
import com.conicream.alpaso.domain.service.LiquidacionConDescuentoPorVolumen;
import com.conicream.alpaso.domain.service.LiquidacionPorVendidas;
import com.conicream.alpaso.domain.service.ReglaLiquidacion;
import com.conicream.alpaso.domain.vo.Money;
import com.conicream.alpaso.testsupport.Datos;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LiquidacionConDescuentoPorVolumenTest {

    private final ReglaLiquidacion regla = new LiquidacionConDescuentoPorVolumen(new LiquidacionPorVendidas());

    private Pedido pedidoDe(int cantidad) {
        return Pedido.reconstruir(5, "111", Datos.HOY, EstadoPedido.CONFIRMADO,
                List.of(new DetallePedido(1, "Cono", cantidad, Money.de(2500))));
    }

    @Test
    @DisplayName("Con 200 vendidas o más se descuenta el 5 %")
    void descuentoDesdeElUmbral() {
        assertEquals(Money.de(475000), regla.calcular(pedidoDe(200), Map.of()).total()); // 200 × 2.375
        assertEquals(Money.de(712500), regla.calcular(pedidoDe(300), Map.of()).total()); // 300 × 2.375
    }

    @Test
    @DisplayName("Con 199 vendidas no hay descuento")
    void sinDescuentoPorDebajoDelUmbral() {
        assertEquals(Money.de(497500), regla.calcular(pedidoDe(199), Map.of()).total()); // 199 × 2.500
    }

    @Test
    @DisplayName("El umbral se mide sobre lo vendido, no sobre lo despachado")
    void seMideSobreLoVendido() {
        // despachadas 250, sobran 60 → vendidas 190 → sin descuento
        assertEquals(Money.de(475000), regla.calcular(pedidoDe(250), Map.of(1L, 60)).total());
    }
}