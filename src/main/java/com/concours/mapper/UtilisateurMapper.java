package com.concours.mapper;

import com.concours.dto.UtilisateurDTO;
import com.concours.entity.Utilisateur;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CentreExamenMapper.class})
public interface UtilisateurMapper {

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "specialitesGerees", ignore = true)
    @Mapping(target = "concoursOrganises", ignore = true)
    @Mapping(target = "centresAffectes", source = "centresAffectes")
    @Mapping(target = "lastLogin", source = "lastLogin")
    UtilisateurDTO toDTO(Utilisateur utilisateur);

    @Mapping(target = "specialitesGerees", ignore = true)
    @Mapping(target = "concoursOrganises", ignore = true)
    @Mapping(target = "centresAffectes", ignore = true)
    @Mapping(target = "lastLogin", source = "lastLogin")
    Utilisateur toEntity(UtilisateurDTO utilisateurDTO);
}