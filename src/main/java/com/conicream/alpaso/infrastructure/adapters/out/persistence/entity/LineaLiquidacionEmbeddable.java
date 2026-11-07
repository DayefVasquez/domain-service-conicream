package com.conicream.alpaso.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class LineaLiquidacionEmbeddable {

    @Column(name = "producto_id", nullable = false)
    private long productoId;

    @Column(name = "nombre_producto", nullable = false, length = 210)
    private String nombreProducto;

    @Column(nullable = false)
    private int despachadas;

    @Column(nullable = false)
    private int sobrantes;

    @Column(name = "precio_unitario", nullable = false, precision = 14, scale = 2)
    private BigDecimal precioUnitario;

    protected LineaLiquidacionEmbeddable() { }

    public LineaLiquidacionEmbeddable(long productoId, String nombreProducto, int despachadas, int sobrantes, BigDecimal precioUnitario) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.despachadas = despachadas;
        this.sobrantes = sobrantes;
        this.precioUnitario = precioUnitario;
    }

    public long getProductoId() { return productoId; }
    public String getNombreProducto() { return nombreProducto; }
    public int getDespachadas() { return despachadas; }
    public int getSobrantes() { return sobrantes; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
}
