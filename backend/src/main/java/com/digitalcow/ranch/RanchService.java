package com.digitalcow.ranch;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.ranch.dto.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/** CRUD de ranchos. Filtro multi-tenant aplicado por Hibernate filter. */
@Service
public class RanchService {

    private final RanchRepository repo;
    private final com.digitalcow.animal.AnimalRepository animals;

    public RanchService(RanchRepository repo, com.digitalcow.animal.AnimalRepository animals) {
        this.repo = repo;
        this.animals = animals;
    }

    /** Este metodo lista los ranchos. */
    public List<RanchDto> list() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    /** Este metodo devuelve el rancho. */
    public RanchDto get(Long id) {
        return toDto(find(id));
    }

    /** Este metodo crea el rancho. */
    @Transactional
    public RanchDto create(RanchUpsertRequest req) {
        Ranch r = new Ranch();
        apply(r, req);
        return toDto(repo.save(r));
    }

    /** Este metodo actualiza el rancho. */
    @Transactional
    public RanchDto update(Long id, RanchUpsertRequest req) {
        Ranch r = find(id);
        apply(r, req);
        return toDto(r);
    }

    /**
     * Elimina rancho previa verificacion de no tener animales ACTIVE.
     */
    @Transactional
    public void delete(Long id) {
        Ranch r = find(id);
        long count = animals.countByRanchIdAndStatus(id, com.digitalcow.animal.AnimalStatus.ACTIVE);
        if (count > 0) {
            throw BusinessException.conflict(ErrorCode.RANCH_HAS_ANIMALS, "Ranch has active animals");
        }
        repo.delete(r);
    }

    private Ranch find(Long id) {
        return repo.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Ranch not found"));
    }

    private void apply(Ranch r, RanchUpsertRequest req) {
        r.setName(req.name());
        r.setLocation(req.location());
        r.setLatitude(req.latitude());
        r.setLongitude(req.longitude());
        r.setAreaHectares(req.areaHectares());
        r.setNotes(req.notes());
    }

    private RanchDto toDto(Ranch r) {
        return new RanchDto(r.getId(), r.getName(), r.getLocation(),
            r.getLatitude(), r.getLongitude(), r.getAreaHectares(), r.getNotes());
    }
}
