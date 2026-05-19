package com.digitalcow.reproduction.bull;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/** Repositorio CRUD de Bull. */
@Repository
public interface BullRepository extends JpaRepository<Bull, Long>, JpaSpecificationExecutor<Bull> {
    boolean existsByInternalCode(String code);
}
