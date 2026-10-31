package com.conicream.alpaso.testsupport;

import com.conicream.alpaso.application.port.out.StockRepositoryPort;
import com.conicream.alpaso.domain.model.StockProducto;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class StockRepositoryEnMemoria implements StockRepositoryPort {

    private final Map<Long, StockProducto> almacen = new TreeMap<>();

    private static StockProducto copia(StockProducto s) {
        return StockProducto.reconstruir(s.getProductoId(), s.getUnidadesPorCaja(), s.unidadesDisponibles());
    }

    @Override
    public StockProducto guardar(StockProducto stock) {
        almacen.put(stock.getProductoId(), copia(stock));
        return copia(stock);
    }

    @Override
    public Optional<StockProducto> buscarPorProducto(long productoId) {
        return Optional.ofNullable(almacen.get(productoId)).map(StockRepositoryEnMemoria::copia);
    }

    @Override
    public List<StockProducto> buscarPorProductos(Collection<Long> productoIds) {
        return productoIds.stream().map(almacen::get).filter(s -> s != null).map(StockRepositoryEnMemoria::copia).toList();
    }
}
