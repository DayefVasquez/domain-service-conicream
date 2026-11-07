package com.conicream.alpaso.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido", indexes = @Index(name = "ix_pedido_fecha", columnList = "fecha"))
public class PedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comerciante_id", nullable = false, length = 40)
    private String comercianteId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 12)
    private String estado;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pedido_detalle", joinColumns = @JoinColumn(name = "pedido_id"))
    @OrderColumn(name = "orden")
    private List<DetallePedidoEmbeddable> detalles = new ArrayList<>();

    @Version
    private Long version;

    protected PedidoEntity() { }

    public static PedidoEntity nueva() { return new PedidoEntity(); }

    public Long getId() { return id; }
    public String getComercianteId() { return comercianteId; }
    public void setComercianteId(String comercianteId) { this.comercianteId = comercianteId; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public List<DetallePedidoEmbeddable> getDetalles() { return detalles; }
}
