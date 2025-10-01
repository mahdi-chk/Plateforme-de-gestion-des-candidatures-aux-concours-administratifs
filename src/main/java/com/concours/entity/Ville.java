package com.concours.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "villes")
@Data @NoArgsConstructor @AllArgsConstructor
public class Ville {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @OneToMany(mappedBy = "ville")
    private List<CentreExamen> centresExamens;

    @OneToMany(mappedBy = "lieuNaissance")
    private List<Candidat> candidatsNes;

    @OneToMany(mappedBy = "villeResidence")
    private List<Candidat> candidatsResidents;
}
