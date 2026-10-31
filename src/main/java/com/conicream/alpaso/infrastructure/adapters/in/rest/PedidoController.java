package com.conicream.alpaso.infrastructure.adapters.in.rest;

import com.conicream.alpaso.application.port.in.LiquidacionUseCase;
import com.conicream.alpaso.application.port.in.PedidoUseCase;
import com.conicream.alpaso.application.port.in.command.CrearPedidoCommand;
import com.conicream.alpaso.application.port.in.command.LiquidarPedidoCommand;
import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import com.conicream.alpaso.infrastructure.adapters.in.rest.dto.*;
import com.conicream.alpaso.infrastructure.security.UsuarioAutenticado;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Pedidos (HU-10) y liquidación (HU-11). El dueño sale del token, nunca del cuerpo de la petición. */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoUseCase pedidos;
    private final LiquidacionUseCase liquidaciones;

    public PedidoController(PedidoUseCase pedidos, LiquidacionUseCase liquidaciones) {
        this.pedidos = pedidos;
        this.liquidaciones = liquidaciones;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> crear(@AuthenticationPrincipal UsuarioAutenticado usuario, @RequestBody PedidoRequest r) {
        List<CrearPedidoCommand.Linea> lineas = (r.lineas() == null ? List.<PedidoRequest.Linea>of() : r.lineas()).stream()
                .map(l -> {
                    if (l.productoId() == null || l.cantidad() == null) {
                        throw new ValorInvalidoException("Cada línea necesita productoId y cantidad");
                    }
                    return new CrearPedidoCommand.Linea(l.productoId(), l.cantidad());
                }).toList();
        var creado = pedidos.crear(new CrearPedidoCommand(Solicitantes.de(usuario), lineas));
        return ResponseEntity.created(URI.create("/api/pedidos/" + creado.getId())).body(PedidoResponse.de(creado));
    }

    @GetMapping("/{id}")
    public PedidoResponse obtener(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable long id) {
        return PedidoResponse.de(pedidos.porId(Solicitantes.de(usuario), id));
    }

    @PostMapping("/{id}/confirmacion")
    public PedidoResponse confirmar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable long id) {
        return PedidoResponse.de(pedidos.confirmar(Solicitantes.de(usuario), id));
    }

    @PostMapping("/{id}/liquidacion")
    public ResponseEntity<LiquidacionResponse> liquidar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                        @PathVariable long id, @RequestBody(required = false) LiquidacionRequest r) {
        Map<Long, Integer> sobrantes = new HashMap<>();
        if (r != null && r.sobrantes() != null) {
            for (LiquidacionRequest.Sobrante s : r.sobrantes()) {
                if (s.productoId() == null || s.cantidad() == null) {
                    throw new ValorInvalidoException("Cada sobrante necesita productoId y cantidad");
                }
                sobrantes.merge(s.productoId(), s.cantidad(), Integer::sum);
            }
        }
        var l = liquidaciones.liquidar(new LiquidarPedidoCommand(Solicitantes.de(usuario), id, sobrantes));
        return ResponseEntity.created(URI.create("/api/pedidos/" + id + "/liquidacion")).body(LiquidacionResponse.de(l));
    }

    @GetMapping("/{id}/liquidacion")
    public LiquidacionResponse liquidacion(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable long id) {
        return LiquidacionResponse.de(liquidaciones.porPedido(Solicitantes.de(usuario), id));
    }
}
