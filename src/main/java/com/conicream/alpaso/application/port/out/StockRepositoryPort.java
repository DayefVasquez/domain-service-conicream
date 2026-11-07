package com.conicream.alpaso.application.port.out;

import com.conicream.alpaso.domain.model.StockProducto;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StockRepositoryPort {
    StockProducto guardar(StockProducto stock);

    Optional<StockProducto> buscarPorProducto(long productoId);

    List<StockProducto> buscarPorProductos(Collection<Long> productoIds);
}
