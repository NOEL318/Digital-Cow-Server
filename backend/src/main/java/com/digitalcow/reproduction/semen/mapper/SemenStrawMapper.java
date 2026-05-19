package com.digitalcow.reproduction.semen.mapper;

import com.digitalcow.reproduction.semen.SemenStraw;
import com.digitalcow.reproduction.semen.dto.SemenStrawCreateDto;
import com.digitalcow.reproduction.semen.dto.SemenStrawResponseDto;
import com.digitalcow.reproduction.semen.dto.SemenStrawUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de SemenStraw. */
@Mapper(componentModel = "spring")
public interface SemenStrawMapper {

    SemenStrawResponseDto toDto(SemenStraw entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    SemenStraw fromCreate(SemenStrawCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    void applyUpdate(SemenStrawUpdateDto dto, @MappingTarget SemenStraw entity);
}
