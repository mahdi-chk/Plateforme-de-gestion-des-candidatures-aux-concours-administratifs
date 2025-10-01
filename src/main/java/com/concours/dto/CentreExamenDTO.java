package com.concours.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CentreExamenDTO {
    private Long id;

    @NotBlank(message = "Le code est obligatoire")
    @Size(max = 20, message = "Le code ne peut pas dépasser 20 caractères")
    private String code;

    @Min(value = 1, message = "La capacité doit être au moins 1")
    private int capacite;

    private boolean actif = true;

    @NotNull(message = "La ville est obligatoire")
    private Long villeId;

    private String villeNom;

    private List<SpecialiteDTO> specialites;
    
    // Pour le binding du formulaire
    private List<Long> specialiteIds;
}
