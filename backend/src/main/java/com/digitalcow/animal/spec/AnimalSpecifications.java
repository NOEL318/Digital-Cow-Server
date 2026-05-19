package com.digitalcow.animal.spec;

import com.digitalcow.animal.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/** Specifications JPA para filtros dinamicos del listado de animales. */
public final class AnimalSpecifications {

    private AnimalSpecifications() {}

    public static Specification<Animal> build(String search, Long ranchId, Long lotId,
                                              Long breedId, Sex sex, Purpose purpose, AnimalStatus status) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String like = "%" + search.toLowerCase() + "%";
                p.add(cb.or(
                    cb.like(cb.lower(root.get("internalTag")), like),
                    cb.like(cb.lower(cb.coalesce(root.get("officialTag"), "")), like),
                    cb.like(cb.lower(cb.coalesce(root.get("name"), "")), like)
                ));
            }
            if (ranchId != null) p.add(cb.equal(root.get("ranchId"), ranchId));
            if (lotId != null) p.add(cb.equal(root.get("lotId"), lotId));
            if (breedId != null) p.add(cb.equal(root.get("breedId"), breedId));
            if (sex != null) p.add(cb.equal(root.get("sex"), sex));
            if (purpose != null) p.add(cb.equal(root.get("purpose"), purpose));
            if (status != null) p.add(cb.equal(root.get("status"), status));
            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}
