package com.concours.service;

import com.concours.entity.Candidature;
import com.concours.entity.Document;
import com.concours.entity.TypeDocument;
import com.concours.exception.BusinessException;
import com.concours.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;

    // Taille maximale par fichier (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // Types MIME autorisés
    private static final String ALLOWED_CONTENT_TYPE = "application/pdf";

    /**
     * Upload et sauvegarde d'un document en base de données
     */
    public Document uploadDocument(Candidature candidature, MultipartFile file, TypeDocument type) {
        try {
            log.info("Upload du document {} pour la candidature {}", type, candidature.getNumero());

            // Validation du fichier
            validateFile(file);

            // Conversion en bytes
            byte[] fileBytes = file.getBytes();

            // Création du document
            Document document = new Document(
                    type,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    fileBytes,
                    candidature
            );

            // Sauvegarde en base
            Document savedDocument = documentRepository.save(document);

            log.info("Document {} sauvegardé avec succès, ID: {}, Taille: {} bytes",
                    type, savedDocument.getId(), fileBytes.length);

            return savedDocument;

        } catch (IOException e) {
            log.error("Erreur lors de la lecture du fichier", e);
            throw new BusinessException("Erreur lors de la lecture du fichier: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erreur lors de la sauvegarde du document", e);
            throw new BusinessException("Erreur lors de la sauvegarde du document: " + e.getMessage());
        }
    }

    /**
     * Upload multiple documents (CV, CIN, Diplome)
     */
    public void uploadDocuments(Candidature candidature,
                                MultipartFile cvFile,
                                MultipartFile cinFile,
                                MultipartFile diplomeFile) {
        try {
            log.info("Upload des documents pour la candidature {}", candidature.getNumero());

            // Upload du CV
            if (cvFile != null && !cvFile.isEmpty()) {
                Document cvDoc = uploadDocument(candidature, cvFile, TypeDocument.CV);
                candidature.addDocument(cvDoc);
            }

            // Upload de la CIN
            if (cinFile != null && !cinFile.isEmpty()) {
                Document cinDoc = uploadDocument(candidature, cinFile, TypeDocument.CIN);
                candidature.addDocument(cinDoc);
            }

            // Upload du diplôme
            if (diplomeFile != null && !diplomeFile.isEmpty()) {
                Document diplomeDoc = uploadDocument(candidature, diplomeFile, TypeDocument.DIPLOME);
                candidature.addDocument(diplomeDoc);
            }

            log.info("Tous les documents ont été uploadés avec succès pour la candidature {}",
                    candidature.getNumero());

        } catch (Exception e) {
            log.error("Erreur lors de l'upload des documents", e);
            throw new BusinessException("Erreur lors de l'upload des documents: " + e.getMessage());
        }
    }

    /**
     * Récupère un document par son ID
     */
    @Transactional(readOnly = true)
    public Document getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Document non trouvé avec l'ID: " + id));
    }

    /**
     * Récupère tous les documents d'une candidature
     */
    @Transactional(readOnly = true)
    public List<Document> getDocumentsByCandidature(Candidature candidature) {
        return documentRepository.findByCandidature(candidature);
    }

    /**
     * Récupère un document d'une candidature par son type
     */
    @Transactional(readOnly = true)
    public Optional<Document> getDocumentByCandidatureAndType(Candidature candidature, TypeDocument type) {
        return documentRepository.findByCandidatureAndType(candidature, type);
    }

    /**
     * Supprime un document
     */
    public void deleteDocument(Long documentId) {
        try {
            Document document = getDocumentById(documentId);
            documentRepository.delete(document);
            log.info("Document {} supprimé avec succès", documentId);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du document {}", documentId, e);
            throw new BusinessException("Erreur lors de la suppression du document");
        }
    }

    /**
     * Supprime tous les documents d'une candidature
     */
    public void deleteDocumentsByCandidature(Candidature candidature) {
        try {
            List<Document> documents = getDocumentsByCandidature(candidature);
            if (!documents.isEmpty()) {
                documentRepository.deleteAll(documents);
                log.info("{} documents supprimés pour la candidature {}",
                        documents.size(), candidature.getNumero());
            }
        } catch (Exception e) {
            log.error("Erreur lors de la suppression des documents de la candidature {}",
                    candidature.getNumero(), e);
            throw new BusinessException("Erreur lors de la suppression des documents");
        }
    }

    /**
     * Remplace un document existant
     */
    public Document replaceDocument(Candidature candidature, MultipartFile newFile, TypeDocument type) {
        try {
            // Supprimer l'ancien document s'il existe
            Optional<Document> existingDoc = getDocumentByCandidatureAndType(candidature, type);
            if (existingDoc.isPresent()) {
                documentRepository.delete(existingDoc.get());
                log.info("Ancien document {} supprimé", type);
            }

            // Créer le nouveau document
            return uploadDocument(candidature, newFile, type);

        } catch (Exception e) {
            log.error("Erreur lors du remplacement du document {}", type, e);
            throw new BusinessException("Erreur lors du remplacement du document");
        }
    }

    /**
     * Vérifie si tous les documents requis sont présents pour une candidature
     */
    @Transactional(readOnly = true)
    public boolean hasAllRequiredDocuments(Candidature candidature) {
        List<Document> documents = getDocumentsByCandidature(candidature);

        boolean hasCV = documents.stream().anyMatch(doc -> doc.getType() == TypeDocument.CV);
        boolean hasCIN = documents.stream().anyMatch(doc -> doc.getType() == TypeDocument.CIN);
        boolean hasDiplome = documents.stream().anyMatch(doc -> doc.getType() == TypeDocument.DIPLOME);

        return hasCV && hasCIN && hasDiplome;
    }

    /**
     * Calcule la taille totale des documents d'une candidature
     */
    @Transactional(readOnly = true)
    public long getTotalDocumentsSize(Candidature candidature) {
        return getDocumentsByCandidature(candidature).stream()
                .mapToLong(Document::getTaille)
                .sum();
    }

    /**
     * Validation d'un fichier uploadé
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Le fichier est vide ou null");
        }

        // Vérification de la taille
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(
                    String.format("Le fichier dépasse la taille maximale autorisée (%.1f MB)",
                            MAX_FILE_SIZE / (1024.0 * 1024.0))
            );
        }

        // Vérification du type MIME
        String contentType = file.getContentType();
        if (!ALLOWED_CONTENT_TYPE.equals(contentType)) {
            throw new BusinessException("Seuls les fichiers PDF sont acceptés. Type reçu: " + contentType);
        }

        // Vérification du nom de fichier
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new BusinessException("Le nom du fichier est invalide");
        }

        // Vérification de l'extension
        if (!filename.toLowerCase().endsWith(".pdf")) {
            throw new BusinessException("Le fichier doit avoir l'extension .pdf");
        }

        log.debug("Fichier validé: {}, Taille: {} bytes, Type: {}",
                filename, file.getSize(), contentType);
    }

    /**
     * Obtient les statistiques des documents
     */
    @Transactional(readOnly = true)
    public DocumentStats getDocumentStats() {
        long totalDocuments = documentRepository.count();

        // Calcul de la taille totale (requête native pour performance)
        Long totalSize = documentRepository.getTotalDocumentsSize();
        if (totalSize == null) totalSize = 0L;

        return new DocumentStats(totalDocuments, totalSize);
    }

    /**
     * Classe interne pour les statistiques des documents
     */
    public static class DocumentStats {
        private final long totalDocuments;
        private final long totalSize;

        public DocumentStats(long totalDocuments, long totalSize) {
            this.totalDocuments = totalDocuments;
            this.totalSize = totalSize;
        }

        public long getTotalDocuments() { return totalDocuments; }
        public long getTotalSize() { return totalSize; }

        public String getTotalSizeFormatted() {
            double bytes = totalSize;
            String[] units = {"B", "KB", "MB", "GB", "TB"};
            int unitIndex = 0;

            while (bytes >= 1024 && unitIndex < units.length - 1) {
                bytes /= 1024;
                unitIndex++;
            }

            return String.format("%.1f %s", bytes, units[unitIndex]);
        }
    }
}