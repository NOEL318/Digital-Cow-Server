package com.digitalcow.reproduction.abortion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Repositorio CRUD de Abortion. */
@Repository
public interface AbortionRepository extends JpaRepository<Abortion, Long>, JpaSpecificationExecutor<Abortion> {

    List<Abortion> findByAnimalIdOrderByAbortedAtDesc(Long animalId);
}
