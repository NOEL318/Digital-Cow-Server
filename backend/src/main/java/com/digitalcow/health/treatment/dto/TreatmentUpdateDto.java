package com.digitalcow.health.treatment.dto;

import com.digitalcow.health.treatment.TreatmentRoute;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Update parcial de Treatment. Las fechas de retiro se recalculan en el service. */
public record TreatmentUpdateDto(
    LocalDate startedAt,
    LocalDate endedAt,
    String dose,
    Short dosesCount,
    TreatmentRoute route,
    BigDecimal cost,
    String prescribedBy,
    String notes
) { }
