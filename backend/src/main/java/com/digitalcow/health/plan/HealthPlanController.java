package com.digitalcow.health.plan;

import com.digitalcow.health.plan.dto.AnimalHealthPlanDto;
import com.digitalcow.health.plan.dto.HealthPlanCreateDto;
import com.digitalcow.health.plan.dto.HealthPlanResponseDto;
import com.digitalcow.health.plan.dto.HealthPlanStepCreateDto;
import com.digitalcow.health.plan.dto.HealthPlanStepDto;
import com.digitalcow.health.plan.dto.HealthPlanStepUpdateDto;
import com.digitalcow.health.plan.dto.HealthPlanUpdateDto;
import com.digitalcow.health.plan.dto.PlanAssignDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Endpoints REST de HealthPlan, sus pasos y asignaciones. */
@RestController
@RequestMapping("/api/v1/health/plans")
public class HealthPlanController {

    private final HealthPlanService service;

    public HealthPlanController(HealthPlanService service) {
        this.service = service;
    }

    /** Este metodo lista los planes sanitarios. */
    @GetMapping
    public List<HealthPlanResponseDto> list() {
        return service.list();
    }

    /** Este metodo devuelve el plan sanitario. */
    @GetMapping("/{id}")
    public HealthPlanResponseDto get(@PathVariable Long id) {
        return service.get(id);
    }

    /** Este metodo crea el plan sanitario. */
    @PostMapping
    public ResponseEntity<HealthPlanResponseDto> create(@Valid @RequestBody HealthPlanCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza el plan sanitario. */
    @PatchMapping("/{id}")
    public HealthPlanResponseDto update(@PathVariable Long id, @Valid @RequestBody HealthPlanUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina el plan sanitario. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Este metodo agrega un paso al plan sanitario. */
    @PostMapping("/{id}/steps")
    public ResponseEntity<HealthPlanStepDto> addStep(@PathVariable Long id,
                                                     @Valid @RequestBody HealthPlanStepCreateDto dto) {
        return ResponseEntity.status(201).body(service.addStep(id, dto));
    }

    /** Este metodo actualiza un paso del plan sanitario. */
    @PatchMapping("/steps/{stepId}")
    public HealthPlanStepDto updateStep(@PathVariable Long stepId,
                                        @Valid @RequestBody HealthPlanStepUpdateDto dto) {
        return service.updateStep(stepId, dto);
    }

    /** Este metodo elimina un paso del plan sanitario. */
    @DeleteMapping("/steps/{stepId}")
    public ResponseEntity<Void> deleteStep(@PathVariable Long stepId) {
        service.deleteStep(stepId);
        return ResponseEntity.noContent().build();
    }

    /** Este metodo asigna el plan sanitario a uno o varios animales. */
    @PostMapping("/{id}/assignments")
    public ResponseEntity<List<AnimalHealthPlanDto>> assign(@PathVariable Long id,
                                                            @Valid @RequestBody PlanAssignDto dto) {
        return ResponseEntity.status(201).body(service.assign(id, dto));
    }

    /** Este metodo desasigna un animal del plan sanitario. */
    @DeleteMapping("/assignments/{assignmentId}")
    public ResponseEntity<Void> unassign(@PathVariable Long assignmentId) {
        service.unassign(assignmentId);
        return ResponseEntity.noContent().build();
    }
}
