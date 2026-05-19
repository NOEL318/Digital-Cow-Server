package com.digitalcow.health.treatment.dto;

import com.digitalcow.health.treatment.TreatmentRoute;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Response DTO de Treatment con nombres del catalogo expandidos. */
public record TreatmentResponseDto(
    Long id,
    Long animalId,
    Long diagnosisId,
    Long medicationId,
    String medicationNameEs,
    String medicationNameEn,
    LocalDate startedAt,
    LocalDate endedAt,
    String dose,
    Short dosesCount,
    TreatmentRoute route,
    LocalDate withdrawalMilkUntil,
    LocalDate withdrawalMeatUntil,
    BigDecimal cost,
    String prescribedBy,
    Long vetVisitId,
    String notes
) { }
