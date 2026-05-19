package com.digitalcow.health.diagnosis;

import com.digitalcow.health.diagnosis.dto.DiagnosisResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Helper para timeline de diagnosticos de un animal. */
@RestController
@RequestMapping("/api/v1/animals/{animalId}/diagnoses")
public class AnimalDiagnosesController {

    private final DiagnosisService service;

    public AnimalDiagnosesController(DiagnosisService service) {
        this.service = service;
    }

    /** Este metodo lista los diagnosticos del animal. */
    @GetMapping
    public List<DiagnosisResponseDto> list(@PathVariable Long animalId) {
        return service.listByAnimal(animalId);
    }
}
