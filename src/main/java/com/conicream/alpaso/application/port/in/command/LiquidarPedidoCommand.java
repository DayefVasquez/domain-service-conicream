package com.conicream.alpaso.application.port.in.command;

import com.conicream.alpaso.application.port.in.Solicitante;
import java.util.Map;

/** @param sobrantes unidades devueltas por id de producto; los productos que no aparecen se toman sin sobrante */
public record LiquidarPedidoCommand(Solicitante solicitante, long pedidoId, Map<Long, Integer> sobrantes) { }
