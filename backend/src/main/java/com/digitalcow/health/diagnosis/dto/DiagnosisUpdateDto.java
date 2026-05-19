package com.digitalcow.health.diagnosis.dto;

import com.digitalcow.catalog.disease.DiseaseSeverity;
import com.digitalcow.health.diagnosis.DiagnosisStatus;

import java.time.LocalDate;

/** Update parcial de Diagnosis. Status puede cambiar; resolvedAt se autocalcula si queda en null. */
public record DiagnosisUpdateDto(
    LocalDate diagnosedAt,
    DiseaseSeverity severity,
    String symptoms,
    DiagnosisStatus status,
    LocalDate resolvedAt,
    String notes
) { }
