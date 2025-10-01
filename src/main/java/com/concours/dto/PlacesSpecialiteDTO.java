package com.concours.dto;

import lombok.Data;

@Data
public class PlacesSpecialiteDTO {
    private Long specialiteId;
    private String specialiteLibelle;
    private String specialiteCode;
    private int nbPostesTotaux;
    private long nbCandidaturesValidees;
    private long nbCandidaturesEnAttente;
    private int placesRestantes;
    private double tauxOccupation;

    // Méthode utilitaire pour calculer les places restantes
    public int getPlacesRestantes() {
        return Math.max(0, nbPostesTotaux - (int) nbCandidaturesValidees);
    }

    // Méthode utilitaire pour calculer le taux d'occupation
    public double getTauxOccupation() {
        if (nbPostesTotaux == 0) return 0.0;
        return ((double) nbCandidaturesValidees / nbPostesTotaux) * 100.0;
    }
}