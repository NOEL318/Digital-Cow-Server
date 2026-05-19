package com.digitalcow.catalog.medication.dto;

import com.digitalcow.catalog.medication.MedicationCategory;
import com.digitalcow.catalog.medication.MedicationRoute;

import java.time.LocalDate;

/** DTO publico de Medication. */
public record MedicationDto(
    Long id,
    Long accountId,
    String code,
    String nameEs,
    String nameEn,
    String activeIngredient,
    String manufacturer,
    String presentation,
    String barcode,
    LocalDate expiresAt,
    MedicationCategory category,
    String defaultDose,
    MedicationRoute defaultRoute,
    short withdrawalMilkDays,
    short withdrawalMeatDays,
    String notes
) { }
