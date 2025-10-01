package com.concours.dto;

import lombok.Data;

@Data
public class StatistiquesLocalDTO {
    private long nbCandidatsCentre;
    private int nbSpecialitesCentre;
    private long nbPlacesRestantes;
    private long nbCandidaturesEnAttente;
    private long nbCandidaturesValidees;
    private long nbCandidaturesRejetees;
    private int tauxRemplissage; // en pourcentage
    private String nomCentre;
    private String villeCentre;
    private int capaciteTotale;
}
