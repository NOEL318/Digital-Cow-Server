package com.digitalcow.catalog.medication.dto;

import com.digitalcow.catalog.medication.MedicationCategory;
import com.digitalcow.catalog.medication.MedicationRoute;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Payload de creacion o actualizacion de un medicamento propio del
 * tenant. El nombre es lo unico obligatorio; el code y el nameEn se
 * autogeneran a partir del nombre si no se mandan.
 */
public record MedicationUpsertRequest(
    @Size(max = 60) String code,
    @NotBlank @Size(max = 160) String nameEs,
    @Size(max = 160) String nameEn,
    @Size(max = 200) String activeIngredient,
    @Size(max = 160) String manufacturer,
    @Size(max = 160) String presentation,
    @Size(max = 40) String barcode,
    LocalDate expiresAt,
    MedicationCategory category,
    @Size(max = 120) String defaultDose,
    MedicationRoute defaultRoute,
    @Min(0) Short withdrawalMilkDays,
    @Min(0) Short withdrawalMeatDays,
    @Size(max = 400) String notes
) { }
