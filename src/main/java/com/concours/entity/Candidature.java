package com.concours.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class Candidature {
    @Id
    private String numero;

    @Enumerated(EnumType.STRING)
    private StatutCandidature statut = StatutCandidature.EN_ATTENTE;

    private LocalDate dateDepot = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private TypeNotification notifications;

    @Column(name = "conditions_acceptees")
    private boolean accepter;

    @OneToMany(mappedBy = "candidature", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Document> documents = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat_id", nullable = false)
    private Candidat candidat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concours_id", nullable = false)
    private Concours concours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialite_id", nullable = false)
    private Specialite specialite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centre_examen_id", nullable = false)
    private CentreExamen centreExamen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_traitant_id")
    private Utilisateur utilisateurTraitant;

    // Méthode utilitaire pour ajouter des documents
    public void addDocument(Document document) {
        if (this.documents == null) {
            this.documents = new ArrayList<>();
        }
        documents.add(document);
        document.setCandidature(this);
    }

    // Méthode pour obtenir un document par type
    public Document getDocumentByType(TypeDocument type) {
        return documents.stream()
                .filter(doc -> doc.getType() == type)
                .findFirst()
                .orElse(null);
    }

    // Méthode pour vérifier si tous les documents requis sont présents
    public boolean hasAllRequiredDocuments() {
        return hasDocumentOfType(TypeDocument.CV) &&
                hasDocumentOfType(TypeDocument.CIN) &&
                hasDocumentOfType(TypeDocument.DIPLOME);
    }

    private boolean hasDocumentOfType(TypeDocument type) {
        return documents.stream()
                .anyMatch(doc -> doc.getType() == type);
    }

    // Méthode pour calculer la taille totale des documents
    public long getTailleTotaleDocuments() {
        return documents.stream()
                .mapToLong(Document::getTaille)
                .sum();
    }
}