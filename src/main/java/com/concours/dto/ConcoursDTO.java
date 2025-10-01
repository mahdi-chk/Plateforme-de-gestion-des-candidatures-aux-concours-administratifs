package com.concours.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ConcoursDTO {
    private Long id;

    // Référence générée automatiquement, pas de validation
    private String reference;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 200, message = "Le titre ne peut pas dépasser 200 caractères")
    private String titre;

    @NotNull(message = "La date d'ouverture est obligatoire")
    @FutureOrPresent(message = "La date d'ouverture doit être aujourd'hui ou dans le futur")
    private LocalDate dateOuverture;

    @NotNull(message = "La date de clôture est obligatoire")
    @Future(message = "La date de clôture doit être dans le futur")
    private LocalDate dateCloture;

    @NotNull(message = "La date du concours est obligatoire")
    @Future(message = "La date du concours doit être dans le futur")
    private LocalDate dateConcours;

    @Min(value = 1, message = "Le nombre de postes doit être au moins 1")
    private int nbPostes;

    @NotBlank(message = "Les conditions sont obligatoires")
    private String conditions;

    private boolean publie;

    // Pour le binding du formulaire
    private String centresExamenIds;

    private List<SpecialiteDTO> specialites;
    private List<CentreExamenDTO> centresExamen;
    private List<String> organisateurs;
}