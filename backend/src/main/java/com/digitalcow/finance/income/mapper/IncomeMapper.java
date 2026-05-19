package com.digitalcow.finance.income.mapper;

import com.digitalcow.finance.income.Income;
import com.digitalcow.finance.income.dto.IncomeCreateDto;
import com.digitalcow.finance.income.dto.IncomeResponseDto;
import com.digitalcow.finance.income.dto.IncomeUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de Income. */
@Mapper(componentModel = "spring")
public interface IncomeMapper {

    IncomeResponseDto toDto(Income entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    @Mapping(target = "sourceType", ignore = true)
    @Mapping(target = "sourceId", ignore = true)
    Income fromCreate(IncomeCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    @Mapping(target = "sourceType", ignore = true)
    @Mapping(target = "sourceId", ignore = true)
    void applyUpdate(IncomeUpdateDto dto, @MappingTarget Income entity);
}
