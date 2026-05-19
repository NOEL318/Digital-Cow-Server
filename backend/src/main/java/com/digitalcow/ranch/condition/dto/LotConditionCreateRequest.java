package com.digitalcow.ranch.condition.dto;

import com.digitalcow.ranch.condition.LotConditionKind;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Payload para registrar una condicion del corral. */
public record LotConditionCreateRequest(
    @NotNull Long lotId,
    @NotNull LocalDate observedAt,
    @NotNull LotConditionKind kind,
    @Min(1) @Max(5) Short severity,
    @Size(max = 80) String customLabel,
    @Size(max = 400) String notes
) { }
