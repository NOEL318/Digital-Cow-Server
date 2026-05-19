package com.digitalcow.feeding.feeditem.dto;

import com.digitalcow.feeding.feeditem.FeedCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Create payload de FeedItem. account_id se infiere del tenant actual. */
public record FeedItemCreateDto(
    @NotBlank @Size(max = 60) String code,
    @NotBlank @Size(max = 160) String nameEs,
    @NotBlank @Size(max = 160) String nameEn,
    @NotNull FeedCategory category,
    BigDecimal dryMatterPct,
    BigDecimal proteinPct,
    BigDecimal energyMcalKg,
    BigDecimal unitCost,
    @Size(max = 3) String currency,
    @Size(max = 400) String notes
) { }
