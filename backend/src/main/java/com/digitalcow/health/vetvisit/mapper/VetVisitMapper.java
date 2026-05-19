package com.digitalcow.health.vetvisit.mapper;

import com.digitalcow.health.vetvisit.VetVisit;
import com.digitalcow.health.vetvisit.dto.VetVisitCreateDto;
import com.digitalcow.health.vetvisit.dto.VetVisitResponseDto;
import com.digitalcow.health.vetvisit.dto.VetVisitUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de VetVisit. */
@Mapper(componentModel = "spring")
public interface VetVisitMapper {

    VetVisitResponseDto toDto(VetVisit entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    VetVisit fromCreate(VetVisitCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void applyUpdate(VetVisitUpdateDto dto, @MappingTarget VetVisit entity);
}
