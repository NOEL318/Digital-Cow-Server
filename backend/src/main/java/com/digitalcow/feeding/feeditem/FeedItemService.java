package com.digitalcow.feeding.feeditem;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.feeding.feeditem.dto.FeedItemCreateDto;
import com.digitalcow.feeding.feeditem.dto.FeedItemResponseDto;
import com.digitalcow.feeding.feeditem.dto.FeedItemUpdateDto;
import com.digitalcow.feeding.feeditem.mapper.FeedItemMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de FeedItem multi-tenant con globals (account_id NULL).
 * Bloquea modificaciones a items globales.
 */
@Service
@Transactional
public class FeedItemService {

    private final FeedItemRepository repository;
    private final FeedItemMapper mapper;

    public FeedItemService(FeedItemRepository repository, FeedItemMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /** Lista insumos visibles para el tenant (incluye globales). */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<FeedItemResponseDto> list() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    /** Devuelve un insumo. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public FeedItemResponseDto get(Long id) {
        FeedItem fi = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Feed item not found"));
        return mapper.toDto(fi);
    }

    /** Crea un insumo tenant-scoped. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public FeedItemResponseDto create(FeedItemCreateDto dto) {
        FeedItem entity = mapper.fromCreate(dto);
        try {
            FeedItem saved = repository.saveAndFlush(entity);
            return mapper.toDto(saved);
        } catch (DataIntegrityViolationException ex) {
            throw BusinessException.conflict(ErrorCode.CONFLICT,
                "Feed item code already exists: " + dto.code());
        }
    }

    /** Actualiza un insumo; bloquea si es global. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public FeedItemResponseDto update(Long id, FeedItemUpdateDto dto) {
        FeedItem entity = requireOwnItem(id);
        mapper.applyUpdate(dto, entity);
        return mapper.toDto(entity);
    }

    /** Borra un insumo; bloquea si es global. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        FeedItem entity = requireOwnItem(id);
        try {
            repository.delete(entity);
            repository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw BusinessException.conflict(ErrorCode.CONFLICT,
                "Feed item is referenced by plan items or feeding records");
        }
    }

    private FeedItem requireOwnItem(Long id) {
        FeedItem entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Feed item not found"));
        if (entity.getAccountId() == null) {
            throw BusinessException.forbidden(ErrorCode.FORBIDDEN, "Cannot modify global feed item");
        }
        return entity;
    }
}
