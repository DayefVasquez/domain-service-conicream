package com.conicream.alpaso.infrastructure.adapters.in.rest.dto;

import com.conicream.alpaso.domain.model.Pedido;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PedidoResponse(Long id, String comercianteId, LocalDate fecha, String estado, BigDecimal total, List<Detalle> detalles) {

    public record Detalle(long productoId, String producto, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal) { }

    public static PedidoResponse de(Pedido p) {
        return new PedidoResponse(p.getId(), p.getComercianteId(), p.getFecha(), p.getEstado().name(), p.total().valor(),
                p.getDetalles().stream()
                        .map(d -> new Detalle(d.productoId(), d.nombreProducto(), d.cantidad(), d.precioUnitario().valor(), d.subtotal().valor()))
                        .toList());
    }
}
