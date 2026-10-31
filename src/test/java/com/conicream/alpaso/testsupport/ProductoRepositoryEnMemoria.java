package com.conicream.alpaso.testsupport;

import com.conicream.alpaso.application.port.out.ProductoRepositoryPort;
import com.conicream.alpaso.domain.model.Producto;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

/** Guarda COPIAS, como una base de datos real: modificar un objeto leído no cambia lo guardado. */
public class ProductoRepositoryEnMemoria implements ProductoRepositoryPort {

    private final Map<Long, Producto> almacen = new TreeMap<>();
    private final AtomicLong secuencia = new AtomicLong();

    private static Producto copia(Producto p, long id) {
        return Producto.reconstruir(id, p.getNombre(), p.getMarca(), p.getPrecio(), p.getUnidadesPorCaja(), p.isActivo());
    }

    @Override
    public Producto guardar(Producto p) {
        long id = p.getId() != null ? p.getId() : secuencia.incrementAndGet();
        almacen.put(id, copia(p, id));
        return copia(p, id);
    }

    @Override
    public Optional<Producto> buscarPorId(long id) {
        return Optional.ofNullable(almacen.get(id)).map(p -> copia(p, id));
    }

    @Override
    public Optional<Producto> buscarActivoPorNombreYMarca(String nombre, String marca) {
        return almacen.values().stream()
                .filter(p -> p.isActivo() && p.getNombre().toLowerCase(Locale.ROOT).equals(nombre.toLowerCase(Locale.ROOT))
                        && p.getMarca().toLowerCase(Locale.ROOT).equals(marca.toLowerCase(Locale.ROOT)))
                .findFirst().map(p -> copia(p, p.getId()));
    }

    @Override
    public List<Producto> buscarPorIds(Collection<Long> ids) {
        return ids.stream().map(almacen::get).filter(p -> p != null).map(p -> copia(p, p.getId())).toList();
    }

    @Override
    public List<Producto> listar(boolean soloActivos) {
        return almacen.values().stream().filter(p -> !soloActivos || p.isActivo()).map(p -> copia(p, p.getId())).toList();
    }
}
