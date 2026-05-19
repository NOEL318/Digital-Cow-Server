package com.digitalcow.reproduction.calving.dto;

import com.digitalcow.animal.Sex;
import com.digitalcow.reproduction.calving.CalvingEase;
import com.digitalcow.reproduction.calving.CalvingOutcome;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Response DTO de Calving. */
public record CalvingResponseDto(
    Long id,
    Long animalId,
    LocalDate calvedAt,
    CalvingEase ease,
    CalvingOutcome outcome,
    Long calfAnimalId,
    Sex calfSex,
    BigDecimal calfBirthWeightKg,
    Long pregnancyCheckId,
    String notes,
    Long createdByUserId
) { }
