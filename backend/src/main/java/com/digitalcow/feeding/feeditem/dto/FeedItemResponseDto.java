package com.digitalcow.feeding.feeditem.dto;

import com.digitalcow.feeding.feeditem.FeedCategory;

import java.math.BigDecimal;

/** Response DTO de FeedItem. isGlobal=true si account_id es null. */
public record FeedItemResponseDto(
    Long id,
    Long accountId,
    boolean isGlobal,
    String code,
    String nameEs,
    String nameEn,
    FeedCategory category,
    BigDecimal dryMatterPct,
    BigDecimal proteinPct,
    BigDecimal energyMcalKg,
    BigDecimal unitCost,
    String currency,
    String notes
) { }
