package com.conicream.alpaso.application.service;

import com.conicream.alpaso.application.port.in.ReporteUseCase;
import com.conicream.alpaso.application.port.in.result.ArchivoReporte;
import com.conicream.alpaso.application.port.out.LiquidacionRepositoryPort;
import com.conicream.alpaso.application.port.out.PedidoRepositoryPort;
import com.conicream.alpaso.application.port.out.ReporteExportPort;
import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import com.conicream.alpaso.domain.model.EstadoPedido;
import com.conicream.alpaso.domain.model.Liquidacion;
import com.conicream.alpaso.domain.model.Pedido;
import com.conicream.alpaso.domain.model.ReporteDiario;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** HU-12: reporte diario. Reúne los datos con el dominio y le pide el archivo a un puerto; no sabe que existe Excel. */
public class ReporteService implements ReporteUseCase {

    private final PedidoRepositoryPort pedidos;
    private final LiquidacionRepositoryPort liquidaciones;
    private final ReporteExportPort exportador;
    private final Clock reloj;

    public ReporteService(PedidoRepositoryPort pedidos, LiquidacionRepositoryPort liquidaciones,
                          ReporteExportPort exportador, Clock reloj) {
        this.pedidos = pedidos;
        this.liquidaciones = liquidaciones;
        this.exportador = exportador;
        this.reloj = reloj;
    }

    @Override
    public ArchivoReporte generarDiario(LocalDate fecha) {
        LocalDate hoy = LocalDate.now(reloj);
        LocalDate dia = fecha == null ? hoy : fecha;
        if (dia.isAfter(hoy)) {
            throw new ValorInvalidoException("No se puede generar el reporte de una fecha futura");
        }
        List<Pedido> delDia = pedidos.buscarPorFecha(dia).stream().filter(p -> p.getEstado() != EstadoPedido.BORRADOR).toList();
        List<Liquidacion> liquidadas = delDia.isEmpty() ? List.of()
                : liquidaciones.buscarPorPedidos(delDia.stream().map(Pedido::getId).toList());
        ReporteDiario reporte = ReporteDiario.armar(dia, delDia, liquidadas);
        return new ArchivoReporte("reporte-diario-" + dia + ".xlsx", exportador.exportar(reporte));
    }
}
