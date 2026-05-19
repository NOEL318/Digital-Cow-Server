package com.digitalcow.reproduction.heat.dto;

import com.digitalcow.reproduction.heat.DetectionMethod;
import com.digitalcow.reproduction.heat.HeatIntensity;

import java.time.Instant;

/** Update parcial de Heat. */
public record HeatUpdateDto(
    Instant detectedAt,
    DetectionMethod detectionMethod,
    HeatIntensity intensity,
    String notes
) { }
