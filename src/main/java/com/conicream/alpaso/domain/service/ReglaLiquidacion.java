package com.conicream.alpaso.domain.service;

import com.conicream.alpaso.domain.model.Liquidacion;
import com.conicream.alpaso.domain.model.Pedido;
import java.util.Map;

/**
 * Patrón Strategy (OCP, Semana 5): cada forma de cobrar es una clase nueva; el caso de uso no se modifica.
 * Hoy existe {@link LiquidacionPorVendidas}; mañana podría haber descuentos por volumen.
 */
public interface ReglaLiquidacion {

    /** @param sobrantes unidades devueltas por producto; un producto ausente se toma como sin sobrante */
    Liquidacion calcular(Pedido pedido, Map<Long, Integer> sobrantes);
}
