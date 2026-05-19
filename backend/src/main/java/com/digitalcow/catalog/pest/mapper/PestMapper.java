package com.digitalcow.catalog.pest.mapper;

import com.digitalcow.catalog.pest.Pest;
import com.digitalcow.catalog.pest.dto.PestDto;
import org.mapstruct.Mapper;

/** Mapper MapStruct para Pest. */
@Mapper(componentModel = "spring")
public interface PestMapper {
    PestDto toDto(Pest entity);
}
