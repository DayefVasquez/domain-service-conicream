package com.conicream.alpaso.application.port.out;

import com.conicream.alpaso.domain.model.Liquidacion;

/** Puerto de salida: exportación del comprobante de liquidación en PDF para el comerciante. */
public interface ComprobantePdfPort {
    byte[] generar(Liquidacion liquidacion);
}
