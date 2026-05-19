package com.digitalcow.production.milking.dto;

import com.digitalcow.production.milking.MilkingSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Payload para registro masivo de ordenos. Todas las entradas comparten fecha y sesion.
 */
public record MilkingBulkDto(
    @NotNull LocalDate milkingDate,
    @NotNull MilkingSession session,
    @NotEmpty @Valid List<AnimalMilking> animals
) {
    public record AnimalMilking(
        @NotNull Long animalId,
        @NotNull @Positive BigDecimal liters
    ) { }
}
