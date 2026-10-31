package com.conicream.alpaso.domain.exception;

import com.conicream.alpaso.domain.model.EstadoPedido;

public class PedidoEstadoInvalidoException extends DominioException {

    private final EstadoPedido actual;

    public PedidoEstadoInvalidoException(Long pedidoId, EstadoPedido actual, EstadoPedido esperado) {
        super("El pedido " + pedidoId + " está " + actual + " y esta operación exige que esté " + esperado);
        this.actual = actual;
    }

    public EstadoPedido getActual() {
        return actual;
    }
}
