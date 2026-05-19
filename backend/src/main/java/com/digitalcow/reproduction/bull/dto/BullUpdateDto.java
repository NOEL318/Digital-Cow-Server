package com.digitalcow.reproduction.bull.dto;

import com.digitalcow.reproduction.bull.BullSource;
import jakarta.validation.constraints.Size;

/** Update parcial de Bull. */
public record BullUpdateDto(
    @Size(max = 60) String internalCode,
    @Size(max = 160) String name,
    Long breedId,
    BullSource source,
    Long animalId,
    @Size(max = 80) String registryNumber,
    String notes
) { }
