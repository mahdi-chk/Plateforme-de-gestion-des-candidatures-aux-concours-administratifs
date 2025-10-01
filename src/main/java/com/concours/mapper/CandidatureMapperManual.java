package com.concours.mapper;

import com.concours.dto.CandidatureDTO;
import com.concours.dto.CandidatureCreateDTO;
import com.concours.dto.DocumentDTO;
import com.concours.entity.Candidat;
import com.concours.entity.Candidature;
import com.concours.entity.Document;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CandidatureMapperManual {

    public CandidatureDTO toDTO(Candidature candidature) {
        if (candidature == null) {
            return null;
        }

        CandidatureDTO dto = new CandidatureDTO();
        dto.setNumero(candidature.getNumero());
        dto.setStatut(candidature.getStatut());
        dto.setDateDepot(candidature.getDateDepot());
        dto.setNotifications(candidature.getNotifications());
        dto.setAccepter(candidature.isAccepter());

        // Informations candidat
        if (candidature.getCandidat() != null) {
            dto.setCandidatNom(candidature.getCandidat().getNom());
            dto.setCandidatPrenom(candidature.getCandidat().getPrenom());
            dto.setCandidatCin(candidature.getCandidat().getCin());
            dto.setCandidatEmail(candidature.getCandidat().getEmail());
        }

        // Informations concours
        if (candidature.getConcours() != null) {
            dto.setConcoursTitre(candidature.getConcours().getTitre());
            dto.setConcoursReference(candidature.getConcours().getReference());
        }

        // Informations spécialité
        if (candidature.getSpecialite() != null) {
            dto.setSpecialiteLibelle(candidature.getSpecialite().getLibelle());
        }

        // Informations centre
        if (candidature.getCentreExamen() != null) {
            dto.setCentreCode(candidature.getCentreExamen().getCode());
            if (candidature.getCentreExamen().getVille() != null) {
                dto.setCentreVille(candidature.getCentreExamen().getVille().getNom());
            }
        }

        // Utilisateur traitant
        if (candidature.getUtilisateurTraitant() != null) {
            dto.setUtilisateurTraitant(candidature.getUtilisateurTraitant().getUsername());
        }

        // Documents
        if (candidature.getDocuments() != null) {
            dto.setDocuments(candidature.getDocuments().stream()
                    .map(this::documentToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public Candidat toCandidat(CandidatureCreateDTO candidatureDTO) {
        if (candidatureDTO == null) {
            return null;
        }

        Candidat candidat = new Candidat();
        candidat.setNom(candidatureDTO.getNom());
        candidat.setPrenom(candidatureDTO.getPrenom());
        candidat.setCin(candidatureDTO.getCin());
        candidat.setDateNaissance(candidatureDTO.getDateNaissance());
        candidat.setSexe(candidatureDTO.getSexe());
        candidat.setAdresse(candidatureDTO.getAdresse());
        candidat.setEmail(candidatureDTO.getEmail());
        candidat.setTelephone(candidatureDTO.getTelephone());
        candidat.setNiveauEtude(candidatureDTO.getNiveauEtude());
        candidat.setDiplome(candidatureDTO.getDiplome());
        candidat.setExperience(candidatureDTO.getExperience());

        return candidat;
    }

    private DocumentDTO documentToDTO(Document document) {
        if (document == null) {
            return null;
        }

        DocumentDTO dto = new DocumentDTO();
        dto.setId(document.getId());
        dto.setType(document.getType());
        dto.setCheminFichier(document.getNom());

        // Extraire le nom du fichier du chemin
        String chemin = document.getNom();
        if (chemin != null && !chemin.isEmpty()) {
            dto.setNom(chemin.substring(chemin.lastIndexOf("/") + 1));
        }

        return dto;
    }
}