package com.conicream.alpaso.infrastructure.adapters.in.rest.dto;

import java.util.List;

/** Lo que le sobró al comerciante por producto. Un producto que no aparece se toma como "no sobró nada". */
public record LiquidacionRequest(List<Sobrante> sobrantes) {
    public record Sobrante(Long productoId, Integer cantidad) { }
}
