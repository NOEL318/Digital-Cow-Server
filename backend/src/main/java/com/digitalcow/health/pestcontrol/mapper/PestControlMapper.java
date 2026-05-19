package com.digitalcow.health.pestcontrol.mapper;

import com.digitalcow.catalog.pest.Pest;
import com.digitalcow.health.pestcontrol.PestControl;
import com.digitalcow.health.pestcontrol.dto.PestControlCreateDto;
import com.digitalcow.health.pestcontrol.dto.PestControlResponseDto;
import com.digitalcow.health.pestcontrol.dto.PestControlUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de PestControl con join al catalogo de plagas. */
@Mapper(componentModel = "spring")
public interface PestControlMapper {

    @Mapping(target = "pestNameEs", source = "pest.nameEs")
    @Mapping(target = "pestNameEn", source = "pest.nameEn")
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "ranchId", source = "entity.ranchId")
    @Mapping(target = "lotId", source = "entity.lotId")
    @Mapping(target = "pestId", source = "entity.pestId")
    @Mapping(target = "productUsed", source = "entity.productUsed")
    @Mapping(target = "dose", source = "entity.dose")
    @Mapping(target = "appliedAt", source = "entity.appliedAt")
    @Mapping(target = "nextApplicationAt", source = "entity.nextApplicationAt")
    @Mapping(target = "cost", source = "entity.cost")
    @Mapping(target = "notes", source = "entity.notes")
    PestControlResponseDto toDto(PestControl entity, Pest pest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "appliedByUserId", ignore = true)
    PestControl fromCreate(PestControlCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void applyUpdate(PestControlUpdateDto dto, @MappingTarget PestControl entity);
}
