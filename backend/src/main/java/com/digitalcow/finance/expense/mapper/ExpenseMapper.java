package com.digitalcow.finance.expense.mapper;

import com.digitalcow.finance.expense.Expense;
import com.digitalcow.finance.expense.dto.ExpenseCreateDto;
import com.digitalcow.finance.expense.dto.ExpenseResponseDto;
import com.digitalcow.finance.expense.dto.ExpenseUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de Expense. */
@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    ExpenseResponseDto toDto(Expense entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    Expense fromCreate(ExpenseCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    void applyUpdate(ExpenseUpdateDto dto, @MappingTarget Expense entity);
}
