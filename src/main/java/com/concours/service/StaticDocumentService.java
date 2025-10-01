package com.concours.service;

import com.concours.entity.TypeDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class StaticDocumentService {

    /**
     * Génère un contenu PDF statique basé sur le type de document et les informations du candidat
     */
    public byte[] generateStaticPDF(TypeDocument type, String candidatNom, String candidatPrenom, String candidatCin) {
        String content = generatePDFContent(type, candidatNom, candidatPrenom, candidatCin);
        return content.getBytes();
    }

    /**
     * Génère le contenu d'un PDF simple avec du texte
     */
    private String generatePDFContent(TypeDocument type, String nom, String prenom, String cin) {
        String documentTitle = getDocumentTitle(type);

        return String.format(
                "%%PDF-1.4\n" +
                        "1 0 obj\n" +
                        "<<\n" +
                        "/Type /Catalog\n" +
                        "/Pages 2 0 R\n" +
                        ">>\n" +
                        "endobj\n" +
                        "2 0 obj\n" +
                        "<<\n" +
                        "/Type /Pages\n" +
                        "/Kids [3 0 R]\n" +
                        "/Count 1\n" +
                        ">>\n" +
                        "endobj\n" +
                        "3 0 obj\n" +
                        "<<\n" +
                        "/Type /Page\n" +
                        "/Parent 2 0 R\n" +
                        "/MediaBox [0 0 612 792]\n" +
                        "/Contents 4 0 R\n" +
                        "/Resources <<\n" +
                        "/Font <<\n" +
                        "/F1 5 0 R\n" +
                        ">>\n" +
                        ">>\n" +
                        ">>\n" +
                        "endobj\n" +
                        "4 0 obj\n" +
                        "<<\n" +
                        "/Length 300\n" +
                        ">>\n" +
                        "stream\n" +
                        "BT\n" +
                        "/F1 16 Tf\n" +
                        "100 720 Td\n" +
                        "(%s) Tj\n" +
                        "0 -40 Td\n" +
                        "/F1 12 Tf\n" +
                        "(Candidat: %s %s) Tj\n" +
                        "0 -20 Td\n" +
                        "(CIN: %s) Tj\n" +
                        "0 -40 Td\n" +
                        "(Document genere automatiquement) Tj\n" +
                        "0 -20 Td\n" +
                        "(pour la demonstration du systeme) Tj\n" +
                        "0 -40 Td\n" +
                        "(Date: %s) Tj\n" +
                        "ET\n" +
                        "endstream\n" +
                        "endobj\n" +
                        "5 0 obj\n" +
                        "<<\n" +
                        "/Type /Font\n" +
                        "/Subtype /Type1\n" +
                        "/BaseFont /Helvetica\n" +
                        ">>\n" +
                        "endobj\n" +
                        "xref\n" +
                        "0 6\n" +
                        "0000000000 65535 f \n" +
                        "0000000010 00000 n \n" +
                        "0000000053 00000 n \n" +
                        "0000000125 00000 n \n" +
                        "0000000280 00000 n \n" +
                        "0000000650 00000 n \n" +
                        "trailer\n" +
                        "<<\n" +
                        "/Size 6\n" +
                        "/Root 1 0 R\n" +
                        ">>\n" +
                        "startxref\n" +
                        "720\n" +
                        "%%%%EOF",
                documentTitle,
                nom != null ? nom : "N/A",
                prenom != null ? prenom : "N/A",
                cin != null ? cin : "N/A",
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
    }

    /**
     * Retourne le titre du document selon son type
     */
    private String getDocumentTitle(TypeDocument type) {
        return switch (type) {
            case CV -> "CURRICULUM VITAE";
            case CIN -> "COPIE CARTE IDENTITE NATIONALE";
            case DIPLOME -> "COPIE DIPLOME";
            default -> "DOCUMENT";
        };
    }

    /**
     * Génère un nom de fichier pour le document
     */
    public String generateFileName(TypeDocument type, String nom, String prenom) {
        String prefix = switch (type) {
            case CV -> "CV";
            case CIN -> "CIN";
            case DIPLOME -> "Diplome";
            default -> "Document";
        };

        String candidatName = "";
        if (nom != null && prenom != null) {
            candidatName = "_" + nom.replace(" ", "_") + "_" + prenom.replace(" ", "_");
        }

        return String.format("%s%s.pdf", prefix, candidatName);
    }
}