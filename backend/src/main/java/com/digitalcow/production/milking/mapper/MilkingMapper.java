package com.digitalcow.production.milking.mapper;

import com.digitalcow.production.milking.Milking;
import com.digitalcow.production.milking.dto.MilkingCreateDto;
import com.digitalcow.production.milking.dto.MilkingResponseDto;
import com.digitalcow.production.milking.dto.MilkingUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de Milking. */
@Mapper(componentModel = "spring")
public interface MilkingMapper {

    MilkingResponseDto toDto(Milking entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    Milking fromCreate(MilkingCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "animalId", ignore = true)
    @Mapping(target = "recordedByUserId", ignore = true)
    void applyUpdate(MilkingUpdateDto dto, @MappingTarget Milking entity);
}
