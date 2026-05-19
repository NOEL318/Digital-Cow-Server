package com.digitalcow.dashboard.dto;

import java.math.BigDecimal;

/**
 * Resumen de produccion del tenant para el dashboard.
 * - todayMilkLiters: total de litros ordenados hoy
 * - mtdMilkLiters: total de litros del mes a la fecha
 * - avgAdgKgDayThisMonth: ADG promedio (kg/dia) entre pesajes del mes
 * - activeMilkingCows: vacas DAIRY activas con al menos un ordeno en los ultimos 30 dias
 */
public record DashboardProductionDto(
    BigDecimal todayMilkLiters,
    BigDecimal mtdMilkLiters,
    Double avgAdgKgDayThisMonth,
    long activeMilkingCows
) { }
