package com.digitalcow.health.alerts.dto;

import java.util.List;

/** Conjunto de alertas sanitarias del tenant. */
public record HealthAlertsDto(
    List<AlertItemDto> upcomingVaccinations7d,
    List<AlertItemDto> upcomingVaccinations30d,
    List<AlertItemDto> withdrawalActiveMilk,
    List<AlertItemDto> withdrawalActiveMeat,
    List<AlertItemDto> activeDiagnosesWithoutTreatment
) { }
