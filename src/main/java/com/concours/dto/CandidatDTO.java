package com.concours.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CandidatDTO {
    private Long id;
    private String cin;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private LocalDate dateNaissance;
    private String lieuNaissance;
    private String diplome;
    private String adresse;
    private String villeResidence;

    // Pour compatibilité avec les relations
    private Long lieuNaissanceId;
    private Long villeResidenceId;
}