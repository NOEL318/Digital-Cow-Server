package com.digitalcow.catalog.vaccine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio read-only de catalogo global de vacunas. */
@Repository
public interface VaccineRepository extends JpaRepository<Vaccine, Long> { }
