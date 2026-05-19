package com.digitalcow.reproduction.abortion;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.reproduction.abortion.dto.AbortionCreateDto;
import com.digitalcow.reproduction.abortion.dto.AbortionResponseDto;
import com.digitalcow.reproduction.abortion.dto.AbortionUpdateDto;
import com.digitalcow.reproduction.abortion.event.AbortionChangedEvent;
import com.digitalcow.reproduction.abortion.mapper.AbortionMapper;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Servicio CRUD de Abortion con autorizacion por rol. */
@Service
@Transactional
public class AbortionService {

    private final AbortionRepository repository;
    private final AbortionMapper mapper;
    private final ApplicationEventPublisher events;

    public AbortionService(AbortionRepository repository,
                           AbortionMapper mapper,
                           ApplicationEventPublisher events) {
        this.repository = repository;
        this.mapper = mapper;
        this.events = events;
    }

    /** Lista todos los abortos de la cuenta. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<AbortionResponseDto> list() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    /** Lista los abortos de un animal en orden cronologico inverso. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<AbortionResponseDto> listByAnimal(Long animalId) {
        return repository.findByAnimalIdOrderByAbortedAtDesc(animalId).stream()
            .map(mapper::toDto)
            .toList();
    }

    /** Crea un aborto. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public AbortionResponseDto create(AbortionCreateDto dto) {
        Abortion entity = mapper.fromCreate(dto);
        Abortion saved = repository.save(entity);
        events.publishEvent(new AbortionChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(saved);
    }

    /** Actualiza un aborto. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public AbortionResponseDto update(Long id, AbortionUpdateDto dto) {
        Abortion entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Abortion not found"));
        mapper.applyUpdate(dto, entity);
        events.publishEvent(new AbortionChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(entity);
    }

    /** Borra un aborto. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        Abortion entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Abortion not found"));
        repository.delete(entity);
        events.publishEvent(new AbortionChangedEvent(TenantContext.requireAccountId()));
    }
}
