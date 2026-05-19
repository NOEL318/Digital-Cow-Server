package com.digitalcow.catalog.medication.mapper;

import com.digitalcow.catalog.medication.Medication;
import com.digitalcow.catalog.medication.MedicationCategory;
import com.digitalcow.catalog.medication.dto.MedicationDto;
import com.digitalcow.catalog.medication.dto.MedicationUpsertRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct para Medication. */
@Mapper(componentModel = "spring")
public interface MedicationMapper {

    MedicationDto toDto(Medication entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(MedicationUpsertRequest req, @MappingTarget Medication target);

    default MedicationCategory orDefault(MedicationCategory c) {
        return c == null ? MedicationCategory.OTHER : c;
    }
}
