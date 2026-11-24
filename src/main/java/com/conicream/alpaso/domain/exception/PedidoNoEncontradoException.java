package com.conicream.alpaso.domain.exception;

public class PedidoNoEncontradoException extends NoEncontradoException {
    public PedidoNoEncontradoException(long id) {
        super("Pedido no encontrado: " + id);
    }
}
