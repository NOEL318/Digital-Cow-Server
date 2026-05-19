package com.digitalcow.reproduction.heat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Repositorio CRUD de Heat. */
@Repository
public interface HeatRepository extends JpaRepository<Heat, Long>, JpaSpecificationExecutor<Heat> {
    List<Heat> findByAnimalIdOrderByDetectedAtDesc(Long animalId);
}
