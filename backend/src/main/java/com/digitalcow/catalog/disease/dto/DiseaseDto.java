package com.digitalcow.catalog.disease.dto;

import com.digitalcow.catalog.disease.DiseaseCategory;
import com.digitalcow.catalog.disease.DiseaseSeverity;

/** DTO publico de Disease. */
public record DiseaseDto(
    Long id,
    String code,
    String nameEs,
    String nameEn,
    DiseaseCategory category,
    boolean zoonotic,
    DiseaseSeverity severity,
    String defaultSymptoms
) { }
