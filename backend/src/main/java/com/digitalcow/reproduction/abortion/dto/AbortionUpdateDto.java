package com.digitalcow.reproduction.abortion.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Update parcial de Abortion. */
public record AbortionUpdateDto(
    LocalDate abortedAt,
    Short estimatedGestationDays,
    @Size(max = 300) String cause,
    Long pregnancyCheckId,
    String notes
) { }
