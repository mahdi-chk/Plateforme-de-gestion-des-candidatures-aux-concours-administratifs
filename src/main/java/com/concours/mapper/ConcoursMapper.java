package com.concours.mapper;

import com.concours.dto.ConcoursDTO;
import com.concours.entity.Concours;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConcoursMapper {

    @Mapping(target = "organisateurs", ignore = true)
    @Mapping(target = "centresExamen", ignore = true)
    ConcoursDTO toDTO(Concours concours);

    @Mapping(target = "organisateurs", ignore = true)
    @Mapping(target = "candidatures", ignore = true)
    @Mapping(target = "centresExamen", ignore = true)
    Concours toEntity(ConcoursDTO concoursDTO);
}