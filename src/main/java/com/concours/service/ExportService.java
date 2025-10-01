package com.concours.service;

import com.concours.dto.StatistiquesDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class ExportService {

    public ByteArrayInputStream exportToExcel(StatistiquesDTO stats) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Création de la feuille
            Sheet sheet = workbook.createSheet("Statistiques");

            // Style pour les en-têtes
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Style pour les données
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.RIGHT);

            // Statistiques générales
            int rowNum = 0;
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("Statistiques Générales");
            headerRow.getCell(0).setCellStyle(headerStyle);

            Row dataRow1 = sheet.createRow(rowNum++);
            dataRow1.createCell(0).setCellValue("Concours");
            dataRow1.createCell(1).setCellValue(stats.getNbConcours());
            dataRow1.getCell(1).setCellStyle(dataStyle);

            Row dataRow2 = sheet.createRow(rowNum++);
            dataRow2.createCell(0).setCellValue("Total candidatures");
            dataRow2.createCell(1).setCellValue(stats.getTotalCandidatures());
            dataRow2.getCell(1).setCellStyle(dataStyle);

            Row dataRow3 = sheet.createRow(rowNum++);
            dataRow3.createCell(0).setCellValue("Utilisateurs");
            dataRow3.createCell(1).setCellValue(stats.getNbUtilisateurs());
            dataRow3.getCell(1).setCellStyle(dataStyle);

            Row dataRow4 = sheet.createRow(rowNum++);
            dataRow4.createCell(0).setCellValue("Concours avec candidatures");
            dataRow4.createCell(1).setCellValue(stats.getCandidaturesParConcours().size());
            dataRow4.getCell(1).setCellStyle(dataStyle);

            // Ligne vide
            rowNum++;

            // Statistiques par statut
            Row statutHeader = sheet.createRow(rowNum++);
            statutHeader.createCell(0).setCellValue("Statistiques par Statut");
            statutHeader.getCell(0).setCellStyle(headerStyle);

            Row statutRow1 = sheet.createRow(rowNum++);
            statutRow1.createCell(0).setCellValue("Candidatures validées");
            statutRow1.createCell(1).setCellValue(stats.getCandidaturesValidees());
            statutRow1.getCell(1).setCellStyle(dataStyle);

            Row statutRow2 = sheet.createRow(rowNum++);
            statutRow2.createCell(0).setCellValue("Candidatures en attente");
            statutRow2.createCell(1).setCellValue(stats.getCandidaturesEnAttente());
            statutRow2.getCell(1).setCellStyle(dataStyle);

            Row statutRow3 = sheet.createRow(rowNum++);
            statutRow3.createCell(0).setCellValue("Candidatures rejetées");
            statutRow3.createCell(1).setCellValue(stats.getCandidaturesRejetees());
            statutRow3.getCell(1).setCellStyle(dataStyle);

            // Ligne vide
            rowNum++;

            // Candidatures par concours
            if (!stats.getCandidaturesParConcours().isEmpty()) {
                Row concoursHeader = sheet.createRow(rowNum++);
                concoursHeader.createCell(0).setCellValue("Candidatures par Concours");
                concoursHeader.getCell(0).setCellStyle(headerStyle);

                for (Map.Entry<String, Long> entry : stats.getCandidaturesParConcours().entrySet()) {
                    Row concoursRow = sheet.createRow(rowNum++);
                    concoursRow.createCell(0).setCellValue(entry.getKey());
                    concoursRow.createCell(1).setCellValue(entry.getValue());
                    concoursRow.getCell(1).setCellStyle(dataStyle);
                }

                // Ligne vide
                rowNum++;
            }

            // Candidatures par spécialité
            if (!stats.getCandidaturesParSpecialite().isEmpty()) {
                Row specialiteHeader = sheet.createRow(rowNum++);
                specialiteHeader.createCell(0).setCellValue("Candidatures par Spécialité");
                specialiteHeader.getCell(0).setCellStyle(headerStyle);

                for (Map.Entry<String, Long> entry : stats.getCandidaturesParSpecialite().entrySet()) {
                    Row specialiteRow = sheet.createRow(rowNum++);
                    specialiteRow.createCell(0).setCellValue(entry.getKey());
                    specialiteRow.createCell(1).setCellValue(entry.getValue());
                    specialiteRow.getCell(1).setCellStyle(dataStyle);
                }
            }

            // Ajuster la largeur des colonnes
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public ByteArrayInputStream exportToPdf(StatistiquesDTO stats) throws IOException {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            try {
                // Définir les marges et positions
                float margin = 50;
                float yPosition = page.getMediaBox().getHeight() - margin;
                float width = page.getMediaBox().getWidth() - 2 * margin;

                // Titre
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                String title = "Rapport des Statistiques";
                float titleWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(title) / 1000 * 18;
                contentStream.beginText();
                contentStream.newLineAtOffset(margin + (width - titleWidth) / 2, yPosition);
                contentStream.showText(title);
                contentStream.endText();
                yPosition -= 30;

                // Date de génération
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                String date = "Généré le: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(date);
                contentStream.endText();
                yPosition -= 30;

                // Statistiques générales
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Statistiques Générales");
                contentStream.endText();
                yPosition -= 25;

                // Tableau des statistiques générales
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                addPdfTableRow(contentStream, margin, yPosition, width, "Concours", String.valueOf(stats.getNbConcours()));
                yPosition -= 20;
                addPdfTableRow(contentStream, margin, yPosition, width, "Total candidatures", String.valueOf(stats.getTotalCandidatures()));
                yPosition -= 20;
                addPdfTableRow(contentStream, margin, yPosition, width, "Utilisateurs", String.valueOf(stats.getNbUtilisateurs()));
                yPosition -= 20;
                addPdfTableRow(contentStream, margin, yPosition, width, "Concours avec candidatures", String.valueOf(stats.getCandidaturesParConcours().size()));
                yPosition -= 30;

                // Statistiques par statut
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Statistiques par Statut");
                contentStream.endText();
                yPosition -= 25;

                // Tableau des statistiques par statut
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                addPdfTableRow(contentStream, margin, yPosition, width, "Candidatures validées", String.valueOf(stats.getCandidaturesValidees()));
                yPosition -= 20;
                addPdfTableRow(contentStream, margin, yPosition, width, "Candidatures en attente", String.valueOf(stats.getCandidaturesEnAttente()));
                yPosition -= 20;
                addPdfTableRow(contentStream, margin, yPosition, width, "Candidatures rejetées", String.valueOf(stats.getCandidaturesRejetees()));
                yPosition -= 30;

                // Candidatures par concours
                if (!stats.getCandidaturesParConcours().isEmpty()) {
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Candidatures par Concours");
                    contentStream.endText();
                    yPosition -= 25;

                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    for (Map.Entry<String, Long> entry : stats.getCandidaturesParConcours().entrySet()) {
                        if (yPosition < 100) {
                            // Fermer le contentStream actuel et créer une nouvelle page
                            contentStream.close();
                            PDPage newPage = new PDPage(PDRectangle.A4);
                            document.addPage(newPage);
                            contentStream = new PDPageContentStream(document, newPage);
                            yPosition = page.getMediaBox().getHeight() - margin;
                        }

                        addPdfTableRow(contentStream, margin, yPosition, width, entry.getKey(), String.valueOf(entry.getValue()));
                        yPosition -= 20;
                    }
                    yPosition -= 10;
                }

                // Candidatures par spécialité
                if (!stats.getCandidaturesParSpecialite().isEmpty() && yPosition > 100) {
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Candidatures par Spécialité");
                    contentStream.endText();
                    yPosition -= 25;

                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    for (Map.Entry<String, Long> entry : stats.getCandidaturesParSpecialite().entrySet()) {
                        if (yPosition < 100) {
                            // Fermer le contentStream actuel et créer une nouvelle page
                            contentStream.close();
                            PDPage newPage = new PDPage(PDRectangle.A4);
                            document.addPage(newPage);
                            contentStream = new PDPageContentStream(document, newPage);
                            yPosition = page.getMediaBox().getHeight() - margin;
                        }

                        addPdfTableRow(contentStream, margin, yPosition, width, entry.getKey(), String.valueOf(entry.getValue()));
                        yPosition -= 20;
                    }
                }
            } finally {
                // Fermer le contentStream dans le bloc finally
                if (contentStream != null) {
                    contentStream.close();
                }
            }

            document.save(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private void addPdfTableRow(PDPageContentStream contentStream, float margin, float yPosition, float width, String label, String value) throws IOException {
        // Label
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText(label);
        contentStream.endText();

        // Valeur (alignée à droite)
        float valueWidth = PDType1Font.HELVETICA.getStringWidth(value) / 1000 * 12;
        contentStream.beginText();
        contentStream.newLineAtOffset(margin + width - valueWidth, yPosition);
        contentStream.showText(value);
        contentStream.endText();

        // Ligne de séparation
        contentStream.moveTo(margin, yPosition - 5);
        contentStream.lineTo(margin + width, yPosition - 5);
        contentStream.stroke();
    }
}