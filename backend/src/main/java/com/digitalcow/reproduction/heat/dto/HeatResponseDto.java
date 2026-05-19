package com.digitalcow.reproduction.heat.dto;

import com.digitalcow.reproduction.heat.DetectionMethod;
import com.digitalcow.reproduction.heat.HeatIntensity;

import java.time.Instant;

/** Response DTO de Heat. */
public record HeatResponseDto(
    Long id,
    Long animalId,
    Instant detectedAt,
    DetectionMethod detectionMethod,
    HeatIntensity intensity,
    String notes,
    Long detectedByUserId
) { }
