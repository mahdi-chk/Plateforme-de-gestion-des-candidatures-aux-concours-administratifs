package com.concours.mapper;

import com.concours.dto.SpecialiteDTO;
import com.concours.entity.Specialite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SpecialiteMapper {

    SpecialiteDTO toDTO(Specialite specialite);

    Specialite toEntity(SpecialiteDTO specialiteDTO);
}