package com.digitalcow.reproduction.dryoff.dto;

import java.time.LocalDate;

/** Update parcial de DryOff. */
public record DryOffUpdateDto(
    LocalDate driedOffAt,
    Short lactationDays,
    String notes
) { }
