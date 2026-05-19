package com.digitalcow.dashboard.dto;

/**
 * Resumen reproductivo del tenant para el dashboard.
 * - pregnantConfirmed: animales con al menos un pregnancy_check POSITIVE vigente
 * - upcomingCalvings21d: pregnancy_checks POSITIVE con estimated_calving_date en proximos 21 dias
 * - openCows: vacas vacias (sin POSITIVE despues del ultimo parto)
 * - avgDaysOpen: promedio de dias abiertos del hato (puede ser null)
 */
public record DashboardReproductionDto(
    long pregnantConfirmed,
    long upcomingCalvings21d,
    long openCows,
    Double avgDaysOpen
) { }
