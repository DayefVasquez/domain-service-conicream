package com.conicream.alpaso.infrastructure.adapters.in.rest.dto;

import com.conicream.alpaso.application.port.in.result.ProductoConStock;
import com.conicream.alpaso.domain.model.Producto;
import com.conicream.alpaso.domain.model.StockProducto;
import java.math.BigDecimal;

public record ProductoResponse(
        long id,
        String codigo,
        String nombre,
        String marca,
        String nombreCompleto,
        BigDecimal precio,
        boolean activo,
        int stockMinimo,
        int unidadesDisponibles
) {
    public static ProductoResponse de(ProductoConStock ps) {
        Producto p = ps.producto();
        StockProducto s = ps.stock();
        return new ProductoResponse(
                p.getId(),
                p.getCodigo(),
                p.getNombre(),
                p.getMarca(),
                p.nombreCompleto(),
                p.getPrecio().valor(),
                p.isActivo(),
                s.getStockMinimo(),
                s.unidadesDisponibles()
        );
    }
}
