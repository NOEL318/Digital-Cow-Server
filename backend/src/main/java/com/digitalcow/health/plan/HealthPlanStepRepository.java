package com.digitalcow.health.plan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Repositorio de HealthPlanStep. */
@Repository
public interface HealthPlanStepRepository extends JpaRepository<HealthPlanStep, Long> {
    List<HealthPlanStep> findByHealthPlanIdOrderByStepOrder(Long healthPlanId);
}
