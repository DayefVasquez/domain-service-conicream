package com.conicream.alpaso.infrastructure.adapters.out.persistence.mapper;

import com.conicream.alpaso.domain.model.DetallePedido;
import com.conicream.alpaso.domain.model.EstadoPedido;
import com.conicream.alpaso.domain.model.Pedido;
import com.conicream.alpaso.domain.vo.Money;
import com.conicream.alpaso.infrastructure.adapters.out.persistence.entity.DetallePedidoEmbeddable;
import com.conicream.alpaso.infrastructure.adapters.out.persistence.entity.PedidoEntity;

public final class PedidoMapper {

    private PedidoMapper() { }

    public static Pedido aDominio(PedidoEntity e) {
        return Pedido.reconstruir(e.getId(), e.getComercianteId(), e.getFecha(), EstadoPedido.valueOf(e.getEstado()),
                e.getDetalles().stream()
                        .map(d -> new DetallePedido(d.getProductoId(), d.getNombreProducto(), d.getCantidad(), Money.de(d.getPrecioUnitario())))
                        .toList());
    }

    public static void copiarA(PedidoEntity e, Pedido p) {
        e.setComercianteId(p.getComercianteId());
        e.setFecha(p.getFecha());
        e.setEstado(p.getEstado().name());
        e.getDetalles().clear();
        p.getDetalles().forEach(d -> e.getDetalles().add(
                new DetallePedidoEmbeddable(d.productoId(), d.nombreProducto(), d.cantidad(), d.precioUnitario().valor())));
    }
}
