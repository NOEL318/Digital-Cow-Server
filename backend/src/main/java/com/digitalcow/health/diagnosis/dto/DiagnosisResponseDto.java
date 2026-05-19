package com.digitalcow.health.diagnosis.dto;

import com.digitalcow.catalog.disease.DiseaseSeverity;
import com.digitalcow.health.diagnosis.DiagnosisStatus;

import java.time.LocalDate;

/** Response DTO de Diagnosis con nombres del catalogo expandidos. */
public record DiagnosisResponseDto(
    Long id,
    Long animalId,
    Long diseaseId,
    String diseaseNameEs,
    String diseaseNameEn,
    LocalDate diagnosedAt,
    DiseaseSeverity severity,
    String symptoms,
    DiagnosisStatus status,
    LocalDate resolvedAt,
    Long vetVisitId,
    String notes
) { }
