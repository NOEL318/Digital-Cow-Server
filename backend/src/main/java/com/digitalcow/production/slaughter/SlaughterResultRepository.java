package com.digitalcow.production.slaughter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Repositorio CRUD de SlaughterResult. */
@Repository
public interface SlaughterResultRepository extends JpaRepository<SlaughterResult, Long>, JpaSpecificationExecutor<SlaughterResult> {

    List<SlaughterResult> findByAnimalIdOrderBySlaughteredAtDesc(Long animalId);
}
