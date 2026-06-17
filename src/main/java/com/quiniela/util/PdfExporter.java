package com.quiniela.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import javax.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.io.IOException;
import java.util.List;

public class PdfExporter {

    private Document document;
    private String titulo;

    public PdfExporter(String titulo) {
        this.titulo = titulo;
        this.document = new Document(PageSize.A4.rotate()); // horizontal para tablas anchas
    }

    public void export(HttpServletResponse response, List<String[]> filas,
                       String[] headers, String subtitulo) throws IOException, DocumentException {

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + titulo.replace(" ", "_") + ".pdf");

        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // ── Fuentes ──
        Font fuenteTitulo  = new Font(Font.HELVETICA, 16, Font.BOLD, Color.WHITE);
        Font fuenteSub     = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(100, 100, 100));
        Font fuenteHeader  = new Font(Font.HELVETICA, 8,  Font.BOLD,   Color.WHITE);
        Font fuenteDato    = new Font(Font.HELVETICA, 8,  Font.NORMAL, Color.BLACK);

        // ── Título ──
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        PdfPCell titleCell = new PdfPCell(new Phrase(titulo, fuenteTitulo));
        titleCell.setBackgroundColor(new Color(33, 37, 41));
        titleCell.setPadding(14);
        titleCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(titleCell);
        document.add(headerTable);

        // ── Subtítulo ──
        if (subtitulo != null && !subtitulo.isEmpty()) {
            Paragraph sub = new Paragraph(subtitulo, fuenteSub);
            sub.setSpacingBefore(8);
            sub.setSpacingAfter(14);
            document.add(sub);
        }

        // ── Tabla de datos ──
        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);

        // Encabezados
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fuenteHeader));
            cell.setBackgroundColor(new Color(52, 58, 64));
            cell.setPadding(7);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorderColor(new Color(80, 80, 80));
            table.addCell(cell);
        }

        // Filas
        boolean par = false;
        for (String[] fila : filas) {
            Color bgFila = par ? new Color(248, 249, 250) : Color.WHITE;
            for (String dato : fila) {
                PdfPCell cell = new PdfPCell(new Phrase(dato != null ? dato : "-", fuenteDato));
                cell.setPadding(6);
                cell.setBackgroundColor(bgFila);
                cell.setBorderColor(new Color(220, 220, 220));
                table.addCell(cell);
            }
            par = !par;
        }

        document.add(table);

        // ── Pie de página ──
        Paragraph footer = new Paragraph(
                "Generado por ICHI Quiniela Mundial 2026",
                new Font(Font.HELVETICA, 7, Font.ITALIC, new Color(150, 150, 150)));
        footer.setSpacingBefore(12);
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);

        document.close();
    }
}