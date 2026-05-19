package com.digitalcow.reproduction.service.dto;

import com.digitalcow.reproduction.service.ServiceType;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Update parcial de ServiceEvent. */
public record ServiceEventUpdateDto(
    ServiceType serviceType,
    LocalDate serviceDate,
    Long bullId,
    Long semenStrawId,
    @Size(max = 160) String technicianName,
    Long heatId,
    BigDecimal cost,
    String notes
) { }
