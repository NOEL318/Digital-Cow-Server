package com.digitalcow.feeding.plan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Repositorio CRUD de LotFeedingPlan. */
@Repository
public interface LotFeedingPlanRepository extends JpaRepository<LotFeedingPlan, Long> {

    List<LotFeedingPlan> findByLotIdOrderByAssignedAtDesc(Long lotId);

    List<LotFeedingPlan> findByFeedingPlanIdAndUnassignedAtIsNull(Long feedingPlanId);
}
