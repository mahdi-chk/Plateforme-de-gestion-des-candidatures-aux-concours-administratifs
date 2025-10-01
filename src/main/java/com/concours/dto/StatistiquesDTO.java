package com.concours.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Data
public class StatistiquesDTO {
    private long nbConcours;
    private long nbCandidatures;
    private Long nbCentres;
    private int nbPostes;
    private long nbUtilisateurs;
    private long totalCandidatures;
    private long candidaturesValidees;
    private long candidaturesEnAttente;
    private long candidaturesRejetees;
    private Map<String, Long> candidaturesParConcours;
    private Map<String, Long> candidaturesParSpecialite;
    private Map<String, Long> candidaturesParCentre;
    private Map<String, Long> candidaturesParMois;


}