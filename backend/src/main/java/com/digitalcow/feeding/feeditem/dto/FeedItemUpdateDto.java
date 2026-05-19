package com.digitalcow.feeding.feeditem.dto;

import com.digitalcow.feeding.feeditem.FeedCategory;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Update parcial de FeedItem. */
public record FeedItemUpdateDto(
    @Size(max = 160) String nameEs,
    @Size(max = 160) String nameEn,
    FeedCategory category,
    BigDecimal dryMatterPct,
    BigDecimal proteinPct,
    BigDecimal energyMcalKg,
    BigDecimal unitCost,
    @Size(max = 3) String currency,
    @Size(max = 400) String notes
) { }
