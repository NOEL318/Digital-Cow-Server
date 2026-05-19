package com.digitalcow.ranch.dto;

import java.math.BigDecimal;

public record LotDto(
    Long id,
    Long ranchId,
    String name,
    BigDecimal areaHectares,
    String notes,
    String polygon,
    BigDecimal centerLat,
    BigDecimal centerLng
) {}
