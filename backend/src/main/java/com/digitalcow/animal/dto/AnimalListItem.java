package com.digitalcow.animal.dto;

import com.digitalcow.animal.*;

/**
 * Item paginado para listas. Incluye coverPhotoUrl cuando el animal
 * tiene foto principal asignada, para mostrar miniatura en la UI sin
 * un segundo viaje al backend.
 */
public record AnimalListItem(
    Long id, String internalTag, String officialTag, String name,
    Long breedId, Sex sex, AnimalStatus status, Long lotId, Long coverPhotoId,
    String coverPhotoUrl
) {}
