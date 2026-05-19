package com.digitalcow.catalog.pest.dto;

import com.digitalcow.catalog.pest.PestRegion;
import com.digitalcow.catalog.pest.PestType;

/** DTO publico de Pest. */
public record PestDto(
    Long id,
    String code,
    String nameEs,
    String nameEn,
    String scientificName,
    PestType type,
    PestRegion region,
    String notes
) { }
