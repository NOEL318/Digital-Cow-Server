package com.digitalcow.reproduction.bull;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.reproduction.bull.dto.BullCreateDto;
import com.digitalcow.reproduction.bull.dto.BullResponseDto;
import com.digitalcow.reproduction.bull.dto.BullUpdateDto;
import com.digitalcow.reproduction.bull.event.BullChangedEvent;
import com.digitalcow.reproduction.bull.mapper.BullMapper;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Servicio CRUD de Bull con autorizacion por rol. */
@Service
@Transactional
public class BullService {

    private final BullRepository repository;
    private final BullMapper mapper;
    private final ApplicationEventPublisher events;

    public BullService(BullRepository repository, BullMapper mapper, ApplicationEventPublisher events) {
        this.repository = repository;
        this.mapper = mapper;
        this.events = events;
    }

    /** Lista todos los toros de la cuenta. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<BullResponseDto> list() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    /** Obtiene un toro por id. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public BullResponseDto get(Long id) {
        Bull entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Bull not found"));
        return mapper.toDto(entity);
    }

    /** Crea un toro. Manager o superior. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public BullResponseDto create(BullCreateDto dto) {
        if (repository.existsByInternalCode(dto.internalCode())) {
            throw BusinessException.conflict(ErrorCode.CONFLICT, "Internal code already used");
        }
        Bull entity = mapper.fromCreate(dto);
        Bull saved = repository.save(entity);
        events.publishEvent(new BullChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(saved);
    }

    /** Actualiza un toro por id. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public BullResponseDto update(Long id, BullUpdateDto dto) {
        Bull entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Bull not found"));
        mapper.applyUpdate(dto, entity);
        events.publishEvent(new BullChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(entity);
    }

    /** Borra un toro. Falla si tiene pajillas o servicios asociados. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public void delete(Long id) {
        Bull entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Bull not found"));
        try {
            repository.delete(entity);
            repository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw BusinessException.conflict(ErrorCode.CONFLICT, "Bull has associated semen straws or services");
        }
        events.publishEvent(new BullChangedEvent(TenantContext.requireAccountId()));
    }
}
