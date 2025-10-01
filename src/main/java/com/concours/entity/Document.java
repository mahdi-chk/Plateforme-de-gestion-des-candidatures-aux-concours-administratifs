package com.concours.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class Document {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeDocument type;

    @Column(nullable = false, length = 255)
    private String nom; // Nom original du fichier

    @Column(nullable = false, length = 100)
    private String contentType; // Type MIME du fichier

    @Column(nullable = false)
    private Long taille; // Taille du fichier en octets

    @Lob
    @Column(name = "contenu", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] contenu;

    @Column(name = "date_upload", nullable = false)
    private java.time.LocalDateTime dateUpload = java.time.LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidature_id", nullable = false)
    private Candidature candidature;

    // Constructeur utilitaire
    public Document(TypeDocument type, String nom, String contentType, byte[] contenu, Candidature candidature) {
        this.type = type;
        this.nom = nom;
        this.contentType = contentType;
        this.contenu = contenu;
        this.taille = contenu != null ? (long) contenu.length : 0L;
        this.candidature = candidature;
        this.dateUpload = java.time.LocalDateTime.now();
    }

    // Méthode pour obtenir la taille formatée
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