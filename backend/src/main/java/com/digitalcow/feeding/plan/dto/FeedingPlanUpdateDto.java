package com.digitalcow.feeding.plan.dto;

import com.digitalcow.feeding.plan.FeedingPlanCategory;
import jakarta.validation.constraints.Size;

/** Update parcial de FeedingPlan. */
public record FeedingPlanUpdateDto(
    @Size(max = 160) String name,
    FeedingPlanCategory category,
    @Size(max = 500) String description
) { }
