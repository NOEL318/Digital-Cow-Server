package com.digitalcow.health.treatment.dto;

import com.digitalcow.health.treatment.TreatmentRoute;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Create payload de Treatment. */
public record TreatmentCreateDto(
    @NotNull Long animalId,
    Long diagnosisId,
    @NotNull Long medicationId,
    @NotNull LocalDate startedAt,
    LocalDate endedAt,
    String dose,
    Short dosesCount,
    TreatmentRoute route,
    BigDecimal cost,
    String prescribedBy,
    Long vetVisitId,
    String notes
) { }
