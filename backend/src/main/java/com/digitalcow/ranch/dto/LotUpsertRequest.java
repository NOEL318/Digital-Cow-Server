package com.digitalcow.ranch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record LotUpsertRequest(
    @NotBlank @Size(max = 120) String name,
    BigDecimal areaHectares,
    String notes,
    String polygon,
    BigDecimal centerLat,
    BigDecimal centerLng
) {}
