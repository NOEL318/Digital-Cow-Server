package com.digitalcow.reproduction.calving.dto;

import com.digitalcow.animal.Purpose;
import com.digitalcow.animal.Sex;
import com.digitalcow.reproduction.calving.CalvingEase;
import com.digitalcow.reproduction.calving.CalvingOutcome;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Create payload de Calving. Si createCalfAnimal=true, los campos calf*
 * son requeridos y el backend crea un Animal hijo enlazado por sire/dam.
 */
public record CalvingCreateDto(
    @NotNull Long animalId,
    @NotNull LocalDate calvedAt,
    CalvingEase ease,
    CalvingOutcome outcome,
    Sex calfSex,
    BigDecimal calfBirthWeightKg,
    Long pregnancyCheckId,
    String notes,
    Long createdByUserId,
    boolean createCalfAnimal,
    String calfInternalTag,
    Long calfRanchId,
    Long calfLotId,
    Long calfBreedId,
    Purpose calfPurpose
) { }
