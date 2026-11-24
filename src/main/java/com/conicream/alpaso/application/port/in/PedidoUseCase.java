package com.conicream.alpaso.application.port.in;

import com.conicream.alpaso.application.port.in.command.CrearPedidoCommand;
import com.conicream.alpaso.application.port.in.command.RecargarPedidoCommand;
import com.conicream.alpaso.domain.model.Pedido;

public interface PedidoUseCase {
    Pedido crear(CrearPedidoCommand comando);
    Pedido confirmar(Solicitante solicitante, long pedidoId);
    Pedido recargar(RecargarPedidoCommand comando); // Recarga en ruta (Comanda abierta)
    Pedido porId(Solicitante solicitante, long pedidoId);
}
