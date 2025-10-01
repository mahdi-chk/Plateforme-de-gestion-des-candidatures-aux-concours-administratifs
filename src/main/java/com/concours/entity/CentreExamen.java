package com.concours.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class CentreExamen {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20)
    private String code;

    private int capacite;

    private boolean actif = true;

    @ManyToOne
    @JoinColumn(name = "ville_id")
    private Ville ville;

    @OneToMany(mappedBy = "centreExamen")
    private List<Candidature> candidatures;

    @ManyToMany(mappedBy = "centresAffectes")
    private List<Utilisateur> utilisateurs;

    @ManyToMany
    @JoinTable(
        name = "centre_specialite",
        joinColumns = @JoinColumn(name = "centre_id"),
        inverseJoinColumns = @JoinColumn(name = "specialite_id")
    )
    private List<Specialite> specialites;

}
