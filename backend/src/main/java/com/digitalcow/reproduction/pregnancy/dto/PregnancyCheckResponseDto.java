package com.digitalcow.reproduction.pregnancy.dto;

import com.digitalcow.reproduction.pregnancy.PregnancyMethod;
import com.digitalcow.reproduction.pregnancy.PregnancyResult;

import java.time.LocalDate;

/** Response DTO de PregnancyCheck. */
public record PregnancyCheckResponseDto(
    Long id,
    Long animalId,
    Long serviceId,
    LocalDate checkedAt,
    PregnancyMethod method,
    PregnancyResult result,
    Short estimatedGestationDays,
    LocalDate estimatedCalvingDate,
    Long vetVisitId,
    Long checkedByUserId,
    String notes
) { }
