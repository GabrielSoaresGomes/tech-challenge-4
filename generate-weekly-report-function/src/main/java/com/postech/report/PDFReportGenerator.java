package com.postech.report;

import com.postech.domain.Feedback;
import com.postech.dto.WeeklyReportDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import jakarta.enterprise.context.ApplicationScoped;

import java.awt.Color;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class PDFReportGenerator {

    public byte[] generateReport(WeeklyReportDTO report) {
        Document document = new Document();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);

            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 12);


            document.add(new Paragraph("Relatório Semanal de Feedbacks", titleFont));
            document.add(Chunk.NEWLINE);

            // ============================================================
            // 1. Lista de feedbacks
            // ============================================================
            document.add(new Paragraph("1. Lista de Feedbacks", headerFont));
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);

            addHeaderCell(table, "Descrição");
            addHeaderCell(table, "Urgência");
            addHeaderCell(table, "Data de Envio");

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (Feedback f : report.feedbackList()) {
                table.addCell(new PdfPCell(new Phrase(f.description, normalFont)));
                table.addCell(new PdfPCell(new Phrase(f.urgency ? "Urgente" : "Não urgente", normalFont)));
                table.addCell(new PdfPCell(new Phrase(f.sendDate.format(fmt), normalFont)));
            }

            document.add(table);
            document.add(Chunk.NEWLINE);

            // ============================================================
            // 2. Quantidade por dia
            // ============================================================
            document.add(new Paragraph("2. Quantidade de Avaliações por Dia", headerFont));
            document.add(Chunk.NEWLINE);

            PdfPTable t2 = new PdfPTable(2);
            t2.setWidthPercentage(100);

            addHeaderCell(t2, "Data");
            addHeaderCell(t2, "Quantidade");

            report.aggregations().countPerDay().forEach((day, count) -> {
                t2.addCell(new PdfPCell(new Phrase(day, normalFont)));
                t2.addCell(new PdfPCell(new Phrase(count.toString(), normalFont)));
            });

            document.add(t2);
            document.add(Chunk.NEWLINE);

            // ============================================================
            // 3. Quantidade por urgência
            // ============================================================
            document.add(new Paragraph("3. Quantidade de Avaliações por Urgência", headerFont));
            document.add(Chunk.NEWLINE);

            PdfPTable t3 = new PdfPTable(2);
            t3.setWidthPercentage(100);

            addHeaderCell(t3, "Urgência");
            addHeaderCell(t3, "Quantidade");

            report.aggregations().countPerUrgency().forEach((type, count) -> {
                t3.addCell(new PdfPCell(new Phrase(type, normalFont)));
                t3.addCell(new PdfPCell(new Phrase(count.toString(), normalFont)));
            });

            document.add(t3);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF", e);

        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.HELVETICA, 12, Font.BOLD)));
        cell.setBackgroundColor(new Color(230, 230, 230));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }
}
