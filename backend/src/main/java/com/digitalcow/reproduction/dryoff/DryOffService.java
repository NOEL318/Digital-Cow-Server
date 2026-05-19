package com.digitalcow.reproduction.dryoff;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.reproduction.dryoff.dto.DryOffCreateDto;
import com.digitalcow.reproduction.dryoff.dto.DryOffResponseDto;
import com.digitalcow.reproduction.dryoff.dto.DryOffUpdateDto;
import com.digitalcow.reproduction.dryoff.event.DryOffChangedEvent;
import com.digitalcow.reproduction.dryoff.mapper.DryOffMapper;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Servicio CRUD de DryOff con autorizacion por rol. */
@Service
@Transactional
public class DryOffService {

    private final DryOffRepository repository;
    private final DryOffMapper mapper;
    private final ApplicationEventPublisher events;

    public DryOffService(DryOffRepository repository,
                         DryOffMapper mapper,
                         ApplicationEventPublisher events) {
        this.repository = repository;
        this.mapper = mapper;
        this.events = events;
    }

    /** Lista todos los secados de la cuenta. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<DryOffResponseDto> list() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    /** Lista secados de un animal en orden cronologico inverso. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<DryOffResponseDto> listByAnimal(Long animalId) {
        return repository.findByAnimalIdOrderByDriedOffAtDesc(animalId).stream()
            .map(mapper::toDto)
            .toList();
    }

    /** Crea un secado. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public DryOffResponseDto create(DryOffCreateDto dto) {
        DryOff entity = mapper.fromCreate(dto);
        DryOff saved = repository.save(entity);
        events.publishEvent(new DryOffChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(saved);
    }

    /** Actualiza un secado. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public DryOffResponseDto update(Long id, DryOffUpdateDto dto) {
        DryOff entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Dry off not found"));
        mapper.applyUpdate(dto, entity);
        events.publishEvent(new DryOffChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(entity);
    }

    /** Borra un secado. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        DryOff entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Dry off not found"));
        repository.delete(entity);
        events.publishEvent(new DryOffChangedEvent(TenantContext.requireAccountId()));
    }
}
