package com.digitalcow.animal.mapper;

import com.digitalcow.animal.Animal;
import com.digitalcow.animal.dto.*;
import org.mapstruct.*;

/** Conversion MapStruct entre Animal entity y DTOs publicos. */
@Mapper(componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AnimalMapper {

    AnimalResponse toResponse(Animal a);

    /**
     * Variante por defecto sin URL de foto (uso interno de tests o
     * cuando la URL no es necesaria). En la lista REST se usa
     * toListItem(Animal, String).
     */
    @Mapping(target = "coverPhotoUrl", ignore = true)
    AnimalListItem toListItem(Animal a);

    @Mapping(target = "coverPhotoUrl", source = "coverPhotoUrl")
    AnimalListItem toListItem(Animal a, String coverPhotoUrl);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "coverPhotoId", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Animal fromCreate(AnimalCreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "coverPhotoId", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void applyUpdate(AnimalUpdateRequest req, @MappingTarget Animal a);
}
