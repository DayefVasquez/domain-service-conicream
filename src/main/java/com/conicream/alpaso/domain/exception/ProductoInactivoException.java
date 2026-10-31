package com.conicream.alpaso.domain.exception;

public class ProductoInactivoException extends DominioException {
    public ProductoInactivoException(Long id) {
        super("El producto " + id + " está retirado y no admite esta operación");
    }
}
