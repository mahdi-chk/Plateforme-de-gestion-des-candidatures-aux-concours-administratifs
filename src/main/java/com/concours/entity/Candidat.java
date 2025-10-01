package com.concours.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class Candidat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20)
    private String cin;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    private LocalDate dateNaissance;

    @Enumerated(EnumType.STRING)
    private Genre sexe;

    @Column(length = 255)
    private String adresse;

    @Column(unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String telephone;

    @Column(name = "niveau_etude", length = 100)
    private String niveauEtude;

    @Column(length = 100)
    private String diplome;

    @Column(columnDefinition = "TEXT")
    private String experience;

    @OneToMany(mappedBy = "candidat")
    private List<Candidature> candidatures;

    @ManyToOne
    @JoinColumn(name = "lieu_naissance_id")
    private Ville lieuNaissance;

    @ManyToOne
    @JoinColumn(name = "ville_residence_id")
    private Ville villeResidence;
}