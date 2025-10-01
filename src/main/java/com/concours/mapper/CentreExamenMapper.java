package com.concours.mapper;

import com.concours.dto.CentreExamenDTO;
import com.concours.entity.CentreExamen;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CentreExamenMapper {

    @Mapping(source = "ville.id", target = "villeId")
    @Mapping(source = "ville.nom", target = "villeNom")
    @Mapping(target = "specialites", source = "specialites")
    CentreExamenDTO toDTO(CentreExamen centreExamen);

    @Mapping(target = "ville", ignore = true)
    @Mapping(target = "specialites", source = "specialites")
    CentreExamen toEntity(CentreExamenDTO centreExamenDTO);
}