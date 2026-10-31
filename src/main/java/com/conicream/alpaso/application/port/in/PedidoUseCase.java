package com.conicream.alpaso.application.port.in;

import com.conicream.alpaso.application.port.in.command.CrearPedidoCommand;
import com.conicream.alpaso.domain.model.Pedido;

public interface PedidoUseCase {
    Pedido crear(CrearPedidoCommand comando);

    /** Pasa el pedido a CONFIRMADO y descuenta el stock, todo o nada. */
    Pedido confirmar(Solicitante solicitante, long pedidoId);

    Pedido porId(Solicitante solicitante, long pedidoId);
}
