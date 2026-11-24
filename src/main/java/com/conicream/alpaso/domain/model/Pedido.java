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

/**
 * Agregado Raíz: Comanda diaria de pedidos de un comerciante.
 * Ciclo de vida: BORRADOR (abierto) → CONFIRMADO (en calle) → LIQUIDADO (cerrado).
 * Soporta recargas acumulativas en ruta bajo el patrón de cuenta abierta.
 */
public class Pedido {

    private final Long id;
    private final String comercianteId;
    private final LocalDate fecha;
    private EstadoPedido estado;
    private List<DetallePedido> detalles;

    private Pedido(Long id, String comercianteId, LocalDate fecha, EstadoPedido estado, List<DetallePedido> detalles) {
        if (comercianteId == null || comercianteId.isBlank()) {
            throw new ValorInvalidoException("El comerciante es obligatorio");
        }
        if (fecha == null) {
            throw new ValorInvalidoException("La fecha del pedido es obligatoria");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new ValorInvalidoException("El pedido debe tener al menos un producto");
        }
        this.id = id;
        this.comercianteId = comercianteId.trim();
        this.fecha = fecha;
        this.estado = estado;
        this.detalles = fusionar(detalles);
    }

    /**
     * Fusión de líneas: Unifica productos repetidos sumando las cantidades
     * y garantizando que el precio unitario congelado sea idéntico.
     */
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
                throw new ValorInvalidoException("La cantidad acumulada del producto " + d.productoId() + " es demasiado grande");
            }
        }
        return new ArrayList<>(porProducto.values());
    }

    // --- Factory Methods ---

    /** Crea un pedido nuevo en estado BORRADOR (Salida matutina). */
    public static Pedido crear(String comercianteId, LocalDate fecha, List<DetallePedido> detalles) {
        return new Pedido(null, comercianteId, fecha, EstadoPedido.BORRADOR, detalles);
    }

    /** Reconstrucción desde el adaptador de persistencia. */
    public static Pedido reconstruir(long id, String comercianteId, LocalDate fecha, EstadoPedido estado,
                                     List<DetallePedido> detalles) {
        return new Pedido(id, comercianteId, fecha, estado, detalles);
    }

    // --- Métodos de Negocio (DDD) ---

    /**
     * Recarga en ruta (HU-10):
     * Permite al comerciante en calle solicitar más helados durante el día.
     * Fusiona las nuevas unidades con la comanda abierta existente manteniendo una única cuenta.
     */
    public void recargar(List<DetallePedido> recargas) {
        asegurarEstado(EstadoPedido.CONFIRMADO);
        if (recargas == null || recargas.isEmpty()) {
            throw new ValorInvalidoException("La recarga debe contener al menos un producto");
        }
        List<DetallePedido> acumuladas = new ArrayList<>(this.detalles);
        acumuladas.addAll(recargas);
        this.detalles = fusionar(acumuladas);
    }

    /** Confirmación de entrega matutina y salida a ruta física. */
    public void confirmar() {
        asegurarEstado(EstadoPedido.BORRADOR);
        this.estado = EstadoPedido.CONFIRMADO;
    }

    /** Cierre definitivo tras rendición de cuentas en caja al final de la jornada. */
    public void marcarLiquidado() {
        asegurarEstado(EstadoPedido.CONFIRMADO);
        this.estado = EstadoPedido.LIQUIDADO;
    }

    public void asegurarEstado(EstadoPedido esperado) {
        if (this.estado != esperado) {
            throw new PedidoEstadoInvalidoException(id, this.estado, esperado);
        }
    }

    public boolean perteneceA(String otroComercianteId) {
        return this.comercianteId.equals(otroComercianteId);
    }

    /** Total estimado del pedido (suma de cantidades despachadas por precio unitario). */
    public Money total() {
        return detalles.stream().map(DetallePedido::subtotal).reduce(Money.CERO, Money::sumar);
    }

    // --- Getters limpios ---
    public Long getId() { return id; }
    public String getComercianteId() { return comercianteId; }
    public LocalDate getFecha() { return fecha; }
    public EstadoPedido getEstado() { return estado; }
    public List<DetallePedido> getDetalles() { return Collections.unmodifiableList(detalles); }
}
