package com.digitalcow.reproduction.bull.mapper;

import com.digitalcow.reproduction.bull.Bull;
import com.digitalcow.reproduction.bull.dto.BullCreateDto;
import com.digitalcow.reproduction.bull.dto.BullResponseDto;
import com.digitalcow.reproduction.bull.dto.BullUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de Bull. */
@Mapper(componentModel = "spring")
public interface BullMapper {

    BullResponseDto toDto(Bull entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    Bull fromCreate(BullCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    void applyUpdate(BullUpdateDto dto, @MappingTarget Bull entity);
}
