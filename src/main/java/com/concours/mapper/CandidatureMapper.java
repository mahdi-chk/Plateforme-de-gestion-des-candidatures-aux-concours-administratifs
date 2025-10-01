package com.concours.mapper;

import com.concours.dto.CandidatureDTO;
import com.concours.dto.CandidatureCreateDTO;
import com.concours.entity.Candidat;
import com.concours.entity.Candidature;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {DocumentMapper.class})
public interface CandidatureMapper {

    @Mapping(source = "statut", target = "statut")
    @Mapping(source = "candidat.nom", target = "candidatNom")
    @Mapping(source = "candidat.prenom", target = "candidatPrenom")
    @Mapping(source = "candidat.cin", target = "candidatCin")
    @Mapping(source = "candidat.email", target = "candidatEmail")
    @Mapping(source = "candidat.diplome", target = "candidatDiplome")
    @Mapping(source = "concours.titre", target = "concoursTitre")
    @Mapping(source = "concours.reference", target = "concoursReference")
    @Mapping(source = "specialite.libelle", target = "specialiteLibelle")
    @Mapping(source = "centreExamen.code", target = "centreCode")
    @Mapping(source = "centreExamen.ville.nom", target = "centreVille")
    @Mapping(source = "utilisateurTraitant.username", target = "utilisateurTraitant")
    CandidatureDTO toDTO(Candidature candidature);

    Candidat toCandidat(CandidatureCreateDTO candidatureDTO);

    default CandidatureDTO mapToDTO(Candidature candidature) {
        CandidatureDTO dto = new CandidatureDTO();

        // Informations de base
        dto.setNumero(candidature.getNumero());
        dto.setStatut(candidature.getStatut());
        dto.setDateDepot(candidature.getDateDepot());
        dto.setNotifications(candidature.getNotifications());
        dto.setAccepter(candidature.isAccepter());

        // Informations candidat
        if (candidature.getCandidat() != null) {
            Candidat candidat = candidature.getCandidat();
            dto.setCandidatNom(candidat.getNom());
            dto.setCandidatPrenom(candidat.getPrenom());
            dto.setCandidatCin(candidat.getCin());
            dto.setCandidatEmail(candidat.getEmail());
            dto.setCandidatTelephone(candidat.getTelephone());
            dto.setCandidatDiplome(candidat.getDiplome());
            dto.setCandidatNiveauEtude(candidat.getNiveauEtude());
            dto.setCandidatExperience(candidat.getExperience());
            dto.setCandidatAdresse(candidat.getAdresse());
            dto.setCandidatDateNaissance(candidat.getDateNaissance());

            if (candidat.getLieuNaissance() != null) {
                dto.setCandidatLieuNaissance(candidat.getLieuNaissance().getNom());
            }
        }

        // Informations concours
        if (candidature.getConcours() != null) {
            dto.setConcoursTitre(candidature.getConcours().getTitre());
            dto.setConcoursReference(candidature.getConcours().getReference());
            dto.setConcoursId(candidature.getConcours().getId());
        }

        // Informations spécialité
        if (candidature.getSpecialite() != null) {
            dto.setSpecialiteLibelle(candidature.getSpecialite().getLibelle());
            dto.setSpecialiteCode(candidature.getSpecialite().getCode());
            dto.setSpecialiteId(candidature.getSpecialite().getId());
        }

        // Informations centre
        if (candidature.getCentreExamen() != null) {
            dto.setCentreCode(candidature.getCentreExamen().getCode());
            dto.setCentreId(candidature.getCentreExamen().getId());

            if (candidature.getCentreExamen().getVille() != null) {
                dto.setCentreVille(candidature.getCentreExamen().getVille().getNom());
            }
        }

        // Utilisateur traitant
        if (candidature.getUtilisateurTraitant() != null) {
            dto.setUtilisateurTraitant(candidature.getUtilisateurTraitant().getUsername());
            dto.setUtilisateurTraitantId(candidature.getUtilisateurTraitant().getId());
        }

        return dto;
    }
}