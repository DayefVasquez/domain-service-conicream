package com.conicream.alpaso.application.service;

import com.conicream.alpaso.application.port.in.LiquidacionUseCase;
import com.conicream.alpaso.application.port.in.Solicitante;
import com.conicream.alpaso.application.port.in.command.LiquidarPedidoCommand;
import com.conicream.alpaso.application.port.out.LiquidacionRepositoryPort;
import com.conicream.alpaso.application.port.out.PedidoRepositoryPort;
import com.conicream.alpaso.application.port.out.TransaccionPort;
import com.conicream.alpaso.domain.exception.AccesoDenegadoException;
import com.conicream.alpaso.domain.exception.LiquidacionNoEncontradaException;
import com.conicream.alpaso.domain.exception.PedidoNoEncontradoException;
import com.conicream.alpaso.domain.model.EstadoPedido;
import com.conicream.alpaso.domain.model.Liquidacion;
import com.conicream.alpaso.domain.model.Pedido;
import com.conicream.alpaso.domain.service.ReglaLiquidacion;

/**
 * HU-11: liquidación de un pedido. La fórmula NO está aquí: viene de la {@link ReglaLiquidacion} que se inyecta (SRP y OCP).
 * Este servicio solo carga, delega, marca el pedido como liquidado y guarda.
 */
public class LiquidacionService implements LiquidacionUseCase {

    private final PedidoRepositoryPort pedidos;
    private final LiquidacionRepositoryPort liquidaciones;
    private final TransaccionPort transaccion;
    private final ReglaLiquidacion regla;

    public LiquidacionService(PedidoRepositoryPort pedidos, LiquidacionRepositoryPort liquidaciones,
                              TransaccionPort transaccion, ReglaLiquidacion regla) {
        this.pedidos = pedidos;
        this.liquidaciones = liquidaciones;
        this.transaccion = transaccion;
        this.regla = regla;
    }

    @Override
    public Liquidacion liquidar(LiquidarPedidoCommand c) {
        return transaccion.ejecutar(() -> {
            Pedido pedido = cargar(c.solicitante(), c.pedidoId());
            pedido.asegurarEstado(EstadoPedido.CONFIRMADO);
            Liquidacion calculada = regla.calcular(pedido, c.sobrantes());
            pedido.marcarLiquidado();
            Liquidacion guardada = liquidaciones.guardar(calculada);
            pedidos.guardar(pedido);
            return guardada;
        });
    }

    @Override
    public Liquidacion porPedido(Solicitante solicitante, long pedidoId) {
        cargar(solicitante, pedidoId);
        return liquidaciones.buscarPorPedido(pedidoId).orElseThrow(() -> new LiquidacionNoEncontradaException(pedidoId));
    }

    private Pedido cargar(Solicitante solicitante, long pedidoId) {
        Pedido pedido = pedidos.buscarPorId(pedidoId).orElseThrow(() -> new PedidoNoEncontradoException(pedidoId));
        if (!solicitante.puedeVerPedidoDe(pedido.getComercianteId())) {
            throw new AccesoDenegadoException();
        }
        return pedido;
    }
}
