package com.conicream.alpaso.domain.model;

import com.conicream.alpaso.domain.exception.ValorInvalidoException;

/**
 * Existencias de un producto. Se guardan en UNIDADES; las cajas son solo una forma de verlas y de reponerlas.
 * La clase protege su regla: el stock nunca queda por debajo de cero.
 */
public class StockProducto {

    private static final int MAX_CAJAS = 100_000;

    private final long productoId;
    private final int unidadesPorCaja;
    private int unidades;

    private StockProducto(long productoId, int unidadesPorCaja, int unidades) {
        if (unidadesPorCaja < 1) {
            throw new ValorInvalidoException("Las unidades por caja deben ser al menos 1");
        }
        if (unidades < 0) {
            throw new ValorInvalidoException("El stock no puede ser negativo");
        }
        this.productoId = productoId;
        this.unidadesPorCaja = unidadesPorCaja;
        this.unidades = unidades;
    }

    /** Stock inicial de un producto recién creado, en cajas completas. */
    public static StockProducto crear(long productoId, int unidadesPorCaja, int cajasIniciales) {
        validarCajas(cajasIniciales, "Las cajas iniciales");
        return new StockProducto(productoId, unidadesPorCaja, cajasIniciales * unidadesPorCaja);
    }

    public static StockProducto reconstruir(long productoId, int unidadesPorCaja, int unidades) {
        return new StockProducto(productoId, unidadesPorCaja, unidades);
    }

    private static void validarCajas(int cajas, String que) {
        if (cajas < 0) {
            throw new ValorInvalidoException(que + " no pueden ser negativas");
        }
        if (cajas > MAX_CAJAS) {
            throw new ValorInvalidoException(que + " no pueden superar " + MAX_CAJAS);
        }
    }

    public void reponerCajas(int cajas) {
        if (cajas <= 0) {
            throw new ValorInvalidoException("Las cajas a reponer deben ser mayores que cero");
        }
        validarCajas(cajas, "Las cajas a reponer");
        try {
            unidades = Math.addExact(unidades, Math.multiplyExact(cajas, unidadesPorCaja));
        } catch (ArithmeticException e) {
            throw new ValorInvalidoException("La reposición supera el máximo de unidades que se pueden guardar");
        }
    }

    /** Conteo manual: fija el stock en un número de cajas completas más unidades sueltas. */
    public void corregir(int cajas, int unidadesSueltas) {
        validarCajas(cajas, "Las cajas");
        if (unidadesSueltas < 0 || unidadesSueltas >= unidadesPorCaja) {
            throw new ValorInvalidoException("Las unidades sueltas deben estar entre 0 y " + (unidadesPorCaja - 1));
        }
        this.unidades = cajas * unidadesPorCaja + unidadesSueltas;
    }

    public boolean alcanzaPara(int solicitadas) {
        return solicitadas <= unidades;
    }

    public void descontar(int despachadas) {
        if (despachadas <= 0) {
            throw new ValorInvalidoException("Las unidades a descontar deben ser mayores que cero");
        }
        if (despachadas > unidades) {
            throw new ValorInvalidoException("No hay unidades suficientes: disponibles " + unidades + ", pedidas " + despachadas);
        }
        unidades -= despachadas;
    }

    public int cajasCompletas() { return unidades / unidadesPorCaja; }
    public int unidadesSueltas() { return unidades % unidadesPorCaja; }
    public int unidadesDisponibles() { return unidades; }
    public long getProductoId() { return productoId; }
    public int getUnidadesPorCaja() { return unidadesPorCaja; }
}
