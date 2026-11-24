package com.conicream.alpaso.infrastructure.adapters.in.rest.dto;

import java.math.BigDecimal;

public record ProductoRequest(
        String codigo,
        String nombre,
        String marca,
        BigDecimal precio,
        int unidadesIniciales,
        int stockMinimo
) { }
