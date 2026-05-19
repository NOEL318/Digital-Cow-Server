package com.digitalcow.animal.dto;

import com.digitalcow.animal.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AnimalCreateRequest(
    @NotNull Long ranchId,
    Long lotId,
    @NotBlank @Size(max = 40) String internalTag,
    @Size(max = 60) String officialTag,
    @Size(max = 40) String rfid,
    @Size(max = 80) String name,
    @NotNull Sex sex,
    LocalDate birthDate,
    boolean birthDateEstimated,
    @NotNull Long breedId,
    @NotNull Purpose purpose,
    AnimalStatus status,
    Long sireId,
    @Size(max = 160) String externalSireName,
    Long damId,
    BigDecimal birthWeightKg,
    String notes
) {}
