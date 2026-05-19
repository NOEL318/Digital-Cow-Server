package com.digitalcow.reproduction.semen;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Repositorio CRUD de SemenStraw. */
@Repository
public interface SemenStrawRepository extends JpaRepository<SemenStraw, Long>, JpaSpecificationExecutor<SemenStraw> {
    List<SemenStraw> findByBullId(Long bullId);
}
