package com.conicream.alpaso.application.port.in.command;

import com.conicream.alpaso.application.port.in.Solicitante;
import java.util.List;

/** Comando para anexar helados adicionales a la comanda abierta del comerciante en ruta. */
public record RecargarPedidoCommand(
        Solicitante solicitante,
        long pedidoId,
        List<Linea> lineas
) {
    public record Linea(long productoId, int cantidad) { }
}
