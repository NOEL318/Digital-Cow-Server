package com.digitalcow.health.treatment;

import com.digitalcow.health.treatment.dto.TreatmentResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Helper para timeline de tratamientos de un animal. */
@RestController
@RequestMapping("/api/v1/animals/{animalId}/treatments")
public class AnimalTreatmentsController {

    private final TreatmentService service;

    public AnimalTreatmentsController(TreatmentService service) {
        this.service = service;
    }

    /** Este metodo lista los tratamientos del animal. */
    @GetMapping
    public List<TreatmentResponseDto> list(@PathVariable Long animalId) {
        return service.listByAnimal(animalId);
    }
}
