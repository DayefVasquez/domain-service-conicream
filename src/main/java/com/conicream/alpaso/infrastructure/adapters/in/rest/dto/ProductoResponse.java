package com.conicream.alpaso.infrastructure.adapters.in.rest.dto;

import com.conicream.alpaso.application.port.in.result.ProductoConStock;
import java.math.BigDecimal;

public record ProductoResponse(Long id, String nombre, String marca, String nombreCompleto, BigDecimal precio,
                               int unidadesPorCaja, boolean activo, int cajas, int unidadesSueltas, int unidadesDisponibles) {

    public static ProductoResponse de(ProductoConStock pc) {
        var p = pc.producto();
        var s = pc.stock();
        return new ProductoResponse(p.getId(), p.getNombre(), p.getMarca(), p.nombreCompleto(), p.getPrecio().valor(),
                p.getUnidadesPorCaja(), p.isActivo(), s.cajasCompletas(), s.unidadesSueltas(), s.unidadesDisponibles());
    }
}
