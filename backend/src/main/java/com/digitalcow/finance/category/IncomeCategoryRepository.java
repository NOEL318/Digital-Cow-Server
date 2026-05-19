package com.digitalcow.finance.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Repositorio CRUD de IncomeCategory. */
@Repository
public interface IncomeCategoryRepository extends JpaRepository<IncomeCategory, Long>, JpaSpecificationExecutor<IncomeCategory> {

    List<IncomeCategory> findByKind(IncomeKind kind);

    /** Prefiere categoria del tenant; si no, devuelve global. */
    Optional<IncomeCategory> findFirstByKindAndAccountId(IncomeKind kind, Long accountId);

    Optional<IncomeCategory> findFirstByKindAndAccountIdIsNull(IncomeKind kind);
}
