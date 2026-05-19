package com.digitalcow.reproduction.pregnancy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Repositorio CRUD de PregnancyCheck. */
@Repository
public interface PregnancyCheckRepository extends JpaRepository<PregnancyCheck, Long>, JpaSpecificationExecutor<PregnancyCheck> {

    List<PregnancyCheck> findByAnimalIdOrderByCheckedAtDesc(Long animalId);
}
