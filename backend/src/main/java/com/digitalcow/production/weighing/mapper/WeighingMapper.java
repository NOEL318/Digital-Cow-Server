package com.digitalcow.production.weighing.mapper;

import com.digitalcow.production.weighing.Weighing;
import com.digitalcow.production.weighing.dto.WeighingCreateDto;
import com.digitalcow.production.weighing.dto.WeighingResponseDto;
import com.digitalcow.production.weighing.dto.WeighingUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de Weighing. */
@Mapper(componentModel = "spring")
public interface WeighingMapper {

    WeighingResponseDto toDto(Weighing entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    Weighing fromCreate(WeighingCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "animalId", ignore = true)
    @Mapping(target = "weighedByUserId", ignore = true)
    void applyUpdate(WeighingUpdateDto dto, @MappingTarget Weighing entity);
}
