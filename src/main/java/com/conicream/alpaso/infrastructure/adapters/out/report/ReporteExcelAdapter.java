package com.conicream.alpaso.infrastructure.adapters.out.report;

import com.conicream.alpaso.application.port.out.ReporteExportPort;
import com.conicream.alpaso.domain.model.DetallePedido;
import com.conicream.alpaso.domain.model.LineaLiquidacion;
import com.conicream.alpaso.domain.model.ReporteDiario;
import com.conicream.alpaso.domain.vo.Money;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida: escribe el reporte diario como .xlsx con Apache POI (la tecnología declarada por el grupo).
 * La aplicación solo conoce el puerto {@link ReporteExportPort}; si mañana se pidiera PDF o CSV, se agrega otro adaptador.
 *
 * <p>Una fila por producto de cada pedido. Los pedidos aún sin liquidar se marcan "Pendiente" y no suman al total liquidado.
 */
@Component
public class ReporteExcelAdapter implements ReporteExportPort {

    private static final String[] ENCABEZADOS = {"Pedido", "Comerciante", "Estado", "Producto", "Despachadas",
            "Sobrantes", "Vendidas", "Precio unitario", "Valor liquidado"};
    private static final String PENDIENTE = "Pendiente";

    @Override
    public byte[] exportar(ReporteDiario reporte) {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Reporte diario");
            Estilos estilos = new Estilos(libro);

            Row titulo = hoja.createRow(0);
            celda(titulo, 0, "Coni Cream Al-Paso - Reporte diario", estilos.titulo);
            Row fecha = hoja.createRow(1);
            celda(fecha, 0, "Fecha: " + reporte.fecha(), null);

            Row encabezado = hoja.createRow(3);
            for (int i = 0; i < ENCABEZADOS.length; i++) {
                celda(encabezado, i, ENCABEZADOS[i], estilos.encabezado);
            }

            int fila = 4;
            for (ReporteDiario.Entrada entrada : reporte.entradas()) {
                Map<Long, LineaLiquidacion> liquidadas = new HashMap<>();
                if (entrada.estaLiquidado()) {
                    entrada.liquidacion().getLineas().forEach(l -> liquidadas.put(l.productoId(), l));
                }
                for (DetallePedido d : entrada.pedido().getDetalles()) {
                    Row r = hoja.createRow(fila++);
                    LineaLiquidacion l = liquidadas.get(d.productoId());
                    r.createCell(0).setCellValue(entrada.pedido().getId());
                    r.createCell(1).setCellValue(entrada.pedido().getComercianteId());
                    r.createCell(2).setCellValue(entrada.pedido().getEstado().name());
                    r.createCell(3).setCellValue(d.nombreProducto());
                    r.createCell(4).setCellValue(d.cantidad());
                    if (l == null) {
                        celda(r, 5, PENDIENTE, estilos.pendiente);
                        celda(r, 6, PENDIENTE, estilos.pendiente);
                        dinero(r, 7, d.precioUnitario(), estilos.dinero);
                        celda(r, 8, PENDIENTE, estilos.pendiente);
                    } else {
                        r.createCell(5).setCellValue(l.sobrantes());
                        r.createCell(6).setCellValue(l.vendidas());
                        dinero(r, 7, l.precioUnitario(), estilos.dinero);
                        dinero(r, 8, l.valor(), estilos.dinero);
                    }
                }
            }

            fila++;
            Row t1 = hoja.createRow(fila++);
            celda(t1, 0, "Pedidos del día", estilos.negrita);
            t1.createCell(1).setCellValue(reporte.totalPedidos());
            Row t2 = hoja.createRow(fila++);
            celda(t2, 0, "Pedidos liquidados", estilos.negrita);
            t2.createCell(1).setCellValue(reporte.pedidosLiquidados());
            Row t3 = hoja.createRow(fila);
            celda(t3, 0, "Total liquidado", estilos.negrita);
            dinero(t3, 1, reporte.totalLiquidado(), estilos.dinero);

            for (int i = 0; i < ENCABEZADOS.length; i++) {
                hoja.autoSizeColumn(i);
            }
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el archivo Excel", e);
        }
    }

    private static void celda(Row fila, int columna, String texto, CellStyle estilo) {
        Cell c = fila.createCell(columna);
        c.setCellValue(texto);
        if (estilo != null) {
            c.setCellStyle(estilo);
        }
    }

    /** El dinero va como NÚMERO con formato de pesos, no como texto: así Excel puede sumarlo. */
    private static void dinero(Row fila, int columna, Money valor, CellStyle estilo) {
        Cell c = fila.createCell(columna);
        c.setCellValue(valor.valor().doubleValue());
        c.setCellStyle(estilo);
    }

    private static final class Estilos {
        final CellStyle titulo;
        final CellStyle encabezado;
        final CellStyle negrita;
        final CellStyle dinero;
        final CellStyle pendiente;

        Estilos(Workbook libro) {
            Font fuenteNegrita = libro.createFont();
            fuenteNegrita.setBold(true);
            Font fuenteTitulo = libro.createFont();
            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 14);
            Font fuenteGris = libro.createFont();
            fuenteGris.setItalic(true);
            fuenteGris.setColor(IndexedColors.GREY_50_PERCENT.getIndex());

            titulo = libro.createCellStyle();
            titulo.setFont(fuenteTitulo);
            encabezado = libro.createCellStyle();
            encabezado.setFont(fuenteNegrita);
            encabezado.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            negrita = libro.createCellStyle();
            negrita.setFont(fuenteNegrita);
            dinero = libro.createCellStyle();
            dinero.setDataFormat(libro.createDataFormat().getFormat("$#,##0"));
            pendiente = libro.createCellStyle();
            pendiente.setFont(fuenteGris);
        }
    }
}
