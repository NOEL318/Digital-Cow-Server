package com.digitalcow.reproduction.bull.dto;

import com.digitalcow.reproduction.bull.BullSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Create payload de Bull. */
public record BullCreateDto(
    @NotBlank @Size(max = 60) String internalCode,
    @NotBlank @Size(max = 160) String name,
    Long breedId,
    @NotNull BullSource source,
    Long animalId,
    @Size(max = 80) String registryNumber,
    String notes
) { }
