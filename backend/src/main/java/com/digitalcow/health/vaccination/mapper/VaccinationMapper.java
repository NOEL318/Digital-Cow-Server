package com.digitalcow.health.vaccination.mapper;

import com.digitalcow.catalog.vaccine.Vaccine;
import com.digitalcow.health.vaccination.Vaccination;
import com.digitalcow.health.vaccination.dto.VaccinationCreateDto;
import com.digitalcow.health.vaccination.dto.VaccinationResponseDto;
import com.digitalcow.health.vaccination.dto.VaccinationUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de Vaccination con join expandido al catalogo de vacunas. */
@Mapper(componentModel = "spring")
public interface VaccinationMapper {

    @Mapping(target = "vaccineNameEs", source = "vaccine.nameEs")
    @Mapping(target = "vaccineNameEn", source = "vaccine.nameEn")
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "animalId", source = "entity.animalId")
    @Mapping(target = "lotId", source = "entity.lotId")
    @Mapping(target = "vaccineId", source = "entity.vaccineId")
    @Mapping(target = "batchNumber", source = "entity.batchNumber")
    @Mapping(target = "appliedAt", source = "entity.appliedAt")
    @Mapping(target = "doseMl", source = "entity.doseMl")
    @Mapping(target = "route", source = "entity.route")
    @Mapping(target = "nextDoseDue", source = "entity.nextDoseDue")
    @Mapping(target = "cost", source = "entity.cost")
    @Mapping(target = "vetVisitId", source = "entity.vetVisitId")
    @Mapping(target = "notes", source = "entity.notes")
    VaccinationResponseDto toDto(Vaccination entity, Vaccine vaccine);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "appliedByUserId", ignore = true)
    @Mapping(target = "nextDoseDue", ignore = true)
    @Mapping(target = "lotId", ignore = true)
    Vaccination fromCreate(VaccinationCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void applyUpdate(VaccinationUpdateDto dto, @MappingTarget Vaccination entity);
}
