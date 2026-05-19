package com.digitalcow.finance.expense;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/** Builders de Specification para filtros de Expense. */
public final class ExpenseSpecifications {

    private ExpenseSpecifications() { }

    /** Filtra por expense_category_id exacto. */
    public static Specification<Expense> hasCategory(Long categoryId) {
        return (root, query, cb) -> categoryId == null ? null : cb.equal(root.get("expenseCategoryId"), categoryId);
    }

    /** Filtra por ranch_id exacto. */
    public static Specification<Expense> hasRanch(Long ranchId) {
        return (root, query, cb) -> ranchId == null ? null : cb.equal(root.get("ranchId"), ranchId);
    }

    /** Filtra por lot_id exacto. */
    public static Specification<Expense> hasLot(Long lotId) {
        return (root, query, cb) -> lotId == null ? null : cb.equal(root.get("lotId"), lotId);
    }

    /** Filtra por animal_id exacto. */
    public static Specification<Expense> hasAnimal(Long animalId) {
        return (root, query, cb) -> animalId == null ? null : cb.equal(root.get("animalId"), animalId);
    }

    /** Filtra por rango de incurred_at. */
    public static Specification<Expense> incurredBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null) return cb.between(root.get("incurredAt"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("incurredAt"), from);
            return cb.lessThanOrEqualTo(root.get("incurredAt"), to);
        };
    }
}
