package com.digitalcow.reproduction.service.dto;

import com.digitalcow.reproduction.service.ServiceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Create payload de ServiceEvent. */
public record ServiceEventCreateDto(
    @NotNull Long animalId,
    @NotNull ServiceType serviceType,
    @NotNull LocalDate serviceDate,
    Long bullId,
    Long semenStrawId,
    @Size(max = 160) String technicianName,
    Long heatId,
    BigDecimal cost,
    String notes
) { }
