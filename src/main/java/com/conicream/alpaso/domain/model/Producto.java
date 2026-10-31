package com.conicream.alpaso.domain.model;

import com.conicream.alpaso.domain.exception.ProductoInactivoException;
import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import com.conicream.alpaso.domain.vo.Money;

/**
 * Producto de la distribución. Se retira (baja lógica) en vez de borrarse, para no perder el historial de pedidos.
 * Las unidades por caja dependen de la marca y no cambian después de creado.
 */
public class Producto {

    private static final int MAX_UNIDADES_POR_CAJA = 10_000;

    private final Long id;
    private String nombre;
    private String marca;
    private Money precio;
    private final int unidadesPorCaja;
    private boolean activo;

    private Producto(Long id, String nombre, String marca, Money precio, int unidadesPorCaja, boolean activo) {
        if (unidadesPorCaja < 1 || unidadesPorCaja > MAX_UNIDADES_POR_CAJA) {
            throw new ValorInvalidoException("Las unidades por caja deben estar entre 1 y " + MAX_UNIDADES_POR_CAJA);
        }
        this.id = id;
        this.nombre = validarTexto(nombre, "nombre", 100);
        this.marca = validarTexto(marca, "marca", 60);
        this.precio = validarPrecio(precio);
        this.unidadesPorCaja = unidadesPorCaja;
        this.activo = activo;
    }

    /** Factory Method (Semana 7): un producto nuevo nace activo y sin id. */
    public static Producto registrar(String nombre, String marca, Money precio, int unidadesPorCaja) {
        return new Producto(null, nombre, marca, precio, unidadesPorCaja, true);
    }

    public static Producto reconstruir(long id, String nombre, String marca, Money precio, int unidadesPorCaja, boolean activo) {
        return new Producto(id, nombre, marca, precio, unidadesPorCaja, activo);
    }

    private static String validarTexto(String valor, String campo, int max) {
        if (valor == null || valor.isBlank()) {
            throw new ValorInvalidoException("El " + campo + " es obligatorio");
        }
        String limpio = valor.trim();
        if (limpio.length() > max) {
            throw new ValorInvalidoException("El " + campo + " no puede superar " + max + " caracteres");
        }
        return limpio;
    }

    private static Money validarPrecio(Money precio) {
        if (precio == null || !precio.esMayorQueCero()) {
            throw new ValorInvalidoException("El precio debe ser mayor que cero");
        }
        return precio;
    }

    public void asegurarActivo() {
        if (!activo) {
            throw new ProductoInactivoException(id);
        }
    }

    public void editar(String nombre, String marca, Money precio) {
        asegurarActivo();
        this.nombre = validarTexto(nombre, "nombre", 100);
        this.marca = validarTexto(marca, "marca", 60);
        this.precio = validarPrecio(precio);
    }

    public void retirar() {
        asegurarActivo();
        this.activo = false;
    }

    /** Nombre con el que aparece en pedidos y reportes. */
    public String nombreCompleto() {
        return nombre + " (" + marca + ")";
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getMarca() { return marca; }
    public Money getPrecio() { return precio; }
    public int getUnidadesPorCaja() { return unidadesPorCaja; }
    public boolean isActivo() { return activo; }
}
