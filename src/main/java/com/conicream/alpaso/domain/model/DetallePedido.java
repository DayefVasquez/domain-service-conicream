package com.conicream.alpaso.domain.model;

import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import com.conicream.alpaso.domain.vo.Money;

/** Línea de un pedido. Congela el nombre y el precio del momento: cambiar el producto después no altera el pedido. */
public record DetallePedido(long productoId, String nombreProducto, int cantidad, Money precioUnitario) {

    public DetallePedido {
        if (nombreProducto == null || nombreProducto.isBlank()) {
            throw new ValorInvalidoException("El nombre del producto es obligatorio");
        }
        if (cantidad <= 0) {
            throw new ValorInvalidoException("La cantidad de '" + nombreProducto + "' debe ser mayor que cero");
        }
        if (precioUnitario == null) {
            throw new ValorInvalidoException("El precio unitario es obligatorio");
        }
    }

    public Money subtotal() {
        return precioUnitario.multiplicar(cantidad);
    }
}
