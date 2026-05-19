package com.digitalcow.finance.income;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.common.web.CurrentUser;
import com.digitalcow.finance.income.dto.IncomeCreateDto;
import com.digitalcow.finance.income.dto.IncomeResponseDto;
import com.digitalcow.finance.income.dto.IncomeUpdateDto;
import com.digitalcow.finance.income.event.IncomeChangedEvent;
import com.digitalcow.finance.income.mapper.IncomeMapper;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** Servicio de Income con filtros via specifications. */
@Service
@Transactional
public class IncomeService {

    private final IncomeRepository repository;
    private final IncomeMapper mapper;
    private final ApplicationEventPublisher events;

    public IncomeService(IncomeRepository repository, IncomeMapper mapper, ApplicationEventPublisher events) {
        this.repository = repository;
        this.mapper = mapper;
        this.events = events;
    }

    /** Lista paginada con filtros, por defecto ordenada por fecha descendente. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public Page<IncomeResponseDto> list(LocalDate from, LocalDate to, Long categoryId,
                                        Long ranchId, Long lotId, Long animalId,
                                        IncomeSourceType sourceType, Pageable pageable) {
        Specification<Income> spec = Specification
            .where(IncomeSpecifications.receivedBetween(from, to))
            .and(IncomeSpecifications.hasCategory(categoryId))
            .and(IncomeSpecifications.hasRanch(ranchId))
            .and(IncomeSpecifications.hasLot(lotId))
            .and(IncomeSpecifications.hasAnimal(animalId))
            .and(IncomeSpecifications.hasSourceType(sourceType));
        Pageable sorted = pageable.getSort().isSorted()
            ? pageable
            : org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "receivedAt"));
        return repository.findAll(spec, sorted).map(mapper::toDto);
    }

    /** Devuelve un ingreso. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public IncomeResponseDto get(Long id) {
        Income entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Income not found"));
        return mapper.toDto(entity);
    }

    /** Crea un ingreso manual. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public IncomeResponseDto create(IncomeCreateDto dto) {
        Income entity = mapper.fromCreate(dto);
        entity.setCreatedByUserId(CurrentUser.require().userId());
        entity.setSourceType(IncomeSourceType.MANUAL);
        if (entity.getCurrency() == null) entity.setCurrency("MXN");
        Income saved = repository.save(entity);
        events.publishEvent(new IncomeChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(saved);
    }

    /** Actualiza un ingreso. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public IncomeResponseDto update(Long id, IncomeUpdateDto dto) {
        Income entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Income not found"));
        if (entity.getSourceType() != IncomeSourceType.MANUAL) {
            throw BusinessException.forbidden(ErrorCode.FORBIDDEN,
                "Cannot edit auto-generated income; modify the source sale instead");
        }
        mapper.applyUpdate(dto, entity);
        events.publishEvent(new IncomeChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(entity);
    }

    /** Borra un ingreso manual; los automaticos solo se borran al borrar la venta. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        Income entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Income not found"));
        if (entity.getSourceType() != IncomeSourceType.MANUAL) {
            throw BusinessException.forbidden(ErrorCode.FORBIDDEN,
                "Cannot delete auto-generated income; delete the source sale instead");
        }
        repository.delete(entity);
        events.publishEvent(new IncomeChangedEvent(TenantContext.requireAccountId()));
    }
}
