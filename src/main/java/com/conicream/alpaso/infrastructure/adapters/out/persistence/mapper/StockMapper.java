package com.conicream.alpaso.infrastructure.adapters.out.persistence.mapper;

import com.conicream.alpaso.domain.model.StockProducto;
import com.conicream.alpaso.infrastructure.adapters.out.persistence.entity.StockEntity;

public final class StockMapper {

    private StockMapper() { }

    public static StockProducto aDominio(StockEntity e) {
        return StockProducto.reconstruir(e.getProductoId(), e.getUnidades(), e.getStockMinimo());
    }

    public static void copiarA(StockEntity e, StockProducto s) {
        e.setProductoId(s.getProductoId());
        e.setUnidades(s.unidadesDisponibles());
        e.setStockMinimo(s.getStockMinimo());
    }
}
