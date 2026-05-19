package com.digitalcow.reproduction.weaning;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.reproduction.weaning.dto.WeaningCreateDto;
import com.digitalcow.reproduction.weaning.dto.WeaningResponseDto;
import com.digitalcow.reproduction.weaning.dto.WeaningUpdateDto;
import com.digitalcow.reproduction.weaning.event.WeaningChangedEvent;
import com.digitalcow.reproduction.weaning.mapper.WeaningMapper;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Servicio CRUD de Weaning con autorizacion por rol. */
@Service
@Transactional
public class WeaningService {

    private final WeaningRepository repository;
    private final WeaningMapper mapper;
    private final ApplicationEventPublisher events;

    public WeaningService(WeaningRepository repository,
                          WeaningMapper mapper,
                          ApplicationEventPublisher events) {
        this.repository = repository;
        this.mapper = mapper;
        this.events = events;
    }

    /** Lista todos los destetes de la cuenta. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<WeaningResponseDto> list() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    /** Lista destetes de un animal en orden cronologico inverso. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<WeaningResponseDto> listByAnimal(Long animalId) {
        return repository.findByAnimalIdOrderByWeanedAtDesc(animalId).stream()
            .map(mapper::toDto)
            .toList();
    }

    /** Crea un destete. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public WeaningResponseDto create(WeaningCreateDto dto) {
        Weaning entity = mapper.fromCreate(dto);
        Weaning saved = repository.save(entity);
        events.publishEvent(new WeaningChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(saved);
    }

    /** Actualiza un destete. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public WeaningResponseDto update(Long id, WeaningUpdateDto dto) {
        Weaning entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Weaning not found"));
        mapper.applyUpdate(dto, entity);
        events.publishEvent(new WeaningChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(entity);
    }

    /** Borra un destete. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        Weaning entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Weaning not found"));
        repository.delete(entity);
        events.publishEvent(new WeaningChangedEvent(TenantContext.requireAccountId()));
    }
}
