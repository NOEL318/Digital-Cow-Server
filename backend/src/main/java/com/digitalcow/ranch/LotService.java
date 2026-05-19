package com.digitalcow.ranch;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.ranch.dto.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/** Servicio CRUD de lotes. Soporta poligono y centro lat/lng opcionales. */
@Service
public class LotService {

    private final LotRepository repo;
    private final RanchRepository ranches;
    private final com.digitalcow.animal.AnimalRepository animals;

    public LotService(LotRepository repo, RanchRepository ranches,
                      com.digitalcow.animal.AnimalRepository animals) {
        this.repo = repo;
        this.ranches = ranches;
        this.animals = animals;
    }

    /** Este metodo lista el rancho. */
    public List<LotDto> listByRanch(Long ranchId) {
        return repo.findAllByRanchId(ranchId).stream().map(this::toDto).toList();
    }

    /** Este metodo crea el lote. */
    @Transactional
    public LotDto create(Long ranchId, LotUpsertRequest req) {
        ranches.findById(ranchId).orElseThrow(() ->
            BusinessException.notFound(ErrorCode.NOT_FOUND, "Ranch not found"));
        Lot l = new Lot();
        l.setRanchId(ranchId);
        applyRequest(l, req);
        return toDto(repo.save(l));
    }

    /** Este metodo actualiza el lote. */
    @Transactional
    public LotDto update(Long id, LotUpsertRequest req) {
        Lot l = find(id);
        applyRequest(l, req);
        return toDto(l);
    }

    /** Este metodo elimina el lote. */
    @Transactional
    public void delete(Long id) {
        Lot l = find(id);
        long count = animals.countByLotIdAndStatus(id, com.digitalcow.animal.AnimalStatus.ACTIVE);
        if (count > 0) {
            throw BusinessException.conflict(ErrorCode.LOT_HAS_ANIMALS, "Lot has active animals");
        }
        repo.delete(l);
    }

    private void applyRequest(Lot l, LotUpsertRequest req) {
        l.setName(req.name());
        l.setAreaHectares(req.areaHectares());
        l.setNotes(req.notes());
        l.setPolygon(req.polygon());
        l.setCenterLat(req.centerLat());
        l.setCenterLng(req.centerLng());
    }

    private Lot find(Long id) {
        return repo.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Lot not found"));
    }

    private LotDto toDto(Lot l) {
        return new LotDto(
            l.getId(), l.getRanchId(), l.getName(), l.getAreaHectares(), l.getNotes(),
            l.getPolygon(), l.getCenterLat(), l.getCenterLng()
        );
    }
}
