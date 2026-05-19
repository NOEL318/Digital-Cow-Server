package com.digitalcow.reproduction.calving;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Repositorio CRUD de Calving. */
@Repository
public interface CalvingRepository extends JpaRepository<Calving, Long>, JpaSpecificationExecutor<Calving> {

    List<Calving> findByAnimalIdOrderByCalvedAtDesc(Long animalId);

    Optional<Calving> findFirstByAnimalIdOrderByCalvedAtDesc(Long animalId);
}
