package com.digitalcow.production.weighing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/** Repositorio CRUD de Weighing. */
@Repository
public interface WeighingRepository extends JpaRepository<Weighing, Long>, JpaSpecificationExecutor<Weighing> {

    List<Weighing> findByAnimalIdOrderByWeighedAtAsc(Long animalId);

    List<Weighing> findByAnimalIdOrderByWeighedAtDesc(Long animalId);

    List<Weighing> findByAnimalIdAndWeighedAtBetweenOrderByWeighedAtAsc(Long animalId, LocalDate from, LocalDate to);
}
