package com.conicream.alpaso.domain.service;

import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import com.conicream.alpaso.domain.model.DetallePedido;
import com.conicream.alpaso.domain.model.LineaLiquidacion;
import com.conicream.alpaso.domain.model.Liquidacion;
import com.conicream.alpaso.domain.model.Pedido;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Se cobra lo vendido: (despachadas − sobrantes) × precio. Los sobrantes no vuelven al stock. */
public class LiquidacionPorVendidas implements ReglaLiquidacion {

    @Override
    public Liquidacion calcular(Pedido pedido, Map<Long, Integer> sobrantes) {
        Map<Long, Integer> devueltos = sobrantes == null ? Map.of() : sobrantes;
        for (Long productoId : devueltos.keySet()) {
            boolean estaEnElPedido = pedido.getDetalles().stream().anyMatch(d -> d.productoId() == productoId);
            if (!estaEnElPedido) {
                throw new ValorInvalidoException("El producto " + productoId + " no está en el pedido " + pedido.getId());
            }
        }
        List<LineaLiquidacion> lineas = new ArrayList<>();
        for (DetallePedido d : pedido.getDetalles()) {
            Integer sobrante = devueltos.get(d.productoId());
            if (devueltos.containsKey(d.productoId()) && sobrante == null) {
                throw new ValorInvalidoException("Falta la cantidad de sobrantes de '" + d.nombreProducto() + "'");
            }
            lineas.add(new LineaLiquidacion(d.productoId(), d.nombreProducto(), d.cantidad(),
                    sobrante == null ? 0 : sobrante, d.precioUnitario()));
        }
        return Liquidacion.nueva(pedido.getId(), pedido.getComercianteId(), lineas);
    }
}
