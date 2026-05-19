package com.digitalcow.health.diagnosis.mapper;

import com.digitalcow.catalog.disease.Disease;
import com.digitalcow.health.diagnosis.Diagnosis;
import com.digitalcow.health.diagnosis.dto.DiagnosisCreateDto;
import com.digitalcow.health.diagnosis.dto.DiagnosisResponseDto;
import com.digitalcow.health.diagnosis.dto.DiagnosisUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de Diagnosis con join expandido al catalogo de enfermedades. */
@Mapper(componentModel = "spring")
public interface DiagnosisMapper {

    @Mapping(target = "diseaseNameEs", source = "disease.nameEs")
    @Mapping(target = "diseaseNameEn", source = "disease.nameEn")
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "animalId", source = "entity.animalId")
    @Mapping(target = "diseaseId", source = "entity.diseaseId")
    @Mapping(target = "diagnosedAt", source = "entity.diagnosedAt")
    @Mapping(target = "severity", source = "entity.severity")
    @Mapping(target = "symptoms", source = "entity.symptoms")
    @Mapping(target = "status", source = "entity.status")
    @Mapping(target = "resolvedAt", source = "entity.resolvedAt")
    @Mapping(target = "vetVisitId", source = "entity.vetVisitId")
    @Mapping(target = "notes", source = "entity.notes")
    DiagnosisResponseDto toDto(Diagnosis entity, Disease disease);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "diagnosedByUserId", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    Diagnosis fromCreate(DiagnosisCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void applyUpdate(DiagnosisUpdateDto dto, @MappingTarget Diagnosis entity);
}
