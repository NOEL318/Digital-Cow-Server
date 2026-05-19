package com.digitalcow.health.plan.dto;

import java.time.LocalDate;

/** Response DTO de la asignacion de un plan. */
public record AnimalHealthPlanDto(
    Long id,
    Long healthPlanId,
    Long animalId,
    Long lotId,
    LocalDate assignedAt
) { }
