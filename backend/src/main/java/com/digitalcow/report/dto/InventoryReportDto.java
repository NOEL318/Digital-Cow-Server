package com.digitalcow.report.dto;

import com.digitalcow.animal.Purpose;
import com.digitalcow.animal.Sex;

import java.math.BigDecimal;
import java.util.List;

/** Inventario actual de animales activos con datos basicos. */
public record InventoryReportDto(
    int totalActive,
    List<Row> rows
) {
    public record Row(
        Long animalId,
        String internalTag,
        String officialTag,
        Long breedId,
        String breedNameEs,
        String breedNameEn,
        Sex sex,
        Purpose purpose,
        Long ageDays,
        Long currentLotId,
        String currentLotName,
        Long currentRanchId,
        String currentRanchName,
        BigDecimal lastWeightKg
    ) { }
}
