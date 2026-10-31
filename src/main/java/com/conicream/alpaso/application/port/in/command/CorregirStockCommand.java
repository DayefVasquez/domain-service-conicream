package com.conicream.alpaso.application.port.in.command;

public record CorregirStockCommand(long productoId, int cajas, int unidadesSueltas) { }
