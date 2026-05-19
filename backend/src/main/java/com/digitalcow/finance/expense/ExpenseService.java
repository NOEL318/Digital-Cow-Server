package com.digitalcow.finance.expense;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.common.web.CurrentUser;
import com.digitalcow.finance.expense.dto.ExpenseCreateDto;
import com.digitalcow.finance.expense.dto.ExpenseResponseDto;
import com.digitalcow.finance.expense.dto.ExpenseUpdateDto;
import com.digitalcow.finance.expense.event.ExpenseChangedEvent;
import com.digitalcow.finance.expense.mapper.ExpenseMapper;
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

/** Servicio de Expense con filtros via specifications. */
@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository repository;
    private final ExpenseMapper mapper;
    private final ApplicationEventPublisher events;

    public ExpenseService(ExpenseRepository repository, ExpenseMapper mapper, ApplicationEventPublisher events) {
        this.repository = repository;
        this.mapper = mapper;
        this.events = events;
    }

    /** Lista paginada con filtros, ordenada por fecha descendente por defecto. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public Page<ExpenseResponseDto> list(LocalDate from, LocalDate to, Long categoryId,
                                         Long ranchId, Long lotId, Long animalId, Pageable pageable) {
        Specification<Expense> spec = Specification
            .where(ExpenseSpecifications.incurredBetween(from, to))
            .and(ExpenseSpecifications.hasCategory(categoryId))
            .and(ExpenseSpecifications.hasRanch(ranchId))
            .and(ExpenseSpecifications.hasLot(lotId))
            .and(ExpenseSpecifications.hasAnimal(animalId));
        Pageable sorted = pageable.getSort().isSorted()
            ? pageable
            : org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "incurredAt"));
        return repository.findAll(spec, sorted).map(mapper::toDto);
    }

    /** Devuelve un gasto. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public ExpenseResponseDto get(Long id) {
        Expense entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Expense not found"));
        return mapper.toDto(entity);
    }

    /** Crea un gasto. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public ExpenseResponseDto create(ExpenseCreateDto dto) {
        Expense entity = mapper.fromCreate(dto);
        entity.setCreatedByUserId(CurrentUser.require().userId());
        if (entity.getCurrency() == null) entity.setCurrency("MXN");
        Expense saved = repository.save(entity);
        events.publishEvent(new ExpenseChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(saved);
    }

    /** Actualiza un gasto. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public ExpenseResponseDto update(Long id, ExpenseUpdateDto dto) {
        Expense entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Expense not found"));
        mapper.applyUpdate(dto, entity);
        events.publishEvent(new ExpenseChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(entity);
    }

    /** Borra un gasto. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        Expense entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Expense not found"));
        repository.delete(entity);
        events.publishEvent(new ExpenseChangedEvent(TenantContext.requireAccountId()));
    }
}
