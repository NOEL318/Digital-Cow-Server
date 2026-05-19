package com.digitalcow.health.vetvisit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/** Repositorio CRUD de VetVisit. */
@Repository
public interface VetVisitRepository extends JpaRepository<VetVisit, Long>, JpaSpecificationExecutor<VetVisit> {
    long countByRanchId(Long ranchId);
}
