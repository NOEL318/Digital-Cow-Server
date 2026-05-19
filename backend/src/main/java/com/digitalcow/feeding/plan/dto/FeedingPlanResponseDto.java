package com.digitalcow.feeding.plan.dto;

import com.digitalcow.feeding.plan.FeedingPlanCategory;

import java.util.List;

/** Response DTO de FeedingPlan con sus items. */
public record FeedingPlanResponseDto(
    Long id,
    String name,
    FeedingPlanCategory category,
    String description,
    List<FeedingPlanItemDto> items
) { }
