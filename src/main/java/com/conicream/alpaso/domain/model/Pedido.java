package com.conicream.alpaso.domain.model;

import com.conicream.alpaso.domain.exception.PedidoEstadoInvalidoException;
import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import com.conicream.alpaso.domain.vo.Money;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Pedido de un comerciante. Ciclo: BORRADOR → CONFIRMADO → LIQUIDADO. Sus líneas no existen sin él (composición). */
public class Pedido {

    private final Long id;
    private final String comercianteId;
    private final LocalDate fecha;
    private EstadoPedido estado;
    private final List<DetallePedido> detalles;

    private Pedido(Long id, String comercianteId, LocalDate fecha, EstadoPedido estado, List<DetallePedido> detalles) {
        if (comercianteId == null || comercianteId.isBlank()) {
            throw new ValorInvalidoException("El comerciante es obligatorio");
        }
        if (fecha == null) {
            throw new ValorInvalidoException("La fecha del pedido es obligatoria");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new ValorInvalidoException("El pedido debe tener al menos una línea");
        }
        this.id = id;
        this.comercianteId = comercianteId.trim();
        this.fecha = fecha;
        this.estado = estado;
        this.detalles = fusionar(detalles);
    }

    /** Un producto repetido en varias líneas se suma en una sola, antes de validar nada. */
    private static List<DetallePedido> fusionar(List<DetallePedido> lineas) {
        Map<Long, DetallePedido> porProducto = new LinkedHashMap<>();
        for (DetallePedido d : lineas) {
            DetallePedido previa = porProducto.get(d.productoId());
            if (previa == null) {
                porProducto.put(d.productoId(), d);
                continue;
            }
            if (!previa.precioUnitario().equals(d.precioUnitario())) {
                throw new ValorInvalidoException("El producto " + d.productoId() + " aparece con dos precios distintos");
            }
            try {
                porProducto.put(d.productoId(), new DetallePedido(d.productoId(), previa.nombreProducto(),
                        Math.addExact(previa.cantidad(), d.cantidad()), previa.precioUnitario()));
            } catch (ArithmeticException e) {
                throw new ValorInvalidoException("La cantidad del producto " + d.productoId() + " es demasiado grande");
            }
        }
        return new ArrayList<>(porProducto.values());
    }

    /** Factory Method (Semana 7): un pedido nuevo nace en BORRADOR y sin id. */
    public static Pedido crear(String comercianteId, LocalDate fecha, List<DetallePedido> detalles) {
        return new Pedido(null, comercianteId, fecha, EstadoPedido.BORRADOR, detalles);
    }

    public static Pedido reconstruir(long id, String comercianteId, LocalDate fecha, EstadoPedido estado,
                                     List<DetallePedido> detalles) {
        return new Pedido(id, comercianteId, fecha, estado, detalles);
    }

    public Money total() {
        return detalles.stream().map(DetallePedido::subtotal).reduce(Money.CERO, Money::sumar);
    }

    public void confirmar() {
        asegurarEstado(EstadoPedido.BORRADOR);
        estado = EstadoPedido.CONFIRMADO;
    }

    public void marcarLiquidado() {
        asegurarEstado(EstadoPedido.CONFIRMADO);
        estado = EstadoPedido.LIQUIDADO;
    }

    public void asegurarEstado(EstadoPedido esperado) {
        if (estado != esperado) {
            throw new PedidoEstadoInvalidoException(id, estado, esperado);
        }
    }

    public boolean perteneceA(String otroComercianteId) {
        return comercianteId.equals(otroComercianteId);
    }

    public Long getId() { return id; }
    public String getComercianteId() { return comercianteId; }
    public LocalDate getFecha() { return fecha; }
    public EstadoPedido getEstado() { return estado; }
    public List<DetallePedido> getDetalles() { return Collections.unmodifiableList(detalles); }
}
