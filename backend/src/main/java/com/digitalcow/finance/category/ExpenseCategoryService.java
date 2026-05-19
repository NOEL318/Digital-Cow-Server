package com.digitalcow.finance.category;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.finance.category.dto.ExpenseCategoryCreateDto;
import com.digitalcow.finance.category.dto.ExpenseCategoryResponseDto;
import com.digitalcow.finance.category.dto.ExpenseCategoryUpdateDto;
import com.digitalcow.finance.category.mapper.ExpenseCategoryMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de ExpenseCategory multi-tenant con globals (account_id NULL).
 * Bloquea modificaciones a categorias globales.
 */
@Service
@Transactional
public class ExpenseCategoryService {

    private final ExpenseCategoryRepository repository;
    private final ExpenseCategoryMapper mapper;

    public ExpenseCategoryService(ExpenseCategoryRepository repository, ExpenseCategoryMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /** Lista categorias visibles para el tenant (incluye globales). */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<ExpenseCategoryResponseDto> list() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    /** Devuelve una categoria por id. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public ExpenseCategoryResponseDto get(Long id) {
        ExpenseCategory entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Expense category not found"));
        return mapper.toDto(entity);
    }

    /** Crea una categoria tenant-scoped. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public ExpenseCategoryResponseDto create(ExpenseCategoryCreateDto dto) {
        ExpenseCategory entity = mapper.fromCreate(dto);
        try {
            ExpenseCategory saved = repository.saveAndFlush(entity);
            return mapper.toDto(saved);
        } catch (DataIntegrityViolationException ex) {
            throw BusinessException.conflict(ErrorCode.CONFLICT,
                "Expense category code already exists: " + dto.code());
        }
    }

    /** Actualiza una categoria; bloquea si es global. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public ExpenseCategoryResponseDto update(Long id, ExpenseCategoryUpdateDto dto) {
        ExpenseCategory entity = requireOwn(id);
        mapper.applyUpdate(dto, entity);
        return mapper.toDto(entity);
    }

    /** Borra una categoria; bloquea si es global. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        ExpenseCategory entity = requireOwn(id);
        try {
            repository.delete(entity);
            repository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw BusinessException.conflict(ErrorCode.CONFLICT,
                "Expense category is referenced by expenses");
        }
    }

    private ExpenseCategory requireOwn(Long id) {
        ExpenseCategory entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Expense category not found"));
        if (entity.getAccountId() == null) {
            throw BusinessException.forbidden(ErrorCode.FORBIDDEN, "Cannot modify global category");
        }
        return entity;
    }
}
