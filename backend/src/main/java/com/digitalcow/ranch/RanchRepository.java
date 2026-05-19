package com.digitalcow.ranch;

import org.springframework.data.jpa.repository.JpaRepository;

/** Este repositorio consulta y guarda ranchos en la base de datos. */
public interface RanchRepository extends JpaRepository<Ranch, Long> {
}
