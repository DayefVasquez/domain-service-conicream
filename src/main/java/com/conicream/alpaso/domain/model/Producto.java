package com.conicream.alpaso.domain.model;

import com.conicream.alpaso.domain.exception.ProductoInactivoException;
import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import com.conicream.alpaso.domain.vo.Money;

/**
 * Entidad de Dominio: Helado o producto de la distribución Coni-Cream.
 * Utiliza borrado lógico (retirar/reactivar) para proteger la trazabilidad contable.
 * Cero dependencias de infraestructura y frameworks.
 */
public class Producto {

    private final Long id;
    private String codigo;
    private String nombre;
    private String marca;
    private Money precio;
    private boolean activo;

    private Producto(Long id, String codigo, String nombre, String marca, Money precio, boolean activo) {
        this.id = id;
        this.codigo = validarTexto(codigo, "código de fábrica", 50);
        this.nombre = validarTexto(nombre, "nombre", 120);
        this.marca = validarTexto(marca, "marca", 80);
        this.precio = validarPrecio(precio);
        this.activo = activo;
    }

    /** Factory method para registrar un producto nuevo de catálogo (nace activo). */
    public static Producto registrar(String codigo, String nombre, String marca, Money precio) {
        return new Producto(null, codigo, nombre, marca, precio, true);
    }

    /** Reconstrucción desde el adaptador de persistencia. */
    public static Producto reconstruir(long id, String codigo, String nombre, String marca, Money precio, boolean activo) {
        return new Producto(id, codigo, nombre, marca, precio, activo);
    }

    // --- Validaciones de Invariantes de Dominio ---

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
            throw new ValorInvalidoException("El precio base de liquidación debe ser mayor a cero");
        }
        return precio;
    }

    // --- Métodos de Negocio (DDD) ---

    public void asegurarActivo() {
        if (!activo) {
            throw new ProductoInactivoException(id);
        }
    }

    /** Edición de catálogo: Solo aplicable a productos en estado activo. */
    public void editar(String codigo, String nombre, String marca, Money precio) {
        asegurarActivo();
        this.codigo = validarTexto(codigo, "código de fábrica", 50);
        this.nombre = validarTexto(nombre, "nombre", 120);
        this.marca = validarTexto(marca, "marca", 80);
        this.precio = validarPrecio(precio);
    }

    /**
     * Borrado lógico: Desactiva el producto al finalizar su temporada (ej. Navidad, Halloween).
     * No borra el registro de la base de datos para no romper historiales de pedidos.
     */
    public void retirar() {
        asegurarActivo();
        this.activo = false;
    }

    /**
     * Reactivación de temporada: Permite que un producto retirado vuelva a venderse
     * cuando inicie un nuevo ciclo comercial.
     */
    public void reactivar() {
        if (this.activo) {
            throw new ValorInvalidoException("El producto '" + this.nombre + "' ya se encuentra activo");
        }
        this.activo = true;
    }

    /** Nombre compuesto para representación en pedidos y planillas oficiales. */
    public String nombreCompleto() {
        return nombre + " (" + marca + ")";
    }

    // --- Getters limpios ---
    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getMarca() { return marca; }
    public Money getPrecio() { return precio; }
    public boolean isActivo() { return activo; }
}
