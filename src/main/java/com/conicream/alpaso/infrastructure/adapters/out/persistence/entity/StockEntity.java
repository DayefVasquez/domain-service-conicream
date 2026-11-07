package com.conicream.alpaso.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;

/** El stock comparte identidad con su producto (1 a 1). @Version evita que dos pedidos descuenten a la vez sobre la misma lectura. */
@Entity
@Table(name = "stock_producto")
public class StockEntity {

    @Id
    @Column(name = "producto_id")
    private Long productoId;

    @Column(name = "unidades_por_caja", nullable = false)
    private int unidadesPorCaja;

    @Column(nullable = false)
    private int unidades;

    @Version
    private Long version;

    protected StockEntity() { }

    public static StockEntity nueva() { return new StockEntity(); }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public int getUnidadesPorCaja() { return unidadesPorCaja; }
    public void setUnidadesPorCaja(int unidadesPorCaja) { this.unidadesPorCaja = unidadesPorCaja; }
    public int getUnidades() { return unidades; }
    public void setUnidades(int unidades) { this.unidades = unidades; }
}
