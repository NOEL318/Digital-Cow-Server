package com.digitalcow.ranch.condition.dto;

import com.digitalcow.ranch.condition.LotConditionKind;

import java.time.LocalDate;

/** Vista publica de una condicion del corral. */
public record LotConditionDto(
    Long id,
    Long lotId,
    LocalDate observedAt,
    LotConditionKind kind,
    Short severity,
    String customLabel,
    String notes
) { }
