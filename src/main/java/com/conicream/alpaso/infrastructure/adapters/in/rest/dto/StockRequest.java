package com.conicream.alpaso.infrastructure.adapters.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StockRequest {

    private final int unidades;

    @JsonCreator
    public StockRequest(
            @JsonProperty("unidades")
            @JsonAlias({"unidadesReales", "cajas"})
            Integer unidades
    ) {
        this.unidades = unidades != null ? unidades : 0;
    }

    public int unidades() {
        return unidades;
    }

    public int unidadesReales() {
        return unidades;
    }
}
