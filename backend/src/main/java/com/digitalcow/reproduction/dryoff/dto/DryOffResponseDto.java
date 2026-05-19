package com.digitalcow.reproduction.dryoff.dto;

import java.time.LocalDate;

/** Response DTO de DryOff. */
public record DryOffResponseDto(
    Long id,
    Long animalId,
    LocalDate driedOffAt,
    Short lactationDays,
    String notes,
    Long createdByUserId
) { }
