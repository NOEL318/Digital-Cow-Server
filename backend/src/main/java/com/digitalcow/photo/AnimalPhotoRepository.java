package com.digitalcow.photo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;

/** Este repositorio consulta y guarda las fotos de animales. */
public interface AnimalPhotoRepository extends JpaRepository<AnimalPhoto, Long> {

    List<AnimalPhoto> findAllByAnimalIdOrderByCreatedAtDesc(Long animalId);

    /**
     * Proyeccion ligera id+url para enriquecer listas de animales con
     * la URL de su foto principal en una sola query.
     */
    @Query("SELECT p.id AS id, p.cloudinaryUrl AS url FROM AnimalPhoto p WHERE p.id IN :ids")
    List<IdUrlProjection> findUrlByIdIn(@Param("ids") Collection<Long> ids);

    interface IdUrlProjection {
        Long getId();
        String getUrl();
    }
}
