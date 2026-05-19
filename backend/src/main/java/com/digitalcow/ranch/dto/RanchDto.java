package com.digitalcow.ranch.dto;

import java.math.BigDecimal;

public record RanchDto(Long id, String name, String location,
                       BigDecimal latitude, BigDecimal longitude,
                       BigDecimal areaHectares, String notes) {}
