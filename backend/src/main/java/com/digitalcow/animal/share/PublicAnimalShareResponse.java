package com.digitalcow.animal.share;

import com.digitalcow.animal.AnimalStatus;
import com.digitalcow.animal.Purpose;
import com.digitalcow.animal.Sex;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Vista publica de solo lectura de un animal, expuesta a quien tenga
 * el share_token. Incluye identificacion, foto principal e historial
 * resumido de pesajes, vacunaciones y ordeños. No expone ranchId,
 * lotId, notas ni informacion financiera para no filtrar contexto
 * privado del rancho.
 */
public record PublicAnimalShareResponse(
    String internalTag,
    String name,
    Sex sex,
    AnimalStatus status,
    Purpose purpose,
    LocalDate birthDate,
    boolean birthDateEstimated,
    Integer ageMonths,
    String breedName,
    String coverPhotoUrl,
    Integer daysInMilk,
    LocalDate lastCalving,
    List<WeighingPoint> weighings,
    List<VaccinationPoint> vaccinations,
    List<MilkingPoint> milkings
) {

    /** Punto de pesaje publico (fecha y kilogramos, sin notas). */
    public record WeighingPoint(LocalDate weighedAt, BigDecimal weightKg) {}

    /** Punto de vacunacion publico (fecha y nombre de la vacuna). */
    public record VaccinationPoint(LocalDate appliedAt, String vaccineName) {}

    /** Punto de ordeño publico (fecha y litros totales del dia). */
    public record MilkingPoint(LocalDate milkingDate, BigDecimal liters) {}
}
