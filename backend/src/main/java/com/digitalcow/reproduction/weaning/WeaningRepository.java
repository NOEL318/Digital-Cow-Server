package com.digitalcow.reproduction.weaning;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Repositorio CRUD de Weaning. */
@Repository
public interface WeaningRepository extends JpaRepository<Weaning, Long>, JpaSpecificationExecutor<Weaning> {

    List<Weaning> findByAnimalIdOrderByWeanedAtDesc(Long animalId);
}
