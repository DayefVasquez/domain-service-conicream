package com.conicream.alpaso.application.port.in.command;

/** Entrada directa de unidades físicas al congelador (sin cajas intermedias). */
public record ReponerStockCommand(long productoId, int unidades) { }
