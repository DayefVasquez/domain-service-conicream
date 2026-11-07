package com.conicream.alpaso.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "liquidacion", uniqueConstraints = @UniqueConstraint(name = "uk_liquidacion_pedido", columnNames = "pedido_id"))
public class LiquidacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** UNIQUE: un pedido se liquida una sola vez, aunque dos peticiones lleguen a la vez. */
    @Column(name = "pedido_id", nullable = false)
    private long pedidoId;

    @Column(name = "comerciante_id", nullable = false, length = 40)
    private String comercianteId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "liquidacion_linea", joinColumns = @JoinColumn(name = "liquidacion_id"))
    @OrderColumn(name = "orden")
    private List<LineaLiquidacionEmbeddable> lineas = new ArrayList<>();

    @Version
    private Long version;

    protected LiquidacionEntity() { }

    public static LiquidacionEntity nueva() { return new LiquidacionEntity(); }

    public Long getId() { return id; }
    public long getPedidoId() { return pedidoId; }
    public void setPedidoId(long pedidoId) { this.pedidoId = pedidoId; }
    public String getComercianteId() { return comercianteId; }
    public void setComercianteId(String comercianteId) { this.comercianteId = comercianteId; }
    public List<LineaLiquidacionEmbeddable> getLineas() { return lineas; }
}
