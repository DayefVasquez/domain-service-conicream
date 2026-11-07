package com.conicream.alpaso.infrastructure.adapters.out.persistence.mapper;

import com.conicream.alpaso.domain.model.LineaLiquidacion;
import com.conicream.alpaso.domain.model.Liquidacion;
import com.conicream.alpaso.domain.vo.Money;
import com.conicream.alpaso.infrastructure.adapters.out.persistence.entity.LineaLiquidacionEmbeddable;
import com.conicream.alpaso.infrastructure.adapters.out.persistence.entity.LiquidacionEntity;

public final class LiquidacionMapper {

    private LiquidacionMapper() { }

    public static Liquidacion aDominio(LiquidacionEntity e) {
        return Liquidacion.reconstruir(e.getId(), e.getPedidoId(), e.getComercianteId(),
                e.getLineas().stream()
                        .map(l -> new LineaLiquidacion(l.getProductoId(), l.getNombreProducto(), l.getDespachadas(),
                                l.getSobrantes(), Money.de(l.getPrecioUnitario())))
                        .toList());
    }

    public static void copiarA(LiquidacionEntity e, Liquidacion l) {
        e.setPedidoId(l.getPedidoId());
        e.setComercianteId(l.getComercianteId());
        e.getLineas().clear();
        l.getLineas().forEach(x -> e.getLineas().add(new LineaLiquidacionEmbeddable(
                x.productoId(), x.nombreProducto(), x.despachadas(), x.sobrantes(), x.precioUnitario().valor())));
    }
}
