package com.digitalcow.feeding.plan.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Create payload de FeedingPlanItem. */
public record FeedingPlanItemCreateDto(
    @NotNull Long feedItemId,
    @NotNull @Positive BigDecimal kgPerHeadDay,
    @Size(max = 200) String notes
) { }
