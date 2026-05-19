package com.digitalcow.reproduction.bull.dto;

import com.digitalcow.reproduction.bull.BullSource;

/** Response DTO de Bull. */
public record BullResponseDto(
    Long id,
    String internalCode,
    String name,
    Long breedId,
    BullSource source,
    Long animalId,
    String registryNumber,
    String notes
) { }
