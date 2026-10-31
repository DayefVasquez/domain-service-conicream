package com.conicream.alpaso.infrastructure.adapters.in.rest.dto;

import java.math.BigDecimal;

/** Para crear usa los cinco campos; para editar solo importan nombre, marca y precio. */
public record ProductoRequest(String nombre, String marca, BigDecimal precio, Integer unidadesPorCaja, Integer cajasIniciales) { }
