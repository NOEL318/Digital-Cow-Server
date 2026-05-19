package com.digitalcow.reproduction.abortion.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Create payload de Abortion. */
public record AbortionCreateDto(
    @NotNull Long animalId,
    @NotNull LocalDate abortedAt,
    Short estimatedGestationDays,
    @Size(max = 300) String cause,
    Long pregnancyCheckId,
    String notes,
    Long createdByUserId
) { }
