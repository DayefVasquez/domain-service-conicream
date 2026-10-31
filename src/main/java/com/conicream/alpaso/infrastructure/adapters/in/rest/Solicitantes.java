package com.conicream.alpaso.infrastructure.adapters.in.rest;

import com.conicream.alpaso.application.port.in.Solicitante;
import com.conicream.alpaso.infrastructure.security.UsuarioAutenticado;

/** Traduce "quien llama" (token de AuthCore) al tipo que entiende la aplicación. El id del comerciante es el "sub" del token. */
final class Solicitantes {

    private Solicitantes() { }

    static Solicitante de(UsuarioAutenticado usuario) {
        return new Solicitante(usuario.username(), usuario.roles().contains("ADMIN"));
    }
}
