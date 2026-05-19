package com.digitalcow.health.vaccination.dto;

import com.digitalcow.catalog.vaccine.VaccineRoute;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Vacunacion individual. Requiere animalId. */
public record VaccinationCreateDto(
    @NotNull Long animalId,
    @NotNull Long vaccineId,
    @NotNull LocalDate appliedAt,
    String batchNumber,
    BigDecimal doseMl,
    VaccineRoute route,
    BigDecimal cost,
    Long vetVisitId,
    String notes
) { }
