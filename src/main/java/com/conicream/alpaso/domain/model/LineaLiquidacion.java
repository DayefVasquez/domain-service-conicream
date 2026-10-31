package com.conicream.alpaso.domain.model;

import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import com.conicream.alpaso.domain.vo.Money;

/** Un producto liquidado: lo despachado, lo que volvió y el valor de lo vendido. */
public record LineaLiquidacion(long productoId, String nombreProducto, int despachadas, int sobrantes, Money precioUnitario) {

    public LineaLiquidacion {
        if (despachadas <= 0) {
            throw new ValorInvalidoException("Las unidades despachadas deben ser mayores que cero");
        }
        if (sobrantes < 0 || sobrantes > despachadas) {
            throw new ValorInvalidoException("Los sobrantes de '" + nombreProducto + "' deben estar entre 0 y lo despachado ("
                    + despachadas + ")");
        }
        if (precioUnitario == null) {
            throw new ValorInvalidoException("El precio unitario es obligatorio");
        }
    }

    public int vendidas() {
        return despachadas - sobrantes;
    }

    public Money valor() {
        return precioUnitario.multiplicar(vendidas());
    }
}
