package com.concours.mapper;

import com.concours.dto.ConcoursDTO;
import com.concours.dto.SpecialiteDTO;
import com.concours.entity.Concours;
import com.concours.entity.Specialite;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ConcoursMapperManual {

    public ConcoursDTO toDTO(Concours concours) {
        if (concours == null) {
            return null;
        }

        ConcoursDTO dto = new ConcoursDTO();
        dto.setId(concours.getId());
        dto.setReference(concours.getReference());
        dto.setTitre(concours.getTitre());
        dto.setDateOuverture(concours.getDateOuverture());
        dto.setDateCloture(concours.getDateCloture());
        dto.setDateConcours(concours.getDateConcours());
        dto.setNbPostes(concours.getNbPostes());
        dto.setConditions(concours.getConditions());
        dto.setPublie(concours.isPublie());

        // Spécialités
        if (concours.getSpecialites() != null) {
            dto.setSpecialites(concours.getSpecialites().stream()
                    .map(this::specialiteToDTO)
                    .collect(Collectors.toList()));
        }

        // Organisateurs
        if (concours.getOrganisateurs() != null) {
            dto.setOrganisateurs(concours.getOrganisateurs().stream()
                    .map(org -> org.getUsername())
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public Concours toEntity(ConcoursDTO dto) {
        if (dto == null) {
            return null;
        }

        Concours concours = new Concours();
        concours.setId(dto.getId());
        concours.setReference(dto.getReference());
        concours.setTitre(dto.getTitre());
        concours.setDateConcours(dto.getDateConcours());
        concours.setDateOuverture(dto.getDateOuverture());
        concours.setDateCloture(dto.getDateCloture());
        concours.setNbPostes(dto.getNbPostes());
        concours.setConditions(dto.getConditions());
        concours.setPublie(dto.isPublie());

        return concours;
    }

    private SpecialiteDTO specialiteToDTO(Specialite specialite) {
        if (specialite == null) {
            return null;
        }

        SpecialiteDTO dto = new SpecialiteDTO();
        dto.setId(specialite.getId());
        dto.setCode(specialite.getCode());
        dto.setLibelle(specialite.getLibelle());
        dto.setNbPostes(specialite.getNbPostes());

        return dto;
    }
}