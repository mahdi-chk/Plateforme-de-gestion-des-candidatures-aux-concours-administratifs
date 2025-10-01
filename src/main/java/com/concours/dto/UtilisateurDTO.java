package com.concours.dto;

import com.concours.entity.RoleUtilisateur;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
import java.time.LocalDateTime;

@Data
public class UtilisateurDTO {
    private Long id;

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(max = 50, message = "Le nom d'utilisateur ne peut pas dépasser 50 caractères")
    private String username;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    @NotNull(message = "Le rôle est obligatoire")
    private RoleUtilisateur role;

    private boolean enabled = true;

    @Email(message = "Format d'email invalide")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    private String email;

    private List<Long> specialitesGerees;
    private List<Long> concoursOrganises;
    private List<CentreExamenDTO> centresAffectes; // Modifié pour inclure les DTO complets

    // Ajouter pour l'affichage dans les formulaires
    private List<Long> selectedCentres;

    private LocalDateTime lastLogin;

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
}
