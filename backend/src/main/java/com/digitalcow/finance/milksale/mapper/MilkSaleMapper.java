package com.digitalcow.finance.milksale.mapper;

import com.digitalcow.finance.milksale.MilkSale;
import com.digitalcow.finance.milksale.dto.MilkSaleCreateDto;
import com.digitalcow.finance.milksale.dto.MilkSaleResponseDto;
import com.digitalcow.finance.milksale.dto.MilkSaleUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de MilkSale. */
@Mapper(componentModel = "spring")
public interface MilkSaleMapper {

    MilkSaleResponseDto toDto(MilkSale entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    MilkSale fromCreate(MilkSaleCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    void applyUpdate(MilkSaleUpdateDto dto, @MappingTarget MilkSale entity);
}
