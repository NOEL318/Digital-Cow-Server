package com.digitalcow.production.kpis.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * KPIs de produccion del periodo [from, to].
 * - totalMilkLiters: SUM(milking.liters) en el periodo
 * - avgDailyMilkLiters: total / (dias del periodo)
 * - avgAdgKgDay: promedio de ADG entre primer y ultimo pesaje por animal en el periodo
 * - topProducers: top 5 animales por litros producidos en el periodo
 */
public record ProductionKpisDto(
    LocalDate from,
    LocalDate to,
    BigDecimal totalMilkLiters,
    BigDecimal avgDailyMilkLiters,
    Double avgAdgKgDay,
    List<TopProducer> topProducers
) {
    public record TopProducer(Long animalId, String internalTag, BigDecimal liters) { }
}
