package com.digitalcow.feeding.plan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Repositorio CRUD de FeedingPlanItem. */
@Repository
public interface FeedingPlanItemRepository extends JpaRepository<FeedingPlanItem, Long> {

    List<FeedingPlanItem> findByFeedingPlanIdOrderByIdAsc(Long feedingPlanId);

    void deleteByFeedingPlanId(Long feedingPlanId);
}
