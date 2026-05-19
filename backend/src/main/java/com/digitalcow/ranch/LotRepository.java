package com.digitalcow.ranch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Este repositorio consulta y guarda lotes en la base de datos. */
public interface LotRepository extends JpaRepository<Lot, Long> {
    List<Lot> findAllByRanchId(Long ranchId);
}
