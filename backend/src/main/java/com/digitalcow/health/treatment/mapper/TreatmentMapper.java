package com.digitalcow.health.treatment.mapper;

import com.digitalcow.catalog.medication.Medication;
import com.digitalcow.health.treatment.Treatment;
import com.digitalcow.health.treatment.dto.TreatmentCreateDto;
import com.digitalcow.health.treatment.dto.TreatmentResponseDto;
import com.digitalcow.health.treatment.dto.TreatmentUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de Treatment con join expandido al catalogo de medicamentos. */
@Mapper(componentModel = "spring")
public interface TreatmentMapper {

    @Mapping(target = "medicationNameEs", source = "medication.nameEs")
    @Mapping(target = "medicationNameEn", source = "medication.nameEn")
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "animalId", source = "entity.animalId")
    @Mapping(target = "diagnosisId", source = "entity.diagnosisId")
    @Mapping(target = "medicationId", source = "entity.medicationId")
    @Mapping(target = "startedAt", source = "entity.startedAt")
    @Mapping(target = "endedAt", source = "entity.endedAt")
    @Mapping(target = "dose", source = "entity.dose")
    @Mapping(target = "dosesCount", source = "entity.dosesCount")
    @Mapping(target = "route", source = "entity.route")
    @Mapping(target = "withdrawalMilkUntil", source = "entity.withdrawalMilkUntil")
    @Mapping(target = "withdrawalMeatUntil", source = "entity.withdrawalMeatUntil")
    @Mapping(target = "cost", source = "entity.cost")
    @Mapping(target = "prescribedBy", source = "entity.prescribedBy")
    @Mapping(target = "vetVisitId", source = "entity.vetVisitId")
    @Mapping(target = "notes", source = "entity.notes")
    TreatmentResponseDto toDto(Treatment entity, Medication medication);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    @Mapping(target = "withdrawalMilkUntil", ignore = true)
    @Mapping(target = "withdrawalMeatUntil", ignore = true)
    Treatment fromCreate(TreatmentCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void applyUpdate(TreatmentUpdateDto dto, @MappingTarget Treatment entity);
}
