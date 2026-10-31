package com.conicream.alpaso.domain.exception;

public class LiquidacionNoEncontradaException extends NoEncontradoException {
    public LiquidacionNoEncontradaException(long pedidoId) {
        super("El pedido " + pedidoId + " todavía no tiene liquidación");
    }
}
