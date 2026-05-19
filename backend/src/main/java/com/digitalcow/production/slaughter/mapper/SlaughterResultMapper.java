package com.digitalcow.production.slaughter.mapper;

import com.digitalcow.production.slaughter.SlaughterResult;
import com.digitalcow.production.slaughter.dto.SlaughterResultCreateDto;
import com.digitalcow.production.slaughter.dto.SlaughterResultResponseDto;
import com.digitalcow.production.slaughter.dto.SlaughterResultUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de SlaughterResult. */
@Mapper(componentModel = "spring")
public interface SlaughterResultMapper {

    SlaughterResultResponseDto toDto(SlaughterResult entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    SlaughterResult fromCreate(SlaughterResultCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "animalId", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    void applyUpdate(SlaughterResultUpdateDto dto, @MappingTarget SlaughterResult entity);
}
