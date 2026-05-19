package com.digitalcow.reproduction.pregnancy.dto;

import com.digitalcow.reproduction.pregnancy.PregnancyMethod;
import com.digitalcow.reproduction.pregnancy.PregnancyResult;

import java.time.LocalDate;

/** Update parcial de PregnancyCheck. */
public record PregnancyCheckUpdateDto(
    LocalDate checkedAt,
    PregnancyMethod method,
    PregnancyResult result,
    Short estimatedGestationDays,
    LocalDate estimatedCalvingDate,
    Long vetVisitId,
    String notes
) { }
