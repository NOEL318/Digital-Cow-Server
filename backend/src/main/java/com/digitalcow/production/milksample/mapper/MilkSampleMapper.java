package com.digitalcow.production.milksample.mapper;

import com.digitalcow.production.milksample.MilkSample;
import com.digitalcow.production.milksample.dto.MilkSampleCreateDto;
import com.digitalcow.production.milksample.dto.MilkSampleResponseDto;
import com.digitalcow.production.milksample.dto.MilkSampleUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de MilkSample. */
@Mapper(componentModel = "spring")
public interface MilkSampleMapper {

    MilkSampleResponseDto toDto(MilkSample entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    MilkSample fromCreate(MilkSampleCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "animalId", ignore = true)
    void applyUpdate(MilkSampleUpdateDto dto, @MappingTarget MilkSample entity);
}
