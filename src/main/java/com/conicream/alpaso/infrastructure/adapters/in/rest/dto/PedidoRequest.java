package com.conicream.alpaso.infrastructure.adapters.in.rest.dto;

import java.util.List;

public record PedidoRequest(List<Linea> lineas) {
    public record Linea(Long productoId, Integer cantidad) { }
}
