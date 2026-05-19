package com.digitalcow.catalog.disease;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio read-only de catalogo global de enfermedades. */
@Repository
public interface DiseaseRepository extends JpaRepository<Disease, Long> { }
