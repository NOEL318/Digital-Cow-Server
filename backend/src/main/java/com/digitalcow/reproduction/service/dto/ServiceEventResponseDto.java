package com.digitalcow.reproduction.service.dto;

import com.digitalcow.reproduction.service.ServiceType;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Response DTO de ServiceEvent. */
public record ServiceEventResponseDto(
    Long id,
    Long animalId,
    ServiceType serviceType,
    LocalDate serviceDate,
    Long bullId,
    Long semenStrawId,
    String technicianName,
    Long heatId,
    BigDecimal cost,
    String notes,
    Long createdByUserId
) { }
