package com.digitalcow.feeding.record;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.feeding.feeditem.FeedItem;
import com.digitalcow.feeding.feeditem.FeedItemRepository;
import com.digitalcow.feeding.record.dto.FeedingRecordCreateDto;
import com.digitalcow.feeding.record.dto.FeedingRecordResponseDto;
import com.digitalcow.feeding.record.dto.FeedingRecordUpdateDto;
import com.digitalcow.feeding.record.event.FeedingRecordChangedEvent;
import com.digitalcow.feeding.record.mapper.FeedingRecordMapper;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Servicio CRUD de FeedingRecord. Si cost no viene, calcula
 * cost = total_kg * feed_item.unit_cost (redondeo a 2 decimales).
 */
@Service
@Transactional
public class FeedingRecordService {

    private final FeedingRecordRepository repository;
    private final FeedItemRepository feedItemRepository;
    private final FeedingRecordMapper mapper;
    private final ApplicationEventPublisher events;

    public FeedingRecordService(FeedingRecordRepository repository,
                                FeedItemRepository feedItemRepository,
                                FeedingRecordMapper mapper,
                                ApplicationEventPublisher events) {
        this.repository = repository;
        this.feedItemRepository = feedItemRepository;
        this.mapper = mapper;
        this.events = events;
    }

    /** Este metodo lista los registros de alimentacion. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<FeedingRecordResponseDto> list() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    /** Este metodo crea el registro de alimentacion. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public FeedingRecordResponseDto create(FeedingRecordCreateDto dto) {
        if (dto.lotId() == null && dto.animalId() == null) {
            throw BusinessException.badRequest(ErrorCode.VALIDATION_ERROR,
                "Debe especificar lote o animal a alimentar");
        }
        FeedingRecord entity = mapper.fromCreate(dto);
        if (entity.getCost() == null) {
            entity.setCost(computeCost(entity.getFeedItemId(), entity.getTotalKg()));
        }
        FeedingRecord saved = repository.save(entity);
        events.publishEvent(new FeedingRecordChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(saved);
    }

    /** Este metodo actualiza el registro de alimentacion. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public FeedingRecordResponseDto update(Long id, FeedingRecordUpdateDto dto) {
        FeedingRecord entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Feeding record not found"));
        mapper.applyUpdate(dto, entity);
        events.publishEvent(new FeedingRecordChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(entity);
    }

    /** Este metodo elimina el registro de alimentacion. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        FeedingRecord entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Feeding record not found"));
        repository.delete(entity);
        events.publishEvent(new FeedingRecordChangedEvent(TenantContext.requireAccountId()));
    }

    /**
     * Calcula el costo a partir de total_kg y el unit_cost del feed_item.
     * Devuelve null si el feed_item no tiene unit_cost.
     */
    private BigDecimal computeCost(Long feedItemId, BigDecimal totalKg) {
        FeedItem fi = feedItemRepository.findById(feedItemId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Feed item not found"));
        if (fi.getUnitCost() == null || totalKg == null) return null;
        return totalKg.multiply(fi.getUnitCost()).setScale(2, RoundingMode.HALF_UP);
    }
}
