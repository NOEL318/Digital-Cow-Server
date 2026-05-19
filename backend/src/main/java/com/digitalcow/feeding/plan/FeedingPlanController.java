package com.digitalcow.feeding.plan;

import com.digitalcow.feeding.plan.dto.FeedingPlanCreateDto;
import com.digitalcow.feeding.plan.dto.FeedingPlanItemCreateDto;
import com.digitalcow.feeding.plan.dto.FeedingPlanItemDto;
import com.digitalcow.feeding.plan.dto.FeedingPlanItemUpdateDto;
import com.digitalcow.feeding.plan.dto.FeedingPlanResponseDto;
import com.digitalcow.feeding.plan.dto.FeedingPlanUpdateDto;
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

/** Endpoints REST de FeedingPlan y sus items anidados. */
@RestController
@RequestMapping("/api/v1/feeding/plans")
public class FeedingPlanController {

    private final FeedingPlanService service;

    public FeedingPlanController(FeedingPlanService service) {
        this.service = service;
    }

    /** Este metodo lista los planes de alimentacion. */
    @GetMapping
    public List<FeedingPlanResponseDto> list() {
        return service.list();
    }

    /** Este metodo devuelve el plan de alimentacion. */
    @GetMapping("/{id}")
    public FeedingPlanResponseDto get(@PathVariable Long id) {
        return service.get(id);
    }

    /** Este metodo crea el plan de alimentacion. */
    @PostMapping
    public ResponseEntity<FeedingPlanResponseDto> create(@Valid @RequestBody FeedingPlanCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza el plan de alimentacion. */
    @PatchMapping("/{id}")
    public FeedingPlanResponseDto update(@PathVariable Long id, @Valid @RequestBody FeedingPlanUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina el plan de alimentacion. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Este metodo agrega el item. */
    @PostMapping("/{id}/items")
    public ResponseEntity<FeedingPlanItemDto> addItem(@PathVariable("id") Long planId,
                                                     @Valid @RequestBody FeedingPlanItemCreateDto dto) {
        return ResponseEntity.status(201).body(service.addItem(planId, dto));
    }

    /** Este metodo actualiza el item. */
    @PatchMapping("/items/{itemId}")
    public FeedingPlanItemDto updateItem(@PathVariable Long itemId,
                                         @Valid @RequestBody FeedingPlanItemUpdateDto dto) {
        return service.updateItem(itemId, dto);
    }

    /** Este metodo elimina el item. */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long itemId) {
        service.removeItem(itemId);
        return ResponseEntity.noContent().build();
    }
}
