package com.digitalcow.reproduction.abortion.mapper;

import com.digitalcow.reproduction.abortion.Abortion;
import com.digitalcow.reproduction.abortion.dto.AbortionCreateDto;
import com.digitalcow.reproduction.abortion.dto.AbortionResponseDto;
import com.digitalcow.reproduction.abortion.dto.AbortionUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de Abortion. */
@Mapper(componentModel = "spring")
public interface AbortionMapper {

    AbortionResponseDto toDto(Abortion entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    Abortion fromCreate(AbortionCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "animalId", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    void applyUpdate(AbortionUpdateDto dto, @MappingTarget Abortion entity);
}
