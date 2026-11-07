package com.conicream.alpaso.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

/** Línea del pedido con el nombre y el precio CONGELADOS al momento de pedir. */
@Embeddable
public class DetallePedidoEmbeddable {

    @Column(name = "producto_id", nullable = false)
    private long productoId;

    @Column(name = "nombre_producto", nullable = false, length = 210)
    private String nombreProducto;

    @Column(nullable = false)
    private int cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 14, scale = 2)
    private BigDecimal precioUnitario;

    protected DetallePedidoEmbeddable() { }

    public DetallePedidoEmbeddable(long productoId, String nombreProducto, int cantidad, BigDecimal precioUnitario) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public long getProductoId() { return productoId; }
    public String getNombreProducto() { return nombreProducto; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
}
