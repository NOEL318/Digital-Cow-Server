package com.digitalcow.animal.dto;

import com.digitalcow.animal.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record AnimalResponse(
    Long id, Long ranchId, Long lotId,
    String internalTag, String officialTag, String rfid, String name,
    Sex sex, LocalDate birthDate, boolean birthDateEstimated,
    Long breedId, Purpose purpose, AnimalStatus status,
    Long coverPhotoId,
    Long sireId, String externalSireName, Long damId, BigDecimal birthWeightKg,
    String notes,
    Long createdByUserId, Instant createdAt, Instant updatedAt
) {}
