package com.digitalcow.health.vaccination.dto;

import com.digitalcow.catalog.vaccine.VaccineRoute;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Vacunacion masiva a un lote completo. Expande una fila por animal activo. */
public record VaccinationBulkDto(
    @NotNull Long lotId,
    @NotNull Long vaccineId,
    @NotNull LocalDate appliedAt,
    String batchNumber,
    BigDecimal doseMl,
    VaccineRoute route,
    BigDecimal cost,
    Long vetVisitId,
    String notes
) { }
