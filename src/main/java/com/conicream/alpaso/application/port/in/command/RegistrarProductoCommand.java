package com.conicream.alpaso.application.port.in.command;

import java.math.BigDecimal;

/** Comando para dar de alta un producto con su código de fábrica Crem Helado y stock inicial directo. */
public record RegistrarProductoCommand(
    String codigo,
    String nombre,
    String marca,
    BigDecimal precio,
    int unidadesIniciales,
    int stockMinimo
) {}
