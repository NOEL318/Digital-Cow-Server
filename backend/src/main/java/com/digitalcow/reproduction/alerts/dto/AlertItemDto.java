package com.digitalcow.reproduction.alerts.dto;

import java.time.LocalDate;

/**
 * Item generico de alerta reproductiva.
 * type: "UPCOMING_CALVING" | "DRY_OFF_DUE" | "SERVED_WITHOUT_CHECK" | "OPEN_TOO_LONG".
 * relatedId: id de pregnancy_check / calving / service_event segun corresponda.
 */
public record AlertItemDto(
    String type,
    Long animalId,
    String animalTag,
    String label,
    LocalDate date,
    Long relatedId
) { }
