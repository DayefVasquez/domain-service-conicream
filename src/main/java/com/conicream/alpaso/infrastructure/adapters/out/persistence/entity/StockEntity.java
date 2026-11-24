package com.conicream.alpaso.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;

/** Stock físico directo en el congelador. Comparte la identidad con su producto (1 a 1). */
@Entity
@Table(name = "stock_producto")
public class StockEntity {

    @Id
    @Column(name = "producto_id")
    private Long productoId;

    @Column(nullable = false)
    private int unidades;

    @Column(name = "stock_minimo", nullable = false)
    private int stockMinimo;

    @Version
    private Long version;

    public StockEntity() { }

    public static StockEntity nueva() { return new StockEntity(); }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public int getUnidades() { return unidades; }
    public void setUnidades(int unidades) { this.unidades = unidades; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public Long getVersion() { return version; }
}
