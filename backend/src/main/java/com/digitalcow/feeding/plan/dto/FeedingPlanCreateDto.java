package com.digitalcow.feeding.plan.dto;

import com.digitalcow.feeding.plan.FeedingPlanCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Create payload de FeedingPlan. */
public record FeedingPlanCreateDto(
    @NotBlank @Size(max = 160) String name,
    @NotNull FeedingPlanCategory category,
    @Size(max = 500) String description
) { }
