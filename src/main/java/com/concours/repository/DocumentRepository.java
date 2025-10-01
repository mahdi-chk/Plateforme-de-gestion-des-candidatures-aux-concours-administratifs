package com.concours.repository;

import com.concours.entity.Document;
import com.concours.entity.Candidature;
import com.concours.entity.TypeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    /**
     * Trouve tous les documents d'une candidature
     */
    List<Document> findByCandidature(Candidature candidature);

    /**
     * Trouve un document par candidature et type
     */
    Optional<Document> findByCandidatureAndType(Candidature candidature, TypeDocument type);

    /**
     * Compte les documents par candidature
     */
    long countByCandidature(Candidature candidature);

    /**
     * Trouve les documents par type
     */
    List<Document> findByType(TypeDocument type);

    /**
     * Compte les documents par type
     */
    long countByType(TypeDocument type);

    /**
     * Trouve les documents d'une candidature par type
     */
    @Query("SELECT d FROM Document d WHERE d.candidature = :candidature AND d.type = :type")
    List<Document> findDocumentsByCandidatureAndType(@Param("candidature") Candidature candidature,
                                                     @Param("type") TypeDocument type);

    /**
     * Calcule la taille totale de tous les documents
     */
    @Query("SELECT COALESCE(SUM(d.taille), 0) FROM Document d")
    Long getTotalDocumentsSize();

    /**
     * Calcule la taille totale des documents par candidature
     */
    @Query("SELECT COALESCE(SUM(d.taille), 0) FROM Document d WHERE d.candidature = :candidature")
    Long getTotalSizeByCandidature(@Param("candidature") Candidature candidature);

    /**
     * Calcule la taille totale des documents par type
     */
    @Query("SELECT COALESCE(SUM(d.taille), 0) FROM Document d WHERE d.type = :type")
    Long getTotalSizeByType(@Param("type") TypeDocument type);

    /**
     * Trouve les documents les plus volumineux
     */
    @Query("SELECT d FROM Document d ORDER BY d.taille DESC")
    List<Document> findDocumentsOrderByTailleDesc();

    /**
     * Trouve les documents les plus récents
     */
    @Query("SELECT d FROM Document d ORDER BY d.dateUpload DESC")
    List<Document> findDocumentsOrderByDateUploadDesc();

    /**
     * Compte les documents par candidature et type
     */
    long countByCandidatureAndType(Candidature candidature, TypeDocument type);

    /**
     * Vérifie si une candidature a tous les documents requis
     */
    @Query("""
        SELECT CASE WHEN 
            (SELECT COUNT(d) FROM Document d WHERE d.candidature = :candidature AND d.type = 'CV') > 0 AND
            (SELECT COUNT(d) FROM Document d WHERE d.candidature = :candidature AND d.type = 'CIN') > 0 AND
            (SELECT COUNT(d) FROM Document d WHERE d.candidature = :candidature AND d.type = 'DIPLOME') > 0
        THEN true ELSE false END
        FROM Document d WHERE d.candidature = :candidature
        """)
    Boolean hasAllRequiredDocuments(@Param("candidature") Candidature candidature);

    /**
     * Trouve les documents avec un contenu plus grand qu'une taille donnée
     */
    @Query("SELECT d FROM Document d WHERE d.taille > :taille")
    List<Document> findDocumentsLargerThan(@Param("taille") Long taille);

    /**
     * Statistiques par type de document
     */
    @Query("""
        SELECT d.type as type, 
               COUNT(d) as count, 
               COALESCE(SUM(d.taille), 0) as totalSize,
               COALESCE(AVG(d.taille), 0) as avgSize
        FROM Document d 
        GROUP BY d.type
        """)
    List<DocumentTypeStats> getDocumentStatsByType();

    /**
     * Interface de projection pour les statistiques par type
     */
    interface DocumentTypeStats {
        TypeDocument getType();
        Long getCount();
        Long getTotalSize();
        Double getAvgSize();
    }

    /**
     * Trouve les candidatures sans document d'un type donné
     */
    @Query("""
        SELECT DISTINCT c FROM Candidature c 
        WHERE c NOT IN (
            SELECT DISTINCT d.candidature FROM Document d WHERE d.type = :type
        )
        """)
    List<Candidature> findCandidaturesWithoutDocumentType(@Param("type") TypeDocument type);

    /**
     * Compte les documents uploadés aujourd'hui
     */
    @Query("SELECT COUNT(d) FROM Document d WHERE DATE(d.dateUpload) = CURRENT_DATE")
    Long countDocumentsUploadedToday();

    /**
     * Trouve les documents par candidature avec informations de base seulement (sans contenu)
     * Utile pour l'affichage des listes sans charger les gros volumes de données
     */
    @Query("""
        SELECT new com.concours.dto.DocumentInfoDTO(
            d.id, d.type, d.nom, d.contentType, d.taille, d.dateUpload
        )
        FROM Document d 
        WHERE d.candidature = :candidature
        ORDER BY d.type, d.dateUpload DESC
        """)
    List<DocumentInfoDTO> findDocumentInfoByCandidature(@Param("candidature") Candidature candidature);

    /**
     * DTO pour les informations de document sans le contenu binaire
     */
    public static class DocumentInfoDTO {
        private Long id;
        private TypeDocument type;
        private String nom;
        private String contentType;
        private Long taille;
        private java.time.LocalDateTime dateUpload;

        public DocumentInfoDTO(Long id, TypeDocument type, String nom, String contentType,
                               Long taille, java.time.LocalDateTime dateUpload) {
            this.id = id;
            this.type = type;
            this.nom = nom;
            this.contentType = contentType;
            this.taille = taille;
            this.dateUpload = dateUpload;
        }

        // Getters
        public Long getId() { return id; }
        public TypeDocument getType() { return type; }
        public String getNom() { return nom; }
        public String getContentType() { return contentType; }
        public Long getTaille() { return taille; }
        public java.time.LocalDateTime getDateUpload() { return dateUpload; }

        public String getTailleFormatee() {
            if (taille == null) return "0 B";

            double bytes = taille.doubleValue();
            String[] units = {"B", "KB", "MB", "GB"};
            int unitIndex = 0;

            while (bytes >= 1024 && unitIndex < units.length - 1) {
                bytes /= 1024;
                unitIndex++;
            }

            return String.format("%.1f %s", bytes, units[unitIndex]);
        }
    }

    /**
     * Supprime les documents d'une candidature (pour le nettoyage)
     */
    @Query("DELETE FROM Document d WHERE d.candidature = :candidature")
    void deleteByCandidature(@Param("candidature") Candidature candidature);
}