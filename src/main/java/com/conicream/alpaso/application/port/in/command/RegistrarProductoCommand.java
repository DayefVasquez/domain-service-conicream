package com.conicream.alpaso.application.port.in.command;

import java.math.BigDecimal;

public record RegistrarProductoCommand(String nombre, String marca, BigDecimal precio, int unidadesPorCaja, int cajasIniciales) { }
