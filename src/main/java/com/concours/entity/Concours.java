package com.concours.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Concours {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 50)
    private String reference;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(nullable = false)
    private LocalDate dateOuverture;

    @Column(nullable = false)
    private LocalDate dateCloture;

    @Column(nullable = false)
    private LocalDate dateConcours;

    private int nbPostes;

    @Column(columnDefinition = "TEXT", name = "conditions")
    private String conditions;

    private boolean publie = false;

    @OneToMany(mappedBy = "concours")
    private List<Candidature> candidatures = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "concours_specialites",
            joinColumns = @JoinColumn(name = "concours_id"),
            inverseJoinColumns = @JoinColumn(name = "specialite_id")
    )
    private List<Specialite> specialites = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "concours_centres",
            joinColumns = @JoinColumn(name = "concours_id"),
            inverseJoinColumns = @JoinColumn(name = "centre_id")
    )
    private List<CentreExamen> centresExamen = new ArrayList<>();

    @ManyToMany(mappedBy = "concoursOrganises")
    private List<Utilisateur> organisateurs = new ArrayList<>();

    // Constructeur pour générer la référence automatiquement
    @PrePersist
    public void prePersist() {
        if (this.reference == null) {
            this.reference = genererReference();
        }
    }

    // Méthode pour générer une référence auto-incrémentée
    private String genererReference() {
        return "CONC-" + System.currentTimeMillis();
    }
}