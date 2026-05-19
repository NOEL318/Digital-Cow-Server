package com.digitalcow.reproduction.semen;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.reproduction.semen.dto.SemenStrawCreateDto;
import com.digitalcow.reproduction.semen.dto.SemenStrawResponseDto;
import com.digitalcow.reproduction.semen.dto.SemenStrawUpdateDto;
import com.digitalcow.reproduction.semen.event.SemenStrawChangedEvent;
import com.digitalcow.reproduction.semen.mapper.SemenStrawMapper;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Servicio CRUD de SemenStraw con autorizacion por rol e inventario. */
@Service
@Transactional
public class SemenStrawService {

    private final SemenStrawRepository repository;
    private final SemenStrawMapper mapper;
    private final ApplicationEventPublisher events;

    public SemenStrawService(SemenStrawRepository repository,
                             SemenStrawMapper mapper,
                             ApplicationEventPublisher events) {
        this.repository = repository;
        this.mapper = mapper;
        this.events = events;
    }

    /** Lista todas las pajillas de la cuenta. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<SemenStrawResponseDto> list() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    /** Obtiene una pajilla por id. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public SemenStrawResponseDto get(Long id) {
        SemenStraw entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Semen straw not found"));
        return mapper.toDto(entity);
    }

    /** Crea una pajilla. Manager o superior. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public SemenStrawResponseDto create(SemenStrawCreateDto dto) {
        SemenStraw entity = mapper.fromCreate(dto);
        SemenStraw saved = repository.save(entity);
        events.publishEvent(new SemenStrawChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(saved);
    }

    /** Actualiza una pajilla. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public SemenStrawResponseDto update(Long id, SemenStrawUpdateDto dto) {
        SemenStraw entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Semen straw not found"));
        mapper.applyUpdate(dto, entity);
        events.publishEvent(new SemenStrawChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(entity);
    }

    /** Borra una pajilla. Falla si tiene servicios asociados. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public void delete(Long id) {
        SemenStraw entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Semen straw not found"));
        try {
            repository.delete(entity);
            repository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw BusinessException.conflict(ErrorCode.CONFLICT, "Semen straw has associated services");
        }
        events.publishEvent(new SemenStrawChangedEvent(TenantContext.requireAccountId()));
    }

    /**
     * Decrementa available_quantity en 1 al consumir una pajilla en un servicio AI.
     * Falla con CONFLICT si available_quantity es 0.
     *
     * @param strawId id de la pajilla
     */
    public void decrementAvailable(Long strawId) {
        SemenStraw straw = repository.findById(strawId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Semen straw not found"));
        if (straw.getAvailableQuantity() <= 0) {
            throw BusinessException.conflict(ErrorCode.CONFLICT, "Straw has no available doses");
        }
        straw.setAvailableQuantity(straw.getAvailableQuantity() - 1);
        events.publishEvent(new SemenStrawChangedEvent(TenantContext.requireAccountId()));
    }
}
