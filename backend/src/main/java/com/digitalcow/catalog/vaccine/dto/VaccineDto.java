package com.digitalcow.catalog.vaccine.dto;

import com.digitalcow.catalog.vaccine.VaccineRoute;

import java.math.BigDecimal;

/** DTO publico de Vaccine. Lleva nameEs y nameEn; el frontend elige por locale. */
public record VaccineDto(
    Long id,
    String code,
    String nameEs,
    String nameEn,
    String targetDiseases,
    BigDecimal defaultDoseMl,
    VaccineRoute route,
    Short recommendedAgeMonths,
    Short recommendedFrequencyMonths
) { }
