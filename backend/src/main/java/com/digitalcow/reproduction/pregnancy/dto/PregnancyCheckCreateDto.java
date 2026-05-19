package com.digitalcow.reproduction.pregnancy.dto;

import com.digitalcow.reproduction.pregnancy.PregnancyMethod;
import com.digitalcow.reproduction.pregnancy.PregnancyResult;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Create payload de PregnancyCheck. */
public record PregnancyCheckCreateDto(
    @NotNull Long animalId,
    Long serviceId,
    @NotNull LocalDate checkedAt,
    PregnancyMethod method,
    @NotNull PregnancyResult result,
    Short estimatedGestationDays,
    Long vetVisitId,
    Long checkedByUserId,
    String notes
) { }
