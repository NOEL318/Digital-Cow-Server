package com.digitalcow.health.treatment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/** Repositorio de Treatment con queries para timeline y alertas de retiro. */
@Repository
public interface TreatmentRepository extends JpaRepository<Treatment, Long>, JpaSpecificationExecutor<Treatment> {

    List<Treatment> findByAnimalIdOrderByStartedAtDesc(Long animalId);

    boolean existsByDiagnosisId(Long diagnosisId);

    long countByEndedAtIsNull();

    @Query("SELECT t FROM Treatment t WHERE t.withdrawalMilkUntil >= :today ORDER BY t.withdrawalMilkUntil")
    List<Treatment> findActiveMilkWithdrawals(@Param("today") LocalDate today);

    @Query("SELECT t FROM Treatment t WHERE t.withdrawalMeatUntil >= :today ORDER BY t.withdrawalMeatUntil")
    List<Treatment> findActiveMeatWithdrawals(@Param("today") LocalDate today);
}
