package com.conicream.alpaso.infrastructure.adapters.in.rest;

import com.conicream.alpaso.application.port.in.Solicitante;
import com.conicream.alpaso.infrastructure.security.UsuarioAutenticado;

final class Solicitantes {

    private Solicitantes() { }

    static Solicitante de(UsuarioAutenticado usuario) {
        boolean esAdmin = usuario.roles().contains("ADMIN") || usuario.roles().contains("ADMINISTRADOR");
        return new Solicitante(usuario.username(), esAdmin);
    }
}
