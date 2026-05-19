package com.digitalcow.health.pestcontrol;

import com.digitalcow.catalog.pest.Pest;
import com.digitalcow.catalog.pest.PestRepository;
import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.health.pestcontrol.dto.PestControlCreateDto;
import com.digitalcow.health.pestcontrol.dto.PestControlResponseDto;
import com.digitalcow.health.pestcontrol.dto.PestControlUpdateDto;
import com.digitalcow.health.pestcontrol.event.PestControlChangedEvent;
import com.digitalcow.health.pestcontrol.mapper.PestControlMapper;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de PestControl. Sin expansion por lote ni recalculo automatico;
 * el usuario ingresa next_application_at directamente.
 */
@Service
@Transactional
public class PestControlService {

    private final PestControlRepository repository;
    private final PestRepository pestRepository;
    private final PestControlMapper mapper;
    private final ApplicationEventPublisher events;

    public PestControlService(PestControlRepository repository,
                              PestRepository pestRepository,
                              PestControlMapper mapper,
                              ApplicationEventPublisher events) {
        this.repository = repository;
        this.pestRepository = pestRepository;
        this.mapper = mapper;
        this.events = events;
    }

    /** Lista todos los controles del tenant. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<PestControlResponseDto> list() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    /** Crea un control de plagas. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public PestControlResponseDto create(PestControlCreateDto dto) {
        pestRepository.findById(dto.pestId())
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Pest not found"));
        PestControl entity = mapper.fromCreate(dto);
        PestControl saved = repository.save(entity);
        events.publishEvent(new PestControlChangedEvent(TenantContext.requireAccountId()));
        return toDto(saved);
    }

    /** Actualiza un control existente. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public PestControlResponseDto update(Long id, PestControlUpdateDto dto) {
        PestControl entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Pest control not found"));
        mapper.applyUpdate(dto, entity);
        events.publishEvent(new PestControlChangedEvent(TenantContext.requireAccountId()));
        return toDto(entity);
    }

    /** Borra un control. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        PestControl entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Pest control not found"));
        repository.delete(entity);
        events.publishEvent(new PestControlChangedEvent(TenantContext.requireAccountId()));
    }

    private PestControlResponseDto toDto(PestControl entity) {
        Pest pest = pestRepository.findById(entity.getPestId()).orElse(null);
        return mapper.toDto(entity, pest);
    }
}
