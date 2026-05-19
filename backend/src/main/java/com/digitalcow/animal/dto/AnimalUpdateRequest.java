package com.digitalcow.animal.dto;

import com.digitalcow.animal.*;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AnimalUpdateRequest(
    Long ranchId,
    Long lotId,
    @Size(max = 40) String internalTag,
    @Size(max = 60) String officialTag,
    @Size(max = 40) String rfid,
    @Size(max = 80) String name,
    Sex sex,
    LocalDate birthDate,
    Boolean birthDateEstimated,
    Long breedId,
    Purpose purpose,
    AnimalStatus status,
    Long sireId,
    @Size(max = 160) String externalSireName,
    Long damId,
    BigDecimal birthWeightKg,
    String notes
) {}
