package com.digitalcow.catalog.vaccine.mapper;

import com.digitalcow.catalog.vaccine.Vaccine;
import com.digitalcow.catalog.vaccine.dto.VaccineDto;
import org.mapstruct.Mapper;

/** Mapper MapStruct para Vaccine. */
@Mapper(componentModel = "spring")
public interface VaccineMapper {
    VaccineDto toDto(Vaccine entity);
}
