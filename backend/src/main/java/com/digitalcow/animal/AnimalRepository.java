package com.digitalcow.animal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/** Este repositorio consulta y guarda animales en la base de datos. */
public interface AnimalRepository extends JpaRepository<Animal, Long>, JpaSpecificationExecutor<Animal> {
    long countByRanchIdAndStatus(Long ranchId, AnimalStatus status);
    long countByLotIdAndStatus(Long lotId, AnimalStatus status);
    List<Animal> findByLotIdAndStatus(Long lotId, AnimalStatus status);

    /**
     * Busca por token publico de compartir. Se usa solo desde el endpoint
     * publico sin tenant en contexto, por lo que el filtro Hibernate no
     * aplica y devuelve el animal directamente.
     */
    Optional<Animal> findByShareToken(String shareToken);
}
