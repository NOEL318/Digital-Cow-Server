package com.digitalcow.health.vaccination.dto;

import com.digitalcow.catalog.vaccine.VaccineRoute;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Update parcial de Vaccination. */
public record VaccinationUpdateDto(
    LocalDate appliedAt,
    String batchNumber,
    BigDecimal doseMl,
    VaccineRoute route,
    LocalDate nextDoseDue,
    BigDecimal cost,
    String notes
) { }
