package com.digitalcow.finance.animalsale.mapper;

import com.digitalcow.finance.animalsale.AnimalSale;
import com.digitalcow.finance.animalsale.dto.AnimalSaleCreateDto;
import com.digitalcow.finance.animalsale.dto.AnimalSaleResponseDto;
import com.digitalcow.finance.animalsale.dto.AnimalSaleUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de AnimalSale. */
@Mapper(componentModel = "spring")
public interface AnimalSaleMapper {

    AnimalSaleResponseDto toDto(AnimalSale entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    AnimalSale fromCreate(AnimalSaleCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "animalId", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    void applyUpdate(AnimalSaleUpdateDto dto, @MappingTarget AnimalSale entity);
}
