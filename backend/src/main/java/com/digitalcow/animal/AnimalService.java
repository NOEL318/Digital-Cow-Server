package com.digitalcow.animal;

import com.digitalcow.animal.dto.*;
import com.digitalcow.animal.event.AnimalChangedEvent;
import com.digitalcow.animal.mapper.AnimalMapper;
import com.digitalcow.animal.spec.AnimalSpecifications;
import com.digitalcow.audit.Auditable;
import com.digitalcow.audit.AuditLog;
import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.common.web.CurrentUser;
import com.digitalcow.photo.AnimalPhotoRepository;
import com.digitalcow.tenancy.TenantContext;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** CRUD de animales. */
@Service
public class AnimalService {

    private final AnimalRepository repo;
    private final AnimalMapper mapper;
    private final ApplicationEventPublisher events;
    private final AnimalPhotoRepository photoRepository;

    public AnimalService(AnimalRepository repo, AnimalMapper mapper,
                          ApplicationEventPublisher events,
                          AnimalPhotoRepository photoRepository) {
        this.repo = repo;
        this.mapper = mapper;
        this.events = events;
        this.photoRepository = photoRepository;
    }

    /** Este metodo lista los animales. */
    public Page<AnimalListItem> list(String search, Long ranchId, Long lotId, Long breedId,
                                     Sex sex, Purpose purpose, AnimalStatus status, Pageable pageable) {
        Page<Animal> animals = repo.findAll(
            AnimalSpecifications.build(search, ranchId, lotId, breedId, sex, purpose, status), pageable);
        Map<Long, String> coverUrls = loadCoverUrls(animals.getContent());
        return animals.map(a -> {
            Long photoId = a.getCoverPhotoId();
            String url = photoId == null ? null : coverUrls.get(photoId);
            return mapper.toListItem(a, url);
        });
    }

    private Map<Long, String> loadCoverUrls(List<Animal> animals) {
        List<Long> ids = animals.stream()
            .map(Animal::getCoverPhotoId)
            .filter(java.util.Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, String> map = new HashMap<>(ids.size());
        if (ids.isEmpty()) return map;
        for (AnimalPhotoRepository.IdUrlProjection p : photoRepository.findUrlByIdIn(ids)) {
            map.put(p.getId(), p.getUrl());
        }
        return map;
    }

    /** Este metodo devuelve el animal. */
    public AnimalResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    /** Este metodo crea el animal. */
    @Transactional
    @Auditable(entityType = "Animal", action = AuditLog.Action.CREATE)
    public AnimalResponse create(AnimalCreateRequest req) {
        Animal a = mapper.fromCreate(req);
        a.setCreatedByUserId(CurrentUser.require().userId());
        if (a.getStatus() == null) a.setStatus(AnimalStatus.ACTIVE);
        Animal saved = repo.save(a);
        events.publishEvent(new AnimalChangedEvent(TenantContext.get()));
        return mapper.toResponse(saved);
    }

    /** Este metodo actualiza el animal. */
    @Transactional
    @Auditable(entityType = "Animal", action = AuditLog.Action.UPDATE)
    public AnimalResponse update(Long id, AnimalUpdateRequest req) {
        Animal a = find(id);
        mapper.applyUpdate(req, a);
        events.publishEvent(new AnimalChangedEvent(TenantContext.get()));
        return mapper.toResponse(a);
    }

    /**
     * Solo permite borrado fisico si nunca se edito (createdAt == updatedAt).
     * Caso contrario, exige cambio de status (SOLD/DEAD) via update.
     */
    @Transactional
    @Auditable(entityType = "Animal", action = AuditLog.Action.DELETE)
    public void delete(Long id) {
        Animal a = find(id);
        if (!a.getCreatedAt().equals(a.getUpdatedAt())) {
            throw BusinessException.conflict(ErrorCode.ANIMAL_NOT_DELETABLE,
                "Animal edited; cannot hard-delete. Update status instead.");
        }
        repo.delete(a);
        events.publishEvent(new AnimalChangedEvent(TenantContext.get()));
    }

    Animal find(Long id) {
        return repo.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Animal not found"));
    }
}
