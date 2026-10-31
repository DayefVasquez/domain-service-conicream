package com.conicream.alpaso.domain.exception;

public class ProductoDuplicadoException extends DuplicadoException {
    public ProductoDuplicadoException(String nombre, String marca) {
        super("Ya existe un producto activo llamado '" + nombre + "' de la marca '" + marca + "'");
    }
}
