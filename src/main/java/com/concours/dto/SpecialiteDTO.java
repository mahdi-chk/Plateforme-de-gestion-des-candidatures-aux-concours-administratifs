package com.concours.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SpecialiteDTO {
    private Long id;

    @NotBlank(message = "Le code est obligatoire")
    @Size(max = 20, message = "Le code ne peut pas dépasser 20 caractères")
    private String code;

    @NotBlank(message = "Le libellé est obligatoire")
    @Size(max = 100, message = "Le libellé ne peut pas dépasser 100 caractères")
    private String libelle;

    @Min(value = 0, message = "Le nombre de postes ne peut pas être négatif")
    private int nbPostes;
}