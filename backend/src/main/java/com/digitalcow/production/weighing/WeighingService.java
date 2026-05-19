package com.digitalcow.production.weighing;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.production.weighing.dto.WeighingCreateDto;
import com.digitalcow.production.weighing.dto.WeighingResponseDto;
import com.digitalcow.production.weighing.dto.WeighingUpdateDto;
import com.digitalcow.production.weighing.event.WeighingChangedEvent;
import com.digitalcow.production.weighing.mapper.WeighingMapper;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Servicio CRUD de Weighing. */
@Service
@Transactional
public class WeighingService {

    private final WeighingRepository repository;
    private final WeighingMapper mapper;
    private final ApplicationEventPublisher events;

    public WeighingService(WeighingRepository repository,
                           WeighingMapper mapper,
                           ApplicationEventPublisher events) {
        this.repository = repository;
        this.mapper = mapper;
        this.events = events;
    }

    /** Lista todos los pesajes de la cuenta. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<WeighingResponseDto> list() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    /** Lista pesajes de un animal en orden cronologico inverso. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<WeighingResponseDto> listByAnimal(Long animalId) {
        return repository.findByAnimalIdOrderByWeighedAtDesc(animalId).stream()
            .map(mapper::toDto)
            .toList();
    }

    /** Crea un pesaje. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public WeighingResponseDto create(WeighingCreateDto dto) {
        Weighing entity = mapper.fromCreate(dto);
        Weighing saved = repository.save(entity);
        events.publishEvent(new WeighingChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(saved);
    }

    /** Actualiza un pesaje. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public WeighingResponseDto update(Long id, WeighingUpdateDto dto) {
        Weighing entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Weighing not found"));
        mapper.applyUpdate(dto, entity);
        events.publishEvent(new WeighingChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(entity);
    }

    /** Borra un pesaje. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        Weighing entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Weighing not found"));
        repository.delete(entity);
        events.publishEvent(new WeighingChangedEvent(TenantContext.requireAccountId()));
    }
}
