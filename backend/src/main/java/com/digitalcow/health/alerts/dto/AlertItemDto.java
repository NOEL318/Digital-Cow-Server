package com.digitalcow.health.alerts.dto;

import java.time.LocalDate;

/**
 * Item generico de alerta sanitaria.
 * type: "UPCOMING_VACCINATION" | "WITHDRAWAL_MILK" | "WITHDRAWAL_MEAT" | "ACTIVE_DIAGNOSIS_NO_TREATMENT".
 * relatedId: id de vaccination/treatment/diagnosis segun corresponda.
 */
public record AlertItemDto(
    String type,
    Long animalId,
    String animalTag,
    String label,
    LocalDate date,
    Long relatedId
) { }
