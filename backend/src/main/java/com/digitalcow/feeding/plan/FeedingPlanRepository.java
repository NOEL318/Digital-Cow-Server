package com.digitalcow.feeding.plan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/** Repositorio CRUD de FeedingPlan. */
@Repository
public interface FeedingPlanRepository extends JpaRepository<FeedingPlan, Long>, JpaSpecificationExecutor<FeedingPlan> {
}
