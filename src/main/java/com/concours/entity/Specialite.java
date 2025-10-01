package com.concours.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class Specialite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String libelle;

    private int nbPostes;

    @ManyToMany(mappedBy = "specialites")
    private List<Concours> concours;

    @ManyToMany(mappedBy = "specialitesGerees")
    private List<Utilisateur> gestionnaires;

    @ManyToMany(mappedBy = "specialites")
    private List<CentreExamen> centres;
}
