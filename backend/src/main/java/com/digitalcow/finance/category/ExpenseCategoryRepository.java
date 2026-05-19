package com.digitalcow.finance.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Repositorio CRUD de ExpenseCategory. */
@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long>, JpaSpecificationExecutor<ExpenseCategory> {

    List<ExpenseCategory> findByKind(ExpenseKind kind);

    /** Prefiere categoria del tenant; si no, devuelve global. */
    Optional<ExpenseCategory> findFirstByKindAndAccountId(ExpenseKind kind, Long accountId);

    Optional<ExpenseCategory> findFirstByKindAndAccountIdIsNull(ExpenseKind kind);

    Optional<ExpenseCategory> findFirstByAccountIdAndCode(Long accountId, String code);
}
