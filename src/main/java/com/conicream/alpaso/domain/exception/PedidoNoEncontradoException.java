package com.conicream.alpaso.domain.exception;

public class PedidoNoEncontradoException extends NoEncontradoException {
    public PedidoNoEncontradoException(long id) {
        super("No existe el pedido con id " + id);
    }
}
