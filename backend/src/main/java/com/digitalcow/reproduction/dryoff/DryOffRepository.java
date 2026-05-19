package com.digitalcow.reproduction.dryoff;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Repositorio CRUD de DryOff. */
@Repository
public interface DryOffRepository extends JpaRepository<DryOff, Long>, JpaSpecificationExecutor<DryOff> {

    List<DryOff> findByAnimalIdOrderByDriedOffAtDesc(Long animalId);
}
