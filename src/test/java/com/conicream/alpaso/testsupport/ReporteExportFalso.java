package com.conicream.alpaso.testsupport;

import com.conicream.alpaso.application.port.out.ReporteExportPort;
import com.conicream.alpaso.domain.model.ReporteDiario;
import java.nio.charset.StandardCharsets;

/** Doble del exportador: en vez de Excel devuelve texto, y recuerda qué reporte recibió. */
public class ReporteExportFalso implements ReporteExportPort {

    public ReporteDiario recibido;

    @Override
    public byte[] exportar(ReporteDiario reporte) {
        this.recibido = reporte;
        return ("REPORTE " + reporte.fecha()).getBytes(StandardCharsets.UTF_8);
    }
}
