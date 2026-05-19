package com.digitalcow.reproduction.dryoff.mapper;

import com.digitalcow.reproduction.dryoff.DryOff;
import com.digitalcow.reproduction.dryoff.dto.DryOffCreateDto;
import com.digitalcow.reproduction.dryoff.dto.DryOffResponseDto;
import com.digitalcow.reproduction.dryoff.dto.DryOffUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de DryOff. */
@Mapper(componentModel = "spring")
public interface DryOffMapper {

    DryOffResponseDto toDto(DryOff entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    DryOff fromCreate(DryOffCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "animalId", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    void applyUpdate(DryOffUpdateDto dto, @MappingTarget DryOff entity);
}
