package com.digitalcow.production.bulktank.mapper;

import com.digitalcow.production.bulktank.BulkTankDelivery;
import com.digitalcow.production.bulktank.dto.BulkTankDeliveryCreateDto;
import com.digitalcow.production.bulktank.dto.BulkTankDeliveryResponseDto;
import com.digitalcow.production.bulktank.dto.BulkTankDeliveryUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de BulkTankDelivery. */
@Mapper(componentModel = "spring")
public interface BulkTankDeliveryMapper {

    BulkTankDeliveryResponseDto toDto(BulkTankDelivery entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    BulkTankDelivery fromCreate(BulkTankDeliveryCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "ranchId", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    void applyUpdate(BulkTankDeliveryUpdateDto dto, @MappingTarget BulkTankDelivery entity);
}
