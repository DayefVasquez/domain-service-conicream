package com.conicream.alpaso.domain.service;

import com.conicream.alpaso.domain.exception.StockInsuficienteException;
import com.conicream.alpaso.domain.exception.StockInsuficienteException.Faltante;
import com.conicream.alpaso.domain.model.DetallePedido;
import com.conicream.alpaso.domain.model.StockProducto;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Comprueba, sin modificar nada, que el stock alcance para todas las líneas; si no, informa TODOS los faltantes. */
public class ValidadorStock {

    public void validar(List<DetallePedido> lineas, Map<Long, StockProducto> stocks) {
        List<Faltante> faltantes = new ArrayList<>();
        for (DetallePedido d : lineas) {
            StockProducto stock = stocks.get(d.productoId());
            int disponible = stock == null ? 0 : stock.unidadesDisponibles();
            if (disponible < d.cantidad()) {
                faltantes.add(new Faltante(d.productoId(), d.nombreProducto(), disponible, d.cantidad()));
            }
        }
        if (!faltantes.isEmpty()) {
            throw new StockInsuficienteException(faltantes);
        }
    }
}
