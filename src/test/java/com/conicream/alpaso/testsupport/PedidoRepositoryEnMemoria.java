package com.conicream.alpaso.testsupport;

import com.conicream.alpaso.application.port.out.PedidoRepositoryPort;
import com.conicream.alpaso.domain.model.Pedido;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

public class PedidoRepositoryEnMemoria implements PedidoRepositoryPort {

    private final Map<Long, Pedido> almacen = new TreeMap<>();
    private final AtomicLong secuencia = new AtomicLong();

    private static Pedido copia(Pedido p, long id) {
        return Pedido.reconstruir(id, p.getComercianteId(), p.getFecha(), p.getEstado(), p.getDetalles());
    }

    @Override
    public Pedido guardar(Pedido p) {
        long id = p.getId() != null ? p.getId() : secuencia.incrementAndGet();
        almacen.put(id, copia(p, id));
        return copia(p, id);
    }

    @Override
    public Optional<Pedido> buscarPorId(long id) {
        return Optional.ofNullable(almacen.get(id)).map(p -> copia(p, id));
    }

    @Override
    public List<Pedido> buscarPorFecha(LocalDate fecha) {
        return almacen.values().stream().filter(p -> p.getFecha().equals(fecha)).map(p -> copia(p, p.getId())).toList();
    }
}
