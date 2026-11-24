package com.conicream.alpaso.application.port.in.command;

import java.math.BigDecimal;

public record EditarProductoCommand(
        long id,
        String codigo,
        String nombre,
        String marca,
        BigDecimal precio
) { }
