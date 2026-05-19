package com.digitalcow.finance.category.mapper;

import com.digitalcow.finance.category.ExpenseCategory;
import com.digitalcow.finance.category.dto.ExpenseCategoryCreateDto;
import com.digitalcow.finance.category.dto.ExpenseCategoryResponseDto;
import com.digitalcow.finance.category.dto.ExpenseCategoryUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de ExpenseCategory. */
@Mapper(componentModel = "spring")
public interface ExpenseCategoryMapper {

    @Mapping(target = "isGlobal", expression = "java(entity.getAccountId() == null)")
    ExpenseCategoryResponseDto toDto(ExpenseCategory entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    ExpenseCategory fromCreate(ExpenseCategoryCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "code", ignore = true)
    void applyUpdate(ExpenseCategoryUpdateDto dto, @MappingTarget ExpenseCategory entity);
}
