package com.digitalcow.finance.income;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/** Builders de Specification para filtros de Income. */
public final class IncomeSpecifications {

    private IncomeSpecifications() { }

    /** Filtra por income_category_id exacto. */
    public static Specification<Income> hasCategory(Long categoryId) {
        return (root, query, cb) -> categoryId == null ? null : cb.equal(root.get("incomeCategoryId"), categoryId);
    }

    /** Filtra por ranch_id exacto. */
    public static Specification<Income> hasRanch(Long ranchId) {
        return (root, query, cb) -> ranchId == null ? null : cb.equal(root.get("ranchId"), ranchId);
    }

    /** Filtra por lot_id exacto. */
    public static Specification<Income> hasLot(Long lotId) {
        return (root, query, cb) -> lotId == null ? null : cb.equal(root.get("lotId"), lotId);
    }

    /** Filtra por animal_id exacto. */
    public static Specification<Income> hasAnimal(Long animalId) {
        return (root, query, cb) -> animalId == null ? null : cb.equal(root.get("animalId"), animalId);
    }

    /** Filtra por source_type. */
    public static Specification<Income> hasSourceType(IncomeSourceType sourceType) {
        return (root, query, cb) -> sourceType == null ? null : cb.equal(root.get("sourceType"), sourceType);
    }

    /** Filtra por rango de received_at. */
    public static Specification<Income> receivedBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null) return cb.between(root.get("receivedAt"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("receivedAt"), from);
            return cb.lessThanOrEqualTo(root.get("receivedAt"), to);
        };
    }
}
