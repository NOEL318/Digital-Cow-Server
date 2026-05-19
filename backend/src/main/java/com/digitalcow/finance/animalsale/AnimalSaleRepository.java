package com.digitalcow.finance.animalsale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Repositorio CRUD de AnimalSale. */
@Repository
public interface AnimalSaleRepository extends JpaRepository<AnimalSale, Long>, JpaSpecificationExecutor<AnimalSale> {

    Optional<AnimalSale> findByAnimalId(Long animalId);

    List<AnimalSale> findBySoldAtBetweenOrderBySoldAtDesc(LocalDate from, LocalDate to);
}
