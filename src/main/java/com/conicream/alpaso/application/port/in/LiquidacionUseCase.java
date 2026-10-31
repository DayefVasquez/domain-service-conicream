package com.conicream.alpaso.application.port.in;

import com.conicream.alpaso.application.port.in.command.LiquidarPedidoCommand;
import com.conicream.alpaso.domain.model.Liquidacion;

public interface LiquidacionUseCase {
    Liquidacion liquidar(LiquidarPedidoCommand comando);

    Liquidacion porPedido(Solicitante solicitante, long pedidoId);
}
