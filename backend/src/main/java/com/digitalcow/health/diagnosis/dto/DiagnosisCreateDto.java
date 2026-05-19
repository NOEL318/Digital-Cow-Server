package com.digitalcow.health.diagnosis.dto;

import com.digitalcow.catalog.disease.DiseaseSeverity;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Create payload de Diagnosis. */
public record DiagnosisCreateDto(
    @NotNull Long animalId,
    @NotNull Long diseaseId,
    @NotNull LocalDate diagnosedAt,
    DiseaseSeverity severity,
    String symptoms,
    Long vetVisitId,
    String notes
) { }
