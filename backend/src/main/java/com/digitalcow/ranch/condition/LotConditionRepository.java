package com.digitalcow.ranch.condition;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/** Este repositorio consulta y guarda las condiciones registradas de los lotes. */
public interface LotConditionRepository extends JpaRepository<LotCondition, Long> {

    /** Ultimas N condiciones de un lote, ordenadas por fecha desc. */
    List<LotCondition> findTop100ByLotIdOrderByObservedAtDescIdDesc(Long lotId);

    /** Todas las condiciones en un rango para reportes y graficas. */
    List<LotCondition> findByLotIdAndObservedAtBetweenOrderByObservedAtDesc(
        Long lotId, LocalDate from, LocalDate to);
}
