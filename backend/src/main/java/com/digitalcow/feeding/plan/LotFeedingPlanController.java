package com.digitalcow.feeding.plan;

import com.digitalcow.feeding.plan.dto.LotFeedingPlanCreateDto;
import com.digitalcow.feeding.plan.dto.LotFeedingPlanResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Endpoints REST de asignaciones plan-lote. */
@RestController
@RequestMapping("/api/v1/feeding/lot-assignments")
public class LotFeedingPlanController {

    private final FeedingPlanService service;

    public LotFeedingPlanController(FeedingPlanService service) {
        this.service = service;
    }

    /** Este metodo lista los planes de alimentacion asignados a un lote. */
    @GetMapping
    public List<LotFeedingPlanResponseDto> listByLot(@RequestParam("lotId") Long lotId) {
        return service.listByLot(lotId);
    }

    /** Este metodo asigna el plan de alimentacion del lote. */
    @PostMapping
    public ResponseEntity<LotFeedingPlanResponseDto> assign(@Valid @RequestBody LotFeedingPlanCreateDto dto) {
        return ResponseEntity.status(201).body(service.assignToLot(dto));
    }

    /** Este metodo desasigna el plan de alimentacion del lote. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> unassign(@PathVariable Long id) {
        service.unassignFromLot(id);
        return ResponseEntity.noContent().build();
    }
}
