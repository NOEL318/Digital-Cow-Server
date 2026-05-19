package com.digitalcow.finance.milksale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/** Repositorio CRUD de MilkSale. */
@Repository
public interface MilkSaleRepository extends JpaRepository<MilkSale, Long>, JpaSpecificationExecutor<MilkSale> {

    List<MilkSale> findBySaleDateBetweenOrderBySaleDateDesc(LocalDate from, LocalDate to);
}
