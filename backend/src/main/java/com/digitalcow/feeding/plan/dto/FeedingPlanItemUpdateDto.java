package com.digitalcow.feeding.plan.dto;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Update parcial de FeedingPlanItem. */
public record FeedingPlanItemUpdateDto(
    BigDecimal kgPerHeadDay,
    @Size(max = 200) String notes
) { }
