package com.digitalcow.feeding.plan.dto;

import java.math.BigDecimal;

/** Item dentro de un plan. */
public record FeedingPlanItemDto(
    Long id,
    Long feedingPlanId,
    Long feedItemId,
    BigDecimal kgPerHeadDay,
    String notes
) { }
