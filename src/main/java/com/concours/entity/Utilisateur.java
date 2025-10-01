package com.concours.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "utilisateurs")
public class Utilisateur {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RoleUtilisateur role;

    private boolean enabled = true;

    @Column(unique = true, length = 100)
    private String email;

    @OneToMany(mappedBy = "utilisateurTraitant")
    private List<Candidature> candidaturesTraitees;

    @ManyToMany
    @JoinTable(
            name = "utilisateur_specialites",
            joinColumns = @JoinColumn(name = "utilisateur_id"),
            inverseJoinColumns = @JoinColumn(name = "specialite_id")
    )
    private List<Specialite> specialitesGerees;

    @ManyToMany
    @JoinTable(
            name = "utilisateur_concours",
            joinColumns = @JoinColumn(name = "utilisateur_id"),
            inverseJoinColumns = @JoinColumn(name = "concours_id")
    )
    private List<Concours> concoursOrganises;

    @ManyToMany
    @JoinTable(
            name = "utilisateur_centres",
            joinColumns = @JoinColumn(name = "utilisateur_id"),
            inverseJoinColumns = @JoinColumn(name = "centre_id")
    )
    private List<CentreExamen> centresAffectes;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
}
