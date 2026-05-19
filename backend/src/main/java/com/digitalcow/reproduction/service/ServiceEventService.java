package com.digitalcow.reproduction.service;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.reproduction.semen.SemenStrawService;
import com.digitalcow.reproduction.service.dto.ServiceEventCreateDto;
import com.digitalcow.reproduction.service.dto.ServiceEventResponseDto;
import com.digitalcow.reproduction.service.dto.ServiceEventUpdateDto;
import com.digitalcow.reproduction.service.event.ServiceEventChangedEvent;
import com.digitalcow.reproduction.service.mapper.ServiceEventMapper;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de eventos reproductivos. Soporta IA, monta natural y transferencia de embriones.
 * Cuando el tipo es IA decrementa la pajilla de semen.
 */
@Service
@Transactional
public class ServiceEventService {

    private final ServiceEventRepository repository;
    private final SemenStrawService semenStrawService;
    private final ServiceEventMapper mapper;
    private final ApplicationEventPublisher events;

    public ServiceEventService(ServiceEventRepository repository,
                               SemenStrawService semenStrawService,
                               ServiceEventMapper mapper,
                               ApplicationEventPublisher events) {
        this.repository = repository;
        this.semenStrawService = semenStrawService;
        this.mapper = mapper;
        this.events = events;
    }

    /** Lista servicios de un animal en orden cronologico inverso. */
    @Transactional(readOnly = true)
    public List<ServiceEventResponseDto> listByAnimal(Long animalId) {
        return repository.findByAnimalIdOrderByServiceDateDesc(animalId).stream()
            .map(mapper::toDto)
            .toList();
    }

    /** Crea un servicio. Si es IA, requiere y decrementa una pajilla de semen. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public ServiceEventResponseDto create(ServiceEventCreateDto dto) {
        if (dto.serviceType() == ServiceType.AI) {
            if (dto.semenStrawId() == null) {
                throw BusinessException.badRequest(ErrorCode.VALIDATION_ERROR, "AI service requires semen_straw_id");
            }
            semenStrawService.decrementAvailable(dto.semenStrawId());
        }
        if ((dto.serviceType() == ServiceType.AI || dto.serviceType() == ServiceType.NATURAL)
                && dto.bullId() == null) {
            throw BusinessException.badRequest(ErrorCode.VALIDATION_ERROR, "Bull required for AI and NATURAL services");
        }
        ServiceEvent entity = mapper.fromCreate(dto);
        ServiceEvent saved = repository.save(entity);
        events.publishEvent(new ServiceEventChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(saved);
    }

    /** Actualiza un servicio existente. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public ServiceEventResponseDto update(Long id, ServiceEventUpdateDto dto) {
        ServiceEvent entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Service event not found"));
        mapper.applyUpdate(dto, entity);
        events.publishEvent(new ServiceEventChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(entity);
    }

    /** Borra un servicio. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        ServiceEvent entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Service event not found"));
        repository.delete(entity);
        events.publishEvent(new ServiceEventChangedEvent(TenantContext.requireAccountId()));
    }
}
