package com.digitalcow.reproduction.weaning.mapper;

import com.digitalcow.reproduction.weaning.Weaning;
import com.digitalcow.reproduction.weaning.dto.WeaningCreateDto;
import com.digitalcow.reproduction.weaning.dto.WeaningResponseDto;
import com.digitalcow.reproduction.weaning.dto.WeaningUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de Weaning. */
@Mapper(componentModel = "spring")
public interface WeaningMapper {

    WeaningResponseDto toDto(Weaning entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    Weaning fromCreate(WeaningCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "animalId", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    void applyUpdate(WeaningUpdateDto dto, @MappingTarget Weaning entity);
}
