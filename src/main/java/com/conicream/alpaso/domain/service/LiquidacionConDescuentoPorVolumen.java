package com.conicream.alpaso.domain.service;

import com.conicream.alpaso.domain.model.LineaLiquidacion;
import com.conicream.alpaso.domain.model.Liquidacion;
import com.conicream.alpaso.domain.model.Pedido;
import com.conicream.alpaso.domain.vo.Money;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Strategy (OCP): 5 % de descuento en cada producto con 200 o más unidades VENDIDAS. */
public class LiquidacionConDescuentoPorVolumen implements ReglaLiquidacion {

    private static final int UMBRAL_UNIDADES = 200;
    private static final BigDecimal FACTOR = new BigDecimal("0.95");

    private final ReglaLiquidacion base;

    public LiquidacionConDescuentoPorVolumen(ReglaLiquidacion base) {
        this.base = base;
    }

    @Override
    public Liquidacion calcular(Pedido pedido, Map<Long, Integer> sobrantes) {
        Liquidacion calculada = base.calcular(pedido, sobrantes);
        List<LineaLiquidacion> lineas = new ArrayList<>();
        for (LineaLiquidacion l : calculada.getLineas()) {
            Money precio = l.vendidas() >= UMBRAL_UNIDADES
                    ? Money.de(l.precioUnitario().valor().multiply(FACTOR))
                    : l.precioUnitario();
            lineas.add(new LineaLiquidacion(l.productoId(), l.nombreProducto(),
                    l.despachadas(), l.sobrantes(), precio));
        }
        return Liquidacion.nueva(pedido.getId(), pedido.getComercianteId(), lineas);
    }
}