package com.conicream.alpaso.infrastructure.adapters.in.rest;

import com.conicream.alpaso.application.port.in.ReporteUseCase;
import com.conicream.alpaso.application.port.in.result.ArchivoReporte;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** HU-12: descarga del reporte diario en Excel. Solo ADMIN. */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ReporteUseCase reportes;

    public ReporteController(ReporteUseCase reportes) {
        this.reportes = reportes;
    }

    @GetMapping("/diario")
    public ResponseEntity<byte[]> diario(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        ArchivoReporte archivo = reportes.generarDiario(fecha);
        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(archivo.nombre()).build().toString())
                .body(archivo.contenido());
    }
}
