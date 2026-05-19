package com.digitalcow.health.vaccination.dto;

import com.digitalcow.catalog.vaccine.VaccineRoute;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Response DTO de Vaccination con nombres del catalogo expandidos. */
public record VaccinationResponseDto(
    Long id,
    Long animalId,
    Long lotId,
    Long vaccineId,
    String vaccineNameEs,
    String vaccineNameEn,
    String batchNumber,
    LocalDate appliedAt,
    BigDecimal doseMl,
    VaccineRoute route,
    LocalDate nextDoseDue,
    BigDecimal cost,
    Long vetVisitId,
    String notes
) { }
