package com.digitalcow.ranch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RanchUpsertRequest(@NotBlank @Size(max = 120) String name,
                                  @Size(max = 200) String location,
                                  BigDecimal latitude,
                                  BigDecimal longitude,
                                  BigDecimal areaHectares,
                                  String notes) {}
