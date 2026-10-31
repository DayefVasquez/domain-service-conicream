package com.conicream.alpaso.testsupport;

import com.conicream.alpaso.application.port.out.LiquidacionRepositoryPort;
import com.conicream.alpaso.domain.model.Liquidacion;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

public class LiquidacionRepositoryEnMemoria implements LiquidacionRepositoryPort {

    private final Map<Long, Liquidacion> almacen = new TreeMap<>();
    private final AtomicLong secuencia = new AtomicLong();

    private static Liquidacion copia(Liquidacion l, long id) {
        return Liquidacion.reconstruir(id, l.getPedidoId(), l.getComercianteId(), l.getLineas());
    }

    @Override
    public Liquidacion guardar(Liquidacion l) {
        long id = l.getId() != null ? l.getId() : secuencia.incrementAndGet();
        almacen.put(id, copia(l, id));
        return copia(l, id);
    }

    @Override
    public Optional<Liquidacion> buscarPorPedido(long pedidoId) {
        return almacen.values().stream().filter(l -> l.getPedidoId() == pedidoId).findFirst().map(l -> copia(l, l.getId()));
    }

    @Override
    public List<Liquidacion> buscarPorPedidos(Collection<Long> pedidoIds) {
        return almacen.values().stream().filter(l -> pedidoIds.contains(l.getPedidoId())).map(l -> copia(l, l.getId())).toList();
    }

    public int cantidad() {
        return almacen.size();
    }
}
