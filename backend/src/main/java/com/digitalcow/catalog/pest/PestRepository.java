package com.digitalcow.catalog.pest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio read-only de catalogo global de plagas. */
@Repository
public interface PestRepository extends JpaRepository<Pest, Long> { }
