package com.digitalcow.health.vaccination;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/** Builders de Specification para filtros de Vaccination. */
public final class VaccinationSpecifications {

    private VaccinationSpecifications() { }

    /** Filtra por animal_id exacto. */
    public static Specification<Vaccination> hasAnimal(Long animalId) {
        return (root, query, cb) -> animalId == null ? null : cb.equal(root.get("animalId"), animalId);
    }

    /** Filtra por lot_id exacto. */
    public static Specification<Vaccination> hasLot(Long lotId) {
        return (root, query, cb) -> lotId == null ? null : cb.equal(root.get("lotId"), lotId);
    }

    /** Filtra por vaccine_id exacto. */
    public static Specification<Vaccination> hasVaccine(Long vaccineId) {
        return (root, query, cb) -> vaccineId == null ? null : cb.equal(root.get("vaccineId"), vaccineId);
    }

    /** Filtra por rango de applied_at. */
    public static Specification<Vaccination> appliedBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null) return cb.between(root.get("appliedAt"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("appliedAt"), from);
            return cb.lessThanOrEqualTo(root.get("appliedAt"), to);
        };
    }
}
