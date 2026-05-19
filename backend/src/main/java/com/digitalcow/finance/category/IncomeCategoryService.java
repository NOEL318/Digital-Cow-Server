package com.digitalcow.finance.category;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.finance.category.dto.IncomeCategoryCreateDto;
import com.digitalcow.finance.category.dto.IncomeCategoryResponseDto;
import com.digitalcow.finance.category.dto.IncomeCategoryUpdateDto;
import com.digitalcow.finance.category.mapper.IncomeCategoryMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de IncomeCategory multi-tenant con globals (account_id NULL).
 * Bloquea modificaciones a categorias globales.
 */
@Service
@Transactional
public class IncomeCategoryService {

    private final IncomeCategoryRepository repository;
    private final IncomeCategoryMapper mapper;

    public IncomeCategoryService(IncomeCategoryRepository repository, IncomeCategoryMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /** Lista categorias visibles para el tenant (incluye globales). */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<IncomeCategoryResponseDto> list() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    /** Devuelve una categoria por id. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public IncomeCategoryResponseDto get(Long id) {
        IncomeCategory entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Income category not found"));
        return mapper.toDto(entity);
    }

    /** Crea una categoria tenant-scoped. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public IncomeCategoryResponseDto create(IncomeCategoryCreateDto dto) {
        IncomeCategory entity = mapper.fromCreate(dto);
        try {
            IncomeCategory saved = repository.saveAndFlush(entity);
            return mapper.toDto(saved);
        } catch (DataIntegrityViolationException ex) {
            throw BusinessException.conflict(ErrorCode.CONFLICT,
                "Income category code already exists: " + dto.code());
        }
    }

    /** Actualiza una categoria; bloquea si es global. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public IncomeCategoryResponseDto update(Long id, IncomeCategoryUpdateDto dto) {
        IncomeCategory entity = requireOwn(id);
        mapper.applyUpdate(dto, entity);
        return mapper.toDto(entity);
    }

    /** Borra una categoria; bloquea si es global. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        IncomeCategory entity = requireOwn(id);
        try {
            repository.delete(entity);
            repository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw BusinessException.conflict(ErrorCode.CONFLICT,
                "Income category is referenced by incomes");
        }
    }

    private IncomeCategory requireOwn(Long id) {
        IncomeCategory entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Income category not found"));
        if (entity.getAccountId() == null) {
            throw BusinessException.forbidden(ErrorCode.FORBIDDEN, "Cannot modify global category");
        }
        return entity;
    }
}
