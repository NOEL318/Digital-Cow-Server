package com.digitalcow.health.plan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de AnimalHealthPlan. */
@Repository
public interface AnimalHealthPlanRepository extends JpaRepository<AnimalHealthPlan, Long> {
    boolean existsByHealthPlanId(Long healthPlanId);
}
