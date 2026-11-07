package com.conicream.alpaso.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "producto", uniqueConstraints = @UniqueConstraint(name = "uk_producto_clave_activa", columnNames = "clave_activa"))
public class ProductoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String marca;

    /**
     * Solo tiene valor mientras el producto está ACTIVO ("nombre|marca" en minúsculas). Como UNIQUE ignora los NULL,
     * la base de datos garantiza "no hay dos productos activos con el mismo nombre y marca" y deja que los retirados
     * repitan. Es la última defensa si dos altas llegan al mismo tiempo.
     */
    @Column(name = "clave_activa", length = 210)
    private String claveActiva;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal precio;

    @Column(name = "unidades_por_caja", nullable = false)
    private int unidadesPorCaja;

    @Column(nullable = false)
    private boolean activo;

    @Version
    private Long version;

    protected ProductoEntity() { }

    public static ProductoEntity nueva() { return new ProductoEntity(); }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getClaveActiva() { return claveActiva; }
    public void setClaveActiva(String claveActiva) { this.claveActiva = claveActiva; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public int getUnidadesPorCaja() { return unidadesPorCaja; }
    public void setUnidadesPorCaja(int unidadesPorCaja) { this.unidadesPorCaja = unidadesPorCaja; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
