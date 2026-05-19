package com.digitalcow.reproduction.dryoff.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Create payload de DryOff. */
public record DryOffCreateDto(
    @NotNull Long animalId,
    @NotNull LocalDate driedOffAt,
    Short lactationDays,
    String notes,
    Long createdByUserId
) { }
