package com.conicream.alpaso.application.port.out;

import com.conicream.alpaso.domain.model.ReporteDiario;

/** La aplicación pide «un archivo con este reporte»; qué librería lo escribe (Apache POI) es cosa de la infraestructura. */
public interface ReporteExportPort {
    byte[] exportar(ReporteDiario reporte);
}
