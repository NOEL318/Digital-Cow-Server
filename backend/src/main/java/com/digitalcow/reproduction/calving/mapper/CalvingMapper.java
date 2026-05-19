package com.digitalcow.reproduction.calving.mapper;

import com.digitalcow.reproduction.calving.Calving;
import com.digitalcow.reproduction.calving.dto.CalvingCreateDto;
import com.digitalcow.reproduction.calving.dto.CalvingResponseDto;
import com.digitalcow.reproduction.calving.dto.CalvingUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de Calving. */
@Mapper(componentModel = "spring")
public interface CalvingMapper {

    CalvingResponseDto toDto(Calving entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "calfAnimalId", ignore = true)
    Calving fromCreate(CalvingCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "animalId", ignore = true)
    @Mapping(target = "calfAnimalId", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    void applyUpdate(CalvingUpdateDto dto, @MappingTarget Calving entity);
}
