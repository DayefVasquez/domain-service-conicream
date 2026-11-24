package com.conicream.alpaso.application.port.in.command;

/** Ajuste manual por conteo físico en bodega. */
public record CorregirStockCommand(long productoId, int unidadesReales) { }
