package com.conicream.alpaso.domain.exception;

import java.util.List;

/** Reúne TODOS los productos que no alcanzan, para que quien pide sepa qué corregir de una vez. */
public class StockInsuficienteException extends DominioException {

    public record Faltante(long productoId, String nombre, int disponible, int solicitado) { }

    private final List<Faltante> faltantes;

    public StockInsuficienteException(List<Faltante> faltantes) {
        super("Stock insuficiente para " + faltantes.size() + " producto(s)");
        this.faltantes = List.copyOf(faltantes);
    }

    public List<Faltante> getFaltantes() {
        return faltantes;
    }
}
