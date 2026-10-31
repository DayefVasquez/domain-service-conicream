package com.conicream.alpaso.application.port.in;

/** Quién hace la petición, según el token de AuthCore: su id (campo sub) y si es administrador. */
public record Solicitante(String id, boolean admin) {

    public Solicitante {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El solicitante necesita un identificador");
        }
    }

    public boolean puedeVerPedidoDe(String comercianteId) {
        return admin || id.equals(comercianteId);
    }
}
