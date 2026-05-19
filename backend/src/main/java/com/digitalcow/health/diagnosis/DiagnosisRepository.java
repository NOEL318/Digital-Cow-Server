package com.digitalcow.health.diagnosis;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Repositorio de Diagnosis con queries para timeline y alertas. */
@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long>, JpaSpecificationExecutor<Diagnosis> {

    List<Diagnosis> findByAnimalIdOrderByDiagnosedAtDesc(Long animalId);

    long countByStatus(DiagnosisStatus status);

    /**
     * Diagnosticos activos que aun no tienen tratamiento asociado.
     * Usa subquery sobre Treatment (no JOIN para evitar problemas de filtros).
     */
    @Query("SELECT d FROM Diagnosis d WHERE d.status = com.digitalcow.health.diagnosis.DiagnosisStatus.ACTIVE "
        + "AND NOT EXISTS (SELECT 1 FROM com.digitalcow.health.treatment.Treatment t WHERE t.diagnosisId = d.id)")
    List<Diagnosis> findActiveWithoutTreatment();
}
