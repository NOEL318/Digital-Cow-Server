package com.digitalcow.production.bulktank;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.production.bulktank.dto.BulkTankDeliveryCreateDto;
import com.digitalcow.production.bulktank.dto.BulkTankDeliveryResponseDto;
import com.digitalcow.production.bulktank.dto.BulkTankDeliveryUpdateDto;
import com.digitalcow.production.bulktank.event.BulkTankDeliveryChangedEvent;
import com.digitalcow.production.bulktank.mapper.BulkTankDeliveryMapper;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Servicio CRUD de BulkTankDelivery. */
@Service
@Transactional
public class BulkTankDeliveryService {

    private final BulkTankDeliveryRepository repository;
    private final BulkTankDeliveryMapper mapper;
    private final ApplicationEventPublisher events;

    public BulkTankDeliveryService(BulkTankDeliveryRepository repository,
                                   BulkTankDeliveryMapper mapper,
                                   ApplicationEventPublisher events) {
        this.repository = repository;
        this.mapper = mapper;
        this.events = events;
    }

    /** Este metodo lista las entregas al tanque. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<BulkTankDeliveryResponseDto> list() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    /** Este metodo crea la entrega al tanque. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public BulkTankDeliveryResponseDto create(BulkTankDeliveryCreateDto dto) {
        BulkTankDelivery entity = mapper.fromCreate(dto);
        BulkTankDelivery saved = repository.save(entity);
        events.publishEvent(new BulkTankDeliveryChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(saved);
    }

    /** Este metodo actualiza la entrega al tanque. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public BulkTankDeliveryResponseDto update(Long id, BulkTankDeliveryUpdateDto dto) {
        BulkTankDelivery entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Bulk tank delivery not found"));
        mapper.applyUpdate(dto, entity);
        events.publishEvent(new BulkTankDeliveryChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(entity);
    }

    /** Este metodo elimina la entrega al tanque. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        BulkTankDelivery entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Bulk tank delivery not found"));
        repository.delete(entity);
        events.publishEvent(new BulkTankDeliveryChangedEvent(TenantContext.requireAccountId()));
    }
}
