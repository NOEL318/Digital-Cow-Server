package com.digitalcow.reproduction.heat.dto;

import com.digitalcow.reproduction.heat.DetectionMethod;
import com.digitalcow.reproduction.heat.HeatIntensity;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/** Create payload de Heat. */
public record HeatCreateDto(
    @NotNull Long animalId,
    @NotNull Instant detectedAt,
    DetectionMethod detectionMethod,
    HeatIntensity intensity,
    String notes,
    Long detectedByUserId
) { }
