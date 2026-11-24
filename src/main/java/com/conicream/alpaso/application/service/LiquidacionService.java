package com.conicream.alpaso.application.service;

import com.conicream.alpaso.application.port.in.LiquidacionUseCase;
import com.conicream.alpaso.application.port.in.Solicitante;
import com.conicream.alpaso.application.port.in.command.LiquidarPedidoCommand;
import com.conicream.alpaso.application.port.out.LiquidacionRepositoryPort;
import com.conicream.alpaso.application.port.out.PedidoRepositoryPort;
import com.conicream.alpaso.application.port.out.StockRepositoryPort;
import com.conicream.alpaso.application.port.out.TransaccionPort;
import com.conicream.alpaso.domain.exception.AccesoDenegadoException;
import com.conicream.alpaso.domain.exception.LiquidacionNoEncontradaException;
import com.conicream.alpaso.domain.exception.PedidoNoEncontradoException;
import com.conicream.alpaso.domain.exception.ProductoNoEncontradoException;
import com.conicream.alpaso.domain.model.EstadoPedido;
import com.conicream.alpaso.domain.model.LineaLiquidacion;
import com.conicream.alpaso.domain.model.Liquidacion;
import com.conicream.alpaso.domain.model.Pedido;
import com.conicream.alpaso.domain.model.StockProducto;
import com.conicream.alpaso.domain.service.ReglaLiquidacion;
import com.conicream.alpaso.application.port.in.result.ArchivoReporte;
import com.conicream.alpaso.application.port.out.ComprobantePdfPort;

public class LiquidacionService implements LiquidacionUseCase {

    private final PedidoRepositoryPort pedidos;
    private final LiquidacionRepositoryPort liquidaciones;
    private final StockRepositoryPort stocks;
    private final TransaccionPort transaccion;
    private final ReglaLiquidacion regla;
    private final ComprobantePdfPort pdfExportador;

    public LiquidacionService(PedidoRepositoryPort pedidos, LiquidacionRepositoryPort liquidaciones,
                              StockRepositoryPort stocks, TransaccionPort transaccion,
                              ReglaLiquidacion regla, ComprobantePdfPort pdfExportador) {
        this.pedidos = pedidos;
        this.liquidaciones = liquidaciones;
        this.stocks = stocks;
        this.transaccion = transaccion;
        this.regla = regla;
        this.pdfExportador = pdfExportador;
    }

    @Override
    public ArchivoReporte generarComprobantePdf(Solicitante solicitante, long pedidoId) {
        cargar(solicitante, pedidoId);
        Liquidacion liq = liquidaciones.buscarPorPedido(pedidoId)
                .orElseThrow(() -> new LiquidacionNoEncontradaException(pedidoId));
        byte[] bytes = pdfExportador.generar(liq);
        return new ArchivoReporte("comprobante-pedido-" + pedidoId + ".pdf", bytes);
    }

    @Override
    public Liquidacion liquidar(LiquidarPedidoCommand c) {
        return transaccion.ejecutar(() -> {
            Pedido pedido = cargar(c.solicitante(), c.pedidoId());
            pedido.asegurarEstado(EstadoPedido.CONFIRMADO);

            Liquidacion calculada = regla.calcular(pedido, c.sobrantes());

            // [SOLUCIÓN AL BUG]: Reintegrar físicamente al congelador los helados devueltos
            for (LineaLiquidacion linea : calculada.getLineas()) {
                if (linea.sobrantes() > 0) {
                    StockProducto stock = stocks.buscarPorProducto(linea.productoId())
                            .orElseThrow(() -> new ProductoNoEncontradoException(linea.productoId()));
                    stock.reintegrar(linea.sobrantes());
                    stocks.guardar(stock);
                }
            }

            pedido.marcarLiquidado();
            Liquidacion guardada = liquidaciones.guardar(calculada);
            pedidos.guardar(pedido);
            return guardada;
        });
    }

    @Override
    public Liquidacion porPedido(Solicitante solicitante, long pedidoId) {
        cargar(solicitante, pedidoId);
        return liquidaciones.buscarPorPedido(pedidoId)
                .orElseThrow(() -> new LiquidacionNoEncontradaException(pedidoId));
    }

    private Pedido cargar(Solicitante solicitante, long pedidoId) {
        Pedido pedido = pedidos.buscarPorId(pedidoId)
                .orElseThrow(() -> new PedidoNoEncontradoException(pedidoId));
        if (!solicitante.puedeVerPedidoDe(pedido.getComercianteId())) {
            throw new AccesoDenegadoException();
        }
        return pedido;
    }
}
