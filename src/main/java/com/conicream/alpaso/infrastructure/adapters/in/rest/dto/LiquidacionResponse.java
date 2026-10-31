package com.conicream.alpaso.infrastructure.adapters.in.rest.dto;

import com.conicream.alpaso.domain.model.Liquidacion;
import java.math.BigDecimal;
import java.util.List;

public record LiquidacionResponse(Long id, long pedidoId, String comercianteId, BigDecimal total, List<Linea> lineas) {

    public record Linea(long productoId, String producto, int despachadas, int sobrantes, int vendidas,
                        BigDecimal precioUnitario, BigDecimal valor) { }

    public static LiquidacionResponse de(Liquidacion l) {
        return new LiquidacionResponse(l.getId(), l.getPedidoId(), l.getComercianteId(), l.total().valor(),
                l.getLineas().stream()
                        .map(x -> new Linea(x.productoId(), x.nombreProducto(), x.despachadas(), x.sobrantes(), x.vendidas(),
                                x.precioUnitario().valor(), x.valor().valor()))
                        .toList());
    }
}
