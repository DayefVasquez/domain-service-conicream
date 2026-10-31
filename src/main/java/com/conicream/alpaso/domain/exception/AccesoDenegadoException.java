package com.conicream.alpaso.domain.exception;

/** El usuario está autenticado, pero el recurso es de otro comerciante. */
public class AccesoDenegadoException extends DominioException {
    public AccesoDenegadoException() {
        super("Este pedido pertenece a otro comerciante");
    }
}
