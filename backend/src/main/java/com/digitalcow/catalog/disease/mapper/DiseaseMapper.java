package com.digitalcow.catalog.disease.mapper;

import com.digitalcow.catalog.disease.Disease;
import com.digitalcow.catalog.disease.dto.DiseaseDto;
import org.mapstruct.Mapper;

/** Mapper MapStruct para Disease. */
@Mapper(componentModel = "spring")
public interface DiseaseMapper {
    DiseaseDto toDto(Disease entity);
}
