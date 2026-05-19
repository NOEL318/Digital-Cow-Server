package com.digitalcow.reproduction.calving.dto;

import com.digitalcow.animal.Sex;
import com.digitalcow.reproduction.calving.CalvingEase;
import com.digitalcow.reproduction.calving.CalvingOutcome;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Update parcial de Calving. */
public record CalvingUpdateDto(
    LocalDate calvedAt,
    CalvingEase ease,
    CalvingOutcome outcome,
    Sex calfSex,
    BigDecimal calfBirthWeightKg,
    Long pregnancyCheckId,
    String notes
) { }
