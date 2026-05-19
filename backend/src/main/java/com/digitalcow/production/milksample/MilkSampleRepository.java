package com.digitalcow.production.milksample;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Repositorio CRUD de MilkSample. */
@Repository
public interface MilkSampleRepository extends JpaRepository<MilkSample, Long>, JpaSpecificationExecutor<MilkSample> {

    List<MilkSample> findByAnimalIdOrderBySampledAtDesc(Long animalId);
}
