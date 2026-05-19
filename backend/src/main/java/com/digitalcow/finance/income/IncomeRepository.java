package com.digitalcow.finance.income;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Repositorio CRUD de Income. */
@Repository
public interface IncomeRepository extends JpaRepository<Income, Long>, JpaSpecificationExecutor<Income> {

    /** Busca un income generado automaticamente por una venta. */
    Optional<Income> findFirstBySourceTypeAndSourceId(IncomeSourceType sourceType, Long sourceId);
}
