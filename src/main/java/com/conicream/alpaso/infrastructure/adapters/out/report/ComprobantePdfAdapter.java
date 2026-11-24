package com.conicream.alpaso.infrastructure.adapters.out.report;

import com.conicream.alpaso.application.port.out.ComprobantePdfPort;
import com.conicream.alpaso.domain.model.LineaLiquidacion;
import com.conicream.alpaso.domain.model.Liquidacion;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ComprobantePdfAdapter implements ComprobantePdfPort {

    @Override
    public byte[] generar(Liquidacion liquidacion) {
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Document documento = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(documento, salida);
            documento.open();

            // Fuentes
            Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Font fSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            Font fTexto = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font fNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font fHeaderTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font fCelda = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            Font fTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(0, 102, 204));

            // Encabezado
            Paragraph pTitulo = new Paragraph("CONI-CREAM AL-PASO", fTitulo);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(pTitulo);

            Paragraph pSub = new Paragraph("Comprobante de Liquidación Diaria - Rendición de Cuentas", fSubtitulo);
            pSub.setAlignment(Element.ALIGN_CENTER);
            pSub.setSpacingAfter(15);
            documento.add(pSub);

            // Datos del Pedido
            PdfPTable tInfo = new PdfPTable(2);
            tInfo.setWidthPercentage(100);
            tInfo.setSpacingAfter(15);

            agregarCeldaInfo(tInfo, "N° Pedido Liquidado: " + liquidacion.getPedidoId(), fNegrita);
            agregarCeldaInfo(tInfo, "Comerciante: " + liquidacion.getComercianteId(), fNegrita);
            agregarCeldaInfo(tInfo, "Fecha de Emisión: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), fTexto);
            agregarCeldaInfo(tInfo, "Condición: Pago en Efectivo de Contado (Sin Impuestos)", fTexto);
            documento.add(tInfo);

            // Tabla de Productos
            PdfPTable tabla = new PdfPTable(6);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{3.5f, 1.2f, 1.2f, 1.2f, 1.5f, 1.8f});

            String[] headers = {"Producto", "Despachadas", "Devueltas", "Vendidas", "Precio Base", "Total"};
            for (String h : headers) {
                PdfPCell c = new PdfPCell(new Phrase(h, fHeaderTabla));
                c.setBackgroundColor(new Color(41, 128, 185));
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                c.setPadding(5);
                tabla.addCell(c);
            }

            for (LineaLiquidacion l : liquidacion.getLineas()) {
                tabla.addCell(celdaTexto(l.nombreProducto(), fCelda, Element.ALIGN_LEFT));
                tabla.addCell(celdaTexto(String.valueOf(l.despachadas()), fCelda, Element.ALIGN_CENTER));
                tabla.addCell(celdaTexto(String.valueOf(l.sobrantes()), fCelda, Element.ALIGN_CENTER));
                tabla.addCell(celdaTexto(String.valueOf(l.vendidas()), fCelda, Element.ALIGN_CENTER));
                tabla.addCell(celdaTexto("$" + l.precioUnitario().valor().toPlainString(), fCelda, Element.ALIGN_RIGHT));
                tabla.addCell(celdaTexto("$" + l.valor().valor().toPlainString(), fCelda, Element.ALIGN_RIGHT));
            }
            documento.add(tabla);

            // Total Factura
            Paragraph pTotal = new Paragraph("TOTAL FACTURA A PAGAR: $" + liquidacion.total().valor().toPlainString() + " COP", fTotal);
            pTotal.setAlignment(Element.ALIGN_RIGHT);
            pTotal.setSpacingBefore(15);
            documento.add(pTotal);

            Paragraph pAviso = new Paragraph("Mercancía devuelta reintegrada exitosamente al congelador físico.", fTexto);
            pAviso.setAlignment(Element.ALIGN_CENTER);
            pAviso.setSpacingBefore(20);
            documento.add(pAviso);

            documento.close();
            return salida.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el comprobante PDF con OpenPDF", e);
        }
    }

    private static void agregarCeldaInfo(PdfPTable t, String texto, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(texto, f));
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(2);
        t.addCell(c);
    }

    private static PdfPCell celdaTexto(String texto, Font f, int align) {
        PdfPCell c = new PdfPCell(new Phrase(texto, f));
        c.setHorizontalAlignment(align);
        c.setPadding(4);
        return c;
    }
}
