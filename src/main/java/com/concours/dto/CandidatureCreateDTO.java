package com.concours.dto;

import com.concours.entity.Genre;
import com.concours.entity.TypeNotification;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CandidatureCreateDTO {

    // Informations personnelles
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
    private String prenom;

    @NotBlank(message = "Le CIN est obligatoire")
    @Size(max = 20, message = "Le CIN ne peut pas dépasser 20 caractères")
    private String cin;

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateNaissance;

    @NotNull(message = "Le sexe est obligatoire")
    private Genre sexe;

    @NotBlank(message = "L'adresse est obligatoire")
    @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères")
    private String adresse;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    private String email;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    private String telephone;

    @NotBlank(message = "Le niveau d'étude est obligatoire")
    @Size(max = 100, message = "Le niveau d'étude ne peut pas dépasser 100 caractères")
    private String niveauEtude;

    @NotBlank(message = "Le diplôme est obligatoire")
    @Size(max = 100, message = "Le diplôme ne peut pas dépasser 100 caractères")
    private String diplome;

    @Size(max = 1000, message = "L'expérience ne peut pas dépasser 1000 caractères")
    private String experience;

    // Références géographiques
    @NotNull(message = "Le lieu de naissance est obligatoire")
    private Long lieuNaissanceId;

    @NotNull(message = "La ville de résidence est obligatoire")
    private Long villeResidenceId;

    // Références concours
    @NotNull(message = "Le concours est obligatoire")
    private Long concoursId;

    @NotNull(message = "La spécialité est obligatoire")
    private Long specialiteId;

    @NotNull(message = "Le centre d'examen est obligatoire")
    private Long centreExamenId;

    // Préférences
    private TypeNotification notifications = TypeNotification.EMAIL;

    @AssertTrue(message = "Vous devez accepter les conditions d'utilisation")
    private boolean accepter;
}