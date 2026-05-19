package com.digitalcow.animal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/** Este repositorio consulta y guarda animales en la base de datos. */
public interface AnimalRepository extends JpaRepository<Animal, Long>, JpaSpecificationExecutor<Animal> {
    long countByRanchIdAndStatus(Long ranchId, AnimalStatus status);
    long countByLotIdAndStatus(Long lotId, AnimalStatus status);
    List<Animal> findByLotIdAndStatus(Long lotId, AnimalStatus status);
}
