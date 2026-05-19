package com.digitalcow.reproduction.pregnancy.mapper;

import com.digitalcow.reproduction.pregnancy.PregnancyCheck;
import com.digitalcow.reproduction.pregnancy.dto.PregnancyCheckCreateDto;
import com.digitalcow.reproduction.pregnancy.dto.PregnancyCheckResponseDto;
import com.digitalcow.reproduction.pregnancy.dto.PregnancyCheckUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de PregnancyCheck. */
@Mapper(componentModel = "spring")
public interface PregnancyCheckMapper {

    PregnancyCheckResponseDto toDto(PregnancyCheck entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "estimatedCalvingDate", ignore = true)
    PregnancyCheck fromCreate(PregnancyCheckCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "animalId", ignore = true)
    @Mapping(target = "serviceId", ignore = true)
    @Mapping(target = "checkedByUserId", ignore = true)
    void applyUpdate(PregnancyCheckUpdateDto dto, @MappingTarget PregnancyCheck entity);
}
