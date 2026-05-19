package com.digitalcow.health.vaccination;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/** Repositorio de Vaccination con queries para timeline y alertas. */
@Repository
public interface VaccinationRepository extends JpaRepository<Vaccination, Long>, JpaSpecificationExecutor<Vaccination> {

    List<Vaccination> findByAnimalIdOrderByAppliedAtDesc(Long animalId);

    @Query("SELECT v FROM Vaccination v WHERE v.nextDoseDue BETWEEN :from AND :to ORDER BY v.nextDoseDue")
    List<Vaccination> findUpcomingDoses(@Param("from") LocalDate from, @Param("to") LocalDate to);

    long countByNextDoseDueBetween(LocalDate from, LocalDate to);
}
