package com.digitalcow.health.plan.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/** Payload de asignacion de plan a animales o lotes. */
public record PlanAssignDto(
    @NotNull LocalDate assignedAt,
    List<Long> animalIds,
    List<Long> lotIds
) { }
