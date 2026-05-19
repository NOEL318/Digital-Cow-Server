package com.digitalcow.feeding.plan;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.feeding.plan.dto.FeedingPlanCreateDto;
import com.digitalcow.feeding.plan.dto.FeedingPlanItemCreateDto;
import com.digitalcow.feeding.plan.dto.FeedingPlanItemDto;
import com.digitalcow.feeding.plan.dto.FeedingPlanItemUpdateDto;
import com.digitalcow.feeding.plan.dto.FeedingPlanResponseDto;
import com.digitalcow.feeding.plan.dto.FeedingPlanUpdateDto;
import com.digitalcow.feeding.plan.dto.LotFeedingPlanCreateDto;
import com.digitalcow.feeding.plan.dto.LotFeedingPlanResponseDto;
import com.digitalcow.feeding.plan.mapper.FeedingPlanMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de planes de alimentacion, sus items y asignaciones a lotes.
 * Borra plan solo si no tiene asignaciones activas (unassigned_at IS NULL).
 */
@Service
@Transactional
public class FeedingPlanService {

    private final FeedingPlanRepository planRepository;
    private final FeedingPlanItemRepository itemRepository;
    private final LotFeedingPlanRepository assignmentRepository;
    private final FeedingPlanMapper mapper;

    public FeedingPlanService(FeedingPlanRepository planRepository,
                              FeedingPlanItemRepository itemRepository,
                              LotFeedingPlanRepository assignmentRepository,
                              FeedingPlanMapper mapper) {
        this.planRepository = planRepository;
        this.itemRepository = itemRepository;
        this.assignmentRepository = assignmentRepository;
        this.mapper = mapper;
    }

    /** Lista todos los planes del tenant. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<FeedingPlanResponseDto> list() {
        return planRepository.findAll().stream().map(this::toDto).toList();
    }

    /** Devuelve un plan con sus items. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public FeedingPlanResponseDto get(Long id) {
        FeedingPlan plan = planRepository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Feeding plan not found"));
        return toDto(plan);
    }

    /** Crea un plan. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public FeedingPlanResponseDto create(FeedingPlanCreateDto dto) {
        FeedingPlan plan = mapper.fromCreate(dto);
        FeedingPlan saved = planRepository.save(plan);
        return toDto(saved);
    }

    /** Actualiza un plan. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public FeedingPlanResponseDto update(Long id, FeedingPlanUpdateDto dto) {
        FeedingPlan plan = requirePlan(id);
        mapper.applyUpdate(dto, plan);
        return toDto(plan);
    }

    /** Borra un plan; bloquea si tiene asignaciones activas. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        FeedingPlan plan = requirePlan(id);
        if (!assignmentRepository.findByFeedingPlanIdAndUnassignedAtIsNull(id).isEmpty()) {
            throw BusinessException.conflict(ErrorCode.CONFLICT, "Feeding plan has active lot assignments");
        }
        itemRepository.deleteByFeedingPlanId(id);
        planRepository.delete(plan);
    }

    /** Agrega un item al plan. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public FeedingPlanItemDto addItem(Long planId, FeedingPlanItemCreateDto dto) {
        requirePlan(planId);
        FeedingPlanItem item = mapper.fromItemCreate(dto);
        item.setFeedingPlanId(planId);
        FeedingPlanItem saved = itemRepository.save(item);
        return mapper.toItemDto(saved);
    }

    /** Actualiza un item. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public FeedingPlanItemDto updateItem(Long itemId, FeedingPlanItemUpdateDto dto) {
        FeedingPlanItem item = itemRepository.findById(itemId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Plan item not found"));
        requirePlan(item.getFeedingPlanId());
        mapper.applyItemUpdate(dto, item);
        return mapper.toItemDto(item);
    }

    /** Borra un item. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void removeItem(Long itemId) {
        FeedingPlanItem item = itemRepository.findById(itemId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Plan item not found"));
        requirePlan(item.getFeedingPlanId());
        itemRepository.delete(item);
    }

    /** Asigna el plan a un lote en una fecha. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public LotFeedingPlanResponseDto assignToLot(LotFeedingPlanCreateDto dto) {
        requirePlan(dto.feedingPlanId());
        LotFeedingPlan asg = new LotFeedingPlan();
        asg.setLotId(dto.lotId());
        asg.setFeedingPlanId(dto.feedingPlanId());
        asg.setAssignedAt(dto.assignedAt());
        LotFeedingPlan saved = assignmentRepository.save(asg);
        return mapper.toLotAssignDto(saved);
    }

    /** Desasigna marcando unassigned_at = hoy. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void unassignFromLot(Long assignmentId) {
        LotFeedingPlan asg = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Assignment not found"));
        if (asg.getUnassignedAt() != null) {
            throw BusinessException.conflict(ErrorCode.CONFLICT, "Assignment already closed");
        }
        asg.setUnassignedAt(LocalDate.now());
    }

    /** Lista asignaciones de un lote. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<LotFeedingPlanResponseDto> listByLot(Long lotId) {
        return assignmentRepository.findByLotIdOrderByAssignedAtDesc(lotId)
            .stream().map(mapper::toLotAssignDto).toList();
    }

    private FeedingPlan requirePlan(Long id) {
        return planRepository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Feeding plan not found"));
    }

    private FeedingPlanResponseDto toDto(FeedingPlan plan) {
        List<FeedingPlanItemDto> items = itemRepository
            .findByFeedingPlanIdOrderByIdAsc(plan.getId())
            .stream().map(mapper::toItemDto).toList();
        return new FeedingPlanResponseDto(
            plan.getId(),
            plan.getName(),
            plan.getCategory(),
            plan.getDescription(),
            items
        );
    }
}
