package com.conicream.alpaso.application.port.in.command;

import com.conicream.alpaso.application.port.in.Solicitante;
import java.util.List;

public record CrearPedidoCommand(Solicitante solicitante, List<Linea> lineas) {

    public record Linea(long productoId, int cantidad) { }
}
