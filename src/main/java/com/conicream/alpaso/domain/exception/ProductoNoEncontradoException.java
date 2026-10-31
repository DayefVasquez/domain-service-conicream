package com.conicream.alpaso.domain.exception;

public class ProductoNoEncontradoException extends NoEncontradoException {
    public ProductoNoEncontradoException(long id) {
        super("No existe el producto con id " + id);
    }
}
