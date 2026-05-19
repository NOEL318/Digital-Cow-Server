package com.digitalcow.finance.category.mapper;

import com.digitalcow.finance.category.IncomeCategory;
import com.digitalcow.finance.category.dto.IncomeCategoryCreateDto;
import com.digitalcow.finance.category.dto.IncomeCategoryResponseDto;
import com.digitalcow.finance.category.dto.IncomeCategoryUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de IncomeCategory. */
@Mapper(componentModel = "spring")
public interface IncomeCategoryMapper {

    @Mapping(target = "isGlobal", expression = "java(entity.getAccountId() == null)")
    IncomeCategoryResponseDto toDto(IncomeCategory entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    IncomeCategory fromCreate(IncomeCategoryCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "code", ignore = true)
    void applyUpdate(IncomeCategoryUpdateDto dto, @MappingTarget IncomeCategory entity);
}
