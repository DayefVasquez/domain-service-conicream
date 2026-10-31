package com.conicream.alpaso.domain.model;

/** Ciclo del pedido: se arma (BORRADOR), se confirma y descuenta stock (CONFIRMADO), se cierra con la liquidación (LIQUIDADO). */
public enum EstadoPedido {
    BORRADOR, CONFIRMADO, LIQUIDADO
}
