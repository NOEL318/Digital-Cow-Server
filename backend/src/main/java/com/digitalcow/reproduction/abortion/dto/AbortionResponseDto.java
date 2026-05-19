package com.digitalcow.reproduction.abortion.dto;

import java.time.LocalDate;

/** Response DTO de Abortion. */
public record AbortionResponseDto(
    Long id,
    Long animalId,
    LocalDate abortedAt,
    Short estimatedGestationDays,
    String cause,
    Long pregnancyCheckId,
    String notes,
    Long createdByUserId
) { }
