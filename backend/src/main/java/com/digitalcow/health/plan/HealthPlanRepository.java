package com.digitalcow.health.plan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de HealthPlan. */
@Repository
public interface HealthPlanRepository extends JpaRepository<HealthPlan, Long> { }
