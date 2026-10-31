package com.conicream.alpaso.application.port.in;

import com.conicream.alpaso.application.port.in.result.ArchivoReporte;
import java.time.LocalDate;

public interface ReporteUseCase {
    /** @param fecha día del reporte; null significa hoy */
    ArchivoReporte generarDiario(LocalDate fecha);
}
