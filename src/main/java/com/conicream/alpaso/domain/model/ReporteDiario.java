package com.conicream.alpaso.domain.model;

import com.conicream.alpaso.domain.vo.Money;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Foto de un día: sus pedidos confirmados o liquidados y, cuando existe, la liquidación de cada uno. */
public record ReporteDiario(LocalDate fecha, List<Entrada> entradas) {

    /** La liquidación es null mientras el pedido siga CONFIRMADO. */
    public record Entrada(Pedido pedido, Liquidacion liquidacion) {
        public boolean estaLiquidado() {
            return liquidacion != null;
        }
    }

    public ReporteDiario {
        entradas = List.copyOf(entradas);
    }

    /** Arma el reporte: ignora borradores y pedidos de otras fechas; ordena por número de pedido. */
    public static ReporteDiario armar(LocalDate fecha, List<Pedido> pedidos, List<Liquidacion> liquidaciones) {
        Map<Long, Liquidacion> porPedido = liquidaciones.stream()
                .collect(Collectors.toMap(Liquidacion::getPedidoId, Function.identity(), (a, b) -> a));
        List<Entrada> entradas = pedidos.stream()
                .filter(p -> p.getFecha().equals(fecha) && p.getEstado() != EstadoPedido.BORRADOR)
                .sorted(Comparator.comparing(Pedido::getId))
                .map(p -> new Entrada(p, porPedido.get(p.getId())))
                .toList();
        return new ReporteDiario(fecha, entradas);
    }

    public int totalPedidos() {
        return entradas.size();
    }

    public int pedidosLiquidados() {
        return (int) entradas.stream().filter(Entrada::estaLiquidado).count();
    }

    public Money totalLiquidado() {
        return entradas.stream().filter(Entrada::estaLiquidado)
                .map(e -> e.liquidacion().total()).reduce(Money.CERO, Money::sumar);
    }
}
