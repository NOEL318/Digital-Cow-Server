package com.digitalcow.health.plan;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.health.plan.dto.AnimalHealthPlanDto;
import com.digitalcow.health.plan.dto.HealthPlanCreateDto;
import com.digitalcow.health.plan.dto.HealthPlanResponseDto;
import com.digitalcow.health.plan.dto.HealthPlanStepCreateDto;
import com.digitalcow.health.plan.dto.HealthPlanStepDto;
import com.digitalcow.health.plan.dto.HealthPlanStepUpdateDto;
import com.digitalcow.health.plan.dto.HealthPlanUpdateDto;
import com.digitalcow.health.plan.dto.PlanAssignDto;
import com.digitalcow.health.plan.mapper.HealthPlanMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de planes sanitarios. Bloquea modificaciones a planes globales (account_id NULL).
 */
@Service
@Transactional
public class HealthPlanService {

    private final HealthPlanRepository planRepository;
    private final HealthPlanStepRepository stepRepository;
    private final AnimalHealthPlanRepository assignmentRepository;
    private final HealthPlanMapper mapper;

    public HealthPlanService(HealthPlanRepository planRepository,
                             HealthPlanStepRepository stepRepository,
                             AnimalHealthPlanRepository assignmentRepository,
                             HealthPlanMapper mapper) {
        this.planRepository = planRepository;
        this.stepRepository = stepRepository;
        this.assignmentRepository = assignmentRepository;
        this.mapper = mapper;
    }

    /** Lista los planes visibles para el tenant (incluye globales). */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<HealthPlanResponseDto> list() {
        return planRepository.findAll().stream().map(this::toDto).toList();
    }

    /** Devuelve un plan con sus pasos. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public HealthPlanResponseDto get(Long id) {
        HealthPlan plan = planRepository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Health plan not found"));
        return toDto(plan);
    }

    /** Crea un plan tenant-scoped. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public HealthPlanResponseDto create(HealthPlanCreateDto dto) {
        HealthPlan plan = mapper.fromCreate(dto);
        HealthPlan saved = planRepository.save(plan);
        return toDto(saved);
    }

    /** Actualiza un plan; bloquea si es global. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public HealthPlanResponseDto update(Long id, HealthPlanUpdateDto dto) {
        HealthPlan plan = requireOwnPlan(id);
        mapper.applyUpdate(dto, plan);
        return toDto(plan);
    }

    /** Borra un plan; bloquea si es global o tiene asignaciones. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public void delete(Long id) {
        HealthPlan plan = requireOwnPlan(id);
        if (assignmentRepository.existsByHealthPlanId(id)) {
            throw BusinessException.conflict(ErrorCode.CONFLICT, "Plan has active assignments");
        }
        planRepository.delete(plan);
    }

    /** Agrega un paso al plan. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public HealthPlanStepDto addStep(Long planId, HealthPlanStepCreateDto dto) {
        requireOwnPlan(planId);
        HealthPlanStep step = mapper.fromStepCreate(dto);
        step.setHealthPlanId(planId);
        HealthPlanStep saved = stepRepository.save(step);
        return mapper.toStepDto(saved);
    }

    /** Actualiza un paso. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public HealthPlanStepDto updateStep(Long stepId, HealthPlanStepUpdateDto dto) {
        HealthPlanStep step = stepRepository.findById(stepId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Step not found"));
        requireOwnPlan(step.getHealthPlanId());
        mapper.applyStepUpdate(dto, step);
        return mapper.toStepDto(step);
    }

    /** Borra un paso. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void deleteStep(Long stepId) {
        HealthPlanStep step = stepRepository.findById(stepId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Step not found"));
        requireOwnPlan(step.getHealthPlanId());
        stepRepository.delete(step);
    }

    /** Asigna el plan a una lista de animales y/o lotes. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public List<AnimalHealthPlanDto> assign(Long planId, PlanAssignDto dto) {
        planRepository.findById(planId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Health plan not found"));
        List<AnimalHealthPlan> created = new ArrayList<>();
        if (dto.animalIds() != null) {
            for (Long animalId : dto.animalIds()) {
                AnimalHealthPlan a = new AnimalHealthPlan();
                a.setHealthPlanId(planId);
                a.setAnimalId(animalId);
                a.setAssignedAt(dto.assignedAt());
                created.add(a);
            }
        }
        if (dto.lotIds() != null) {
            for (Long lotId : dto.lotIds()) {
                AnimalHealthPlan a = new AnimalHealthPlan();
                a.setHealthPlanId(planId);
                a.setLotId(lotId);
                a.setAssignedAt(dto.assignedAt());
                created.add(a);
            }
        }
        return assignmentRepository.saveAll(created).stream().map(mapper::toAssignDto).toList();
    }

    /** Desasigna un plan. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void unassign(Long assignmentId) {
        AnimalHealthPlan a = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Assignment not found"));
        assignmentRepository.delete(a);
    }

    /** Carga un plan que NO sea global. Lanza FORBIDDEN si lo es. */
    private HealthPlan requireOwnPlan(Long id) {
        HealthPlan plan = planRepository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Health plan not found"));
        if (plan.getAccountId() == null) {
            throw BusinessException.forbidden(ErrorCode.FORBIDDEN, "Cannot modify global plan");
        }
        return plan;
    }

    private HealthPlanResponseDto toDto(HealthPlan plan) {
        List<HealthPlanStepDto> steps = stepRepository
            .findByHealthPlanIdOrderByStepOrder(plan.getId())
            .stream().map(mapper::toStepDto).toList();
        return new HealthPlanResponseDto(
            plan.getId(),
            plan.getAccountId(),
            plan.getAccountId() == null,
            plan.getName(),
            plan.getDescription(),
            plan.getAppliesToPurpose(),
            plan.getAppliesToSex(),
            steps
        );
    }
}
