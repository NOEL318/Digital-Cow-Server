package com.digitalcow.production.milking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/** Repositorio CRUD de Milking. */
@Repository
public interface MilkingRepository extends JpaRepository<Milking, Long>, JpaSpecificationExecutor<Milking> {

    List<Milking> findByAnimalIdOrderByMilkingDateDesc(Long animalId);

    List<Milking> findByAnimalIdAndMilkingDateBetweenOrderByMilkingDateAsc(Long animalId, LocalDate from, LocalDate to);

    List<Milking> findByAnimalIdAndMilkingDateGreaterThanEqualOrderByMilkingDateAsc(Long animalId, LocalDate from);
}
