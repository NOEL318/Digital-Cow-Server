package com.digitalcow.reproduction.heat;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.reproduction.heat.dto.HeatCreateDto;
import com.digitalcow.reproduction.heat.dto.HeatResponseDto;
import com.digitalcow.reproduction.heat.dto.HeatUpdateDto;
import com.digitalcow.reproduction.heat.event.HeatChangedEvent;
import com.digitalcow.reproduction.heat.mapper.HeatMapper;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Servicio CRUD de Heat con autorizacion por rol. */
@Service
@Transactional
public class HeatService {

    private final HeatRepository repository;
    private final HeatMapper mapper;
    private final ApplicationEventPublisher events;

    public HeatService(HeatRepository repository, HeatMapper mapper, ApplicationEventPublisher events) {
        this.repository = repository;
        this.mapper = mapper;
        this.events = events;
    }

    /** Lista todos los celos de la cuenta. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<HeatResponseDto> list() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    /** Lista celos de un animal en orden cronologico inverso. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<HeatResponseDto> listByAnimal(Long animalId) {
        return repository.findByAnimalIdOrderByDetectedAtDesc(animalId).stream()
            .map(mapper::toDto)
            .toList();
    }

    /** Crea un celo. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public HeatResponseDto create(HeatCreateDto dto) {
        Heat entity = mapper.fromCreate(dto);
        Heat saved = repository.save(entity);
        events.publishEvent(new HeatChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(saved);
    }

    /** Actualiza un celo. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public HeatResponseDto update(Long id, HeatUpdateDto dto) {
        Heat entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Heat not found"));
        mapper.applyUpdate(dto, entity);
        events.publishEvent(new HeatChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(entity);
    }

    /** Borra un celo. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        Heat entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Heat not found"));
        repository.delete(entity);
        events.publishEvent(new HeatChangedEvent(TenantContext.requireAccountId()));
    }
}
