package com.digitalcow.report.dto;

import com.digitalcow.animal.AnimalStatus;
import com.digitalcow.animal.Purpose;
import com.digitalcow.animal.Sex;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Reporte completo de un animal: datos basicos + timeline de eventos historicos.
 * Pensado para vista imprimible y exportacion a CSV/PDF cliente-side.
 */
public record AnimalReportDto(
    AnimalSummary animal,
    List<VaccinationEntry> vaccinations,
    List<DiagnosisEntry> diagnoses,
    List<TreatmentEntry> treatments,
    List<WeighingEntry> weighings,
    List<MilkingEntry> recentMilkings,
    List<CalvingEntry> calvings,
    AnimalSaleEntry sale,
    BigDecimal totalCost,
    BigDecimal totalIncome
) {
    public record AnimalSummary(
        Long id,
        String internalTag,
        String officialTag,
        String name,
        Sex sex,
        Purpose purpose,
        AnimalStatus status,
        LocalDate birthDate,
        Long ranchId,
        String ranchName,
        Long lotId,
        String lotName,
        Long breedId,
        String breedNameEs,
        String breedNameEn
    ) { }

    public record VaccinationEntry(Long id, LocalDate appliedAt, Long vaccineId, BigDecimal cost, LocalDate nextDoseDue) { }
    public record DiagnosisEntry(Long id, LocalDate diagnosedAt, Long diseaseId, String severity, String status) { }
    public record TreatmentEntry(Long id, LocalDate startedAt, LocalDate endedAt, Long medicationId, BigDecimal cost) { }
    public record WeighingEntry(Long id, LocalDate weighedAt, BigDecimal weightKg) { }
    public record MilkingEntry(Long id, LocalDate milkingDate, String session, BigDecimal liters) { }
    public record CalvingEntry(Long id, LocalDate calvedAt, String outcome, Long calfAnimalId) { }
    public record AnimalSaleEntry(Long id, LocalDate soldAt, BigDecimal totalPrice, String buyer) { }
}
