package com.conicream.alpaso.domain.model;

import com.conicream.alpaso.domain.exception.ValorInvalidoException;

/**
 * Existencias físicas de un producto en la heladería.
 * Se gestiona estrictamente en UNIDADES directas.
 * Protege la invariante de negocio: el stock físico nunca puede ser negativo.
 */
public class StockProducto {

    private final long productoId;
    private int unidades;
    private final int stockMinimo;

    private StockProducto(long productoId, int unidades, int stockMinimo) {
        if (unidades < 0) {
            throw new ValorInvalidoException("El stock de unidades no puede ser negativo");
        }
        if (stockMinimo < 0) {
            throw new ValorInvalidoException("El stock mínimo no puede ser negativo");
        }
        this.productoId = productoId;
        this.unidades = unidades;
        this.stockMinimo = stockMinimo;
    }

    /** Factory method para crear el inventario inicial de un producto nuevo. */
    public static StockProducto crear(long productoId, int unidadesIniciales, int stockMinimo) {
        return new StockProducto(productoId, unidadesIniciales, stockMinimo);
    }

    /** Reconstrucción desde la persistencia o base de datos. */
    public static StockProducto reconstruir(long productoId, int unidades, int stockMinimo) {
        return new StockProducto(productoId, unidades, stockMinimo);
    }

    // --- Métodos de Negocio (DDD) ---

    /** Reposición: Ingreso directo de unidades que llegan de la fábrica Crem Helado. */
    public void reponer(int unidadesAdicionales) {
        if (unidadesAdicionales <= 0) {
            throw new ValorInvalidoException("Las unidades a reponer deben ser mayores a cero");
        }
        try {
            this.unidades = Math.addExact(this.unidades, unidadesAdicionales);
        } catch (ArithmeticException e) {
            throw new ValorInvalidoException("El stock resultante supera el límite numérico admitido");
        }
    }

    /**
     * Reintegración: Regresa al congelador los helados físicos que sobraron en la liquidación diaria.
     * Resuelve el bug de pérdida de inventario.
     */
    public void reintegrar(int unidadesDevueltas) {
        if (unidadesDevueltas < 0) {
            throw new ValorInvalidoException("Las unidades devueltas no pueden ser negativas");
        }
        if (unidadesDevueltas > 0) {
            try {
                this.unidades = Math.addExact(this.unidades, unidadesDevueltas);
            } catch (ArithmeticException e) {
                throw new ValorInvalidoException("Error aritmético al reintegrar sobrantes al stock");
            }
        }
    }

    /** Despacho: Descuenta unidades para entregar al carrito del comerciante. */
    public void descontar(int unidadesADescontar) {
        if (unidadesADescontar <= 0) {
            throw new ValorInvalidoException("Las unidades a descontar deben ser mayores a cero");
        }
        if (unidadesADescontar > this.unidades) {
            throw new ValorInvalidoException("No hay stock suficiente: disponibles " + this.unidades
                    + ", solicitadas " + unidadesADescontar);
        }
        this.unidades -= unidadesADescontar;
    }

    /** Ajuste físico por conteo manual en bodega (diferencias de inventario). */
    public void ajustarFisico(int unidadesReales) {
        if (unidadesReales < 0) {
            throw new ValorInvalidoException("El conteo físico de stock no puede ser negativo");
        }
        this.unidades = unidadesReales;
    }

    /** Consulta de disponibilidad. */
    public boolean alcanzaPara(int solicitadas) {
        return solicitadas > 0 && solicitadas <= this.unidades;
    }

    /** Regla de Alerta de Existencias (HU-06). */
    public boolean estaBajoDeStock() {
        return this.unidades <= this.stockMinimo;
    }

    // --- Getters limpios ---
    public long getProductoId() { return productoId; }
    public int unidadesDisponibles() { return unidades; }
    public int getStockMinimo() { return stockMinimo; }
}
