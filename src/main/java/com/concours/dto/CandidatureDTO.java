package com.concours.dto;

import com.concours.entity.StatutCandidature;
import com.concours.entity.TypeNotification;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CandidatureDTO {
    private String numero;
    private StatutCandidature statut;
    private LocalDate dateDepot;
    private TypeNotification notifications;
    private boolean accepter;

    // Informations candidat (dénormalisées)
    private String candidatNom;
    private String candidatPrenom;
    private String candidatCin;
    private String candidatEmail;
    private String candidatTelephone;
    private String candidatDiplome;
    private String candidatNiveauEtude;
    private String candidatExperience;
    private String candidatAdresse;
    private LocalDate candidatDateNaissance; // Ajouté
    private String candidatLieuNaissance;

    // Informations concours
    private String concoursTitre;
    private String concoursReference;
    private Long concoursId; // Ajouté pour les relations

    // Informations spécialité
    private String specialiteLibelle;
    private String specialiteCode;
    private Long specialiteId; // Ajouté pour les relations

    // Informations centre
    private String centreCode;
    private String centreVille;
    private Long centreId; // Ajouté pour les relations

    // Documents
    private List<DocumentDTO> documents;

    // Utilisateur traitant
    private String utilisateurTraitant;
    private Long utilisateurTraitantId;


}