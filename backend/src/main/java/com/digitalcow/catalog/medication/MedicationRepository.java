package com.digitalcow.catalog.medication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de medicamentos. Soporta consultas para listado mixto
 * (seeds globales + entradas del tenant) y lookup por codigo de
 * barras restringido al tenant o globales.
 */
@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {

    @Query("""
        SELECT m FROM Medication m
        WHERE (m.accountId IS NULL OR m.accountId = :accountId)
        ORDER BY m.nameEs ASC
        """)
    List<Medication> findVisibleForAccount(@Param("accountId") Long accountId);

    @Query("""
        SELECT m FROM Medication m
        WHERE m.barcode = :barcode
          AND (m.accountId IS NULL OR m.accountId = :accountId)
        """)
    Optional<Medication> findByBarcodeForAccount(@Param("barcode") String barcode,
                                                  @Param("accountId") Long accountId);

    @Query("""
        SELECT m FROM Medication m
        WHERE m.id = :id
          AND (m.accountId IS NULL OR m.accountId = :accountId)
        """)
    Optional<Medication> findVisibleByIdForAccount(@Param("id") Long id,
                                                    @Param("accountId") Long accountId);
}
