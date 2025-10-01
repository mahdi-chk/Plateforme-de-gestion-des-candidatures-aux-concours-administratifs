package com.concours.dto;

import com.concours.entity.TypeDocument;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {
    private Long id;
    private TypeDocument type;
    private String nom;
    private String contentType;
    private Long taille;
    private String tailleFormatee;
    private LocalDateTime dateUpload;

    // Pour compatibilité avec l'ancien système (sera supprimé progressivement)
    @Deprecated
    private String cheminFichier;

    // Constructeur sans contenu binaire (pour les listes)
    public DocumentDTO(Long id, TypeDocument type, String nom, String contentType, Long taille, LocalDateTime dateUpload) {
        this.id = id;
        this.type = type;
        this.nom = nom;
        this.contentType = contentType;
        this.taille = taille;
        this.dateUpload = dateUpload;
        this.tailleFormatee = formatTaille(taille);
    }

    // Méthode utilitaire pour formater la taille
    private String formatTaille(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";

        double b = bytes.doubleValue();
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;

        while (b >= 1024 && unitIndex < units.length - 1) {
            b /= 1024;
            unitIndex++;
        }

        return String.format("%.1f %s", b, units[unitIndex]);
    }

    // Setter personnalisé pour calculer automatiquement la taille formatée
    public void setTaille(Long taille) {
        this.taille = taille;
        this.tailleFormatee = formatTaille(taille);
    }

    // Méthodes pour vérifier le type de document
    public boolean isCV() {
        return TypeDocument.CV.equals(this.type);
    }

    public boolean isCIN() {
        return TypeDocument.CIN.equals(this.type);
    }

    public boolean isDiplome() {
        return TypeDocument.DIPLOME.equals(this.type);
    }

    // Méthode pour obtenir l'extension du fichier
    public String getExtension() {
        if (nom == null || !nom.contains(".")) {
            return "";
        }
        return nom.substring(nom.lastIndexOf(".") + 1).toLowerCase();
    }

    // Méthode pour vérifier si c'est un PDF
    public boolean isPDF() {
        return "application/pdf".equals(contentType) || "pdf".equals(getExtension());
    }

    // URL de téléchargement (sera générée côté contrôleur)
    public String getDownloadUrl() {
        return "/documents/download/" + id;
    }

    // URL d'affichage (pour PDF)
    public String getViewUrl() {
        if (isPDF()) {
            return "/documents/view/" + id;
        }
        return getDownloadUrl();
    }
}