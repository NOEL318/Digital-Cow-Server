package com.digitalcow.reproduction.service.mapper;

import com.digitalcow.reproduction.service.ServiceEvent;
import com.digitalcow.reproduction.service.dto.ServiceEventCreateDto;
import com.digitalcow.reproduction.service.dto.ServiceEventResponseDto;
import com.digitalcow.reproduction.service.dto.ServiceEventUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de ServiceEvent. */
@Mapper(componentModel = "spring")
public interface ServiceEventMapper {

    ServiceEventResponseDto toDto(ServiceEvent entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    ServiceEvent fromCreate(ServiceEventCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    void applyUpdate(ServiceEventUpdateDto dto, @MappingTarget ServiceEvent entity);
}
