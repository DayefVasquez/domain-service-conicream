package com.conicream.alpaso.domain.model;

import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import com.conicream.alpaso.domain.vo.Money;
import java.util.Collections;
import java.util.List;

/** Cierre de un pedido. Se hace una sola vez; el total es la suma del valor de cada línea. */
public class Liquidacion {

    private final Long id;
    private final long pedidoId;
    private final String comercianteId;
    private final List<LineaLiquidacion> lineas;

    private Liquidacion(Long id, long pedidoId, String comercianteId, List<LineaLiquidacion> lineas) {
        if (comercianteId == null || comercianteId.isBlank()) {
            throw new ValorInvalidoException("El comerciante es obligatorio");
        }
        if (lineas == null || lineas.isEmpty()) {
            throw new ValorInvalidoException("La liquidación debe tener al menos una línea");
        }
        this.id = id;
        this.pedidoId = pedidoId;
        this.comercianteId = comercianteId;
        this.lineas = List.copyOf(lineas);
    }

    public static Liquidacion nueva(long pedidoId, String comercianteId, List<LineaLiquidacion> lineas) {
        return new Liquidacion(null, pedidoId, comercianteId, lineas);
    }

    public static Liquidacion reconstruir(long id, long pedidoId, String comercianteId, List<LineaLiquidacion> lineas) {
        return new Liquidacion(id, pedidoId, comercianteId, lineas);
    }

    public Money total() {
        return lineas.stream().map(LineaLiquidacion::valor).reduce(Money.CERO, Money::sumar);
    }

    public Long getId() { return id; }
    public long getPedidoId() { return pedidoId; }
    public String getComercianteId() { return comercianteId; }
    public List<LineaLiquidacion> getLineas() { return Collections.unmodifiableList(lineas); }
}
