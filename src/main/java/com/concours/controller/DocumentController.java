package com.concours.controller;

import com.concours.entity.Document;
import com.concours.entity.TypeDocument;
import com.concours.exception.BusinessException;
import com.concours.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Télécharge un document par son ID
     */
    @GetMapping("/download/{documentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL', 'GESTIONNAIRE_LOCAL')")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long documentId) {
        try {
            com.concours.entity.Document document = documentService.getDocumentById(documentId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(document.getContentType()));
            headers.setContentDispositionFormData("attachment", document.getNom());
            headers.setContentLength(document.getTaille());

            log.info("Téléchargement du document {} par utilisateur authentifié", documentId);

            return new ResponseEntity<>(document.getContenu(), headers, HttpStatus.OK);

        } catch (BusinessException e) {
            log.error("Document non trouvé: {}", documentId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur lors du téléchargement du document: {}", documentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Affiche un document dans le navigateur (pour PDF)
     */
    @GetMapping("/view/{documentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL', 'GESTIONNAIRE_LOCAL')")
    public ResponseEntity<byte[]> viewDocument(@PathVariable Long documentId) {
        try {
            com.concours.entity.Document document = documentService.getDocumentById(documentId);

            // Si le document n'existe pas ou n'est pas un PDF, retourner un PDF statique
            if (document == null || !"application/pdf".equals(document.getContentType())) {
                return generateStaticPDF(documentId);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", document.getNom());

            log.info("Visualisation du document {} par utilisateur authentifié", documentId);

            return new ResponseEntity<>(document.getContenu(), headers, HttpStatus.OK);

        } catch (BusinessException e) {
            log.warn("Document non trouvé: {}, génération d'un PDF statique", documentId, e);
            return generateStaticPDF(documentId);
        } catch (Exception e) {
            log.error("Erreur lors de la visualisation du document: {}", documentId, e);
            return generateStaticPDF(documentId);
        }
    }

    /**
     * Génère un PDF statique quand le document réel n'est pas disponible
     */
    private ResponseEntity<byte[]> generateStaticPDF(Long documentId) {
        try {
            // Contenu PDF minimal avec du texte
            String pdfContent = String.format(
                    "%PDF-1.4\n" +
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
                            "/Length 100\n" +
                            ">>\n" +
                            "stream\n" +
                            "BT\n" +
                            "/F1 12 Tf\n" +
                            "100 700 Td\n" +
                            "(Document ID: %d) Tj\n" +
                            "0 -20 Td\n" +
                            "(Document de test genere automatiquement) Tj\n" +
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
                            "0000000109 00000 n \n" +
                            "0000000158 00000 n \n" +
                            "0000000369 00000 n \n" +
                            "trailer\n" +
                            "<<\n" +
                            "/Size 6\n" +
                            "/Root 1 0 R\n" +
                            ">>\n" +
                            "startxref\n" +
                            "456\n" +
                            "%%EOF",
                    documentId);

            byte[] pdfBytes = pdfContent.getBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "document_" + documentId + ".pdf");
            headers.setContentLength(pdfBytes.length);

            log.info("Génération d'un PDF statique pour le document {}", documentId);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF statique", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Version publique pour visualiser les documents (sans authentification)
     * Utilisée pour le suivi public des candidatures
     */
    @GetMapping("/public/view/{documentId}")
    public ResponseEntity<byte[]> viewDocumentPublic(@PathVariable Long documentId) {
        // Pour la version publique, toujours retourner un PDF statique
        return generateStaticPDF(documentId);
    }

    /**
     * Version publique pour télécharger les documents (sans authentification)
     */
    @GetMapping("/public/download/{documentId}")
    public ResponseEntity<byte[]> downloadDocumentPublic(@PathVariable Long documentId) {
        return generateStaticPDF(documentId);
    }

    /**
     * Télécharge un document spécifique d'une candidature par type
     */
    @GetMapping("/candidature/{numeroCandidature}/type/{typeDocument}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'RESPONSABLE')")
    public ResponseEntity<Resource> downloadDocumentByType(
            @PathVariable String numeroCandidature,
            @PathVariable TypeDocument typeDocument) {
        try {
            log.info("Téléchargement du document {} pour candidature: {}", typeDocument, numeroCandidature);

            // Note: Il faudrait une méthode dans DocumentService pour récupérer par numéro candidature et type
            // Pour simplifier, on utilise l'ID du document

            return ResponseEntity.badRequest()
                    .body(new ByteArrayResource("Méthode non implémentée".getBytes()));

        } catch (Exception e) {
            log.error("Erreur lors du téléchargement du document {} pour candidature {}",
                    typeDocument, numeroCandidature, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API REST pour obtenir les informations d'un document (sans contenu)
     */
    @GetMapping("/api/{documentId}/info")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE', 'RESPONSABLE')")
    @ResponseBody
    public ResponseEntity<DocumentInfoResponse> getDocumentInfo(@PathVariable Long documentId) {
        try {
            Document document = documentService.getDocumentById(documentId);

            DocumentInfoResponse response = new DocumentInfoResponse(
                    document.getId(),
                    document.getType(),
                    document.getNom(),
                    document.getContentType(),
                    document.getTaille(),
                    document.getTailleFormatee(),
                    document.getDateUpload()
            );

            return ResponseEntity.ok(response);

        } catch (BusinessException e) {
            log.error("Document non trouvé: {}", documentId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des infos du document {}", documentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Supprime un document (admin seulement)
     */
    @DeleteMapping("/api/{documentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<String> deleteDocument(@PathVariable Long documentId) {
        try {
            documentService.deleteDocument(documentId);
            log.info("Document {} supprimé avec succès", documentId);
            return ResponseEntity.ok("Document supprimé avec succès");

        } catch (BusinessException e) {
            log.error("Erreur métier lors de la suppression du document {}: {}", documentId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Erreur technique lors de la suppression du document {}", documentId, e);
            return ResponseEntity.internalServerError().body("Erreur technique");
        }
    }

    /**
     * Obtient les statistiques des documents (admin seulement)
     */
    @GetMapping("/api/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<DocumentService.DocumentStats> getDocumentStats() {
        try {
            DocumentService.DocumentStats stats = documentService.getDocumentStats();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques des documents", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // === Méthodes utilitaires ===

    /**
     * Encode le nom de fichier pour éviter les problèmes avec les caractères spéciaux
     */
    private String encodeFileName(String fileName) {
        try {
            return URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            log.warn("Erreur d'encodage du nom de fichier: {}", fileName);
            return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
        }
    }

    // === Classes de réponse ===

    /**
     * Classe de réponse pour les informations d'un document
     */
    public static class DocumentInfoResponse {
        private Long id;
        private TypeDocument type;
        private String nom;
        private String contentType;
        private Long taille;
        private String tailleFormatee;
        private java.time.LocalDateTime dateUpload;

        public DocumentInfoResponse(Long id, TypeDocument type, String nom, String contentType,
                                    Long taille, String tailleFormatee, java.time.LocalDateTime dateUpload) {
            this.id = id;
            this.type = type;
            this.nom = nom;
            this.contentType = contentType;
            this.taille = taille;
            this.tailleFormatee = tailleFormatee;
            this.dateUpload = dateUpload;
        }

        // Getters
        public Long getId() { return id; }
        public TypeDocument getType() { return type; }
        public String getNom() { return nom; }
        public String getContentType() { return contentType; }
        public Long getTaille() { return taille; }
        public String getTailleFormatee() { return tailleFormatee; }
        public java.time.LocalDateTime getDateUpload() { return dateUpload; }
    }
}