package com.digitalcow.reproduction.heat.mapper;

import com.digitalcow.reproduction.heat.Heat;
import com.digitalcow.reproduction.heat.dto.HeatCreateDto;
import com.digitalcow.reproduction.heat.dto.HeatResponseDto;
import com.digitalcow.reproduction.heat.dto.HeatUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de Heat. */
@Mapper(componentModel = "spring")
public interface HeatMapper {

    HeatResponseDto toDto(Heat entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    Heat fromCreate(HeatCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    void applyUpdate(HeatUpdateDto dto, @MappingTarget Heat entity);
}
