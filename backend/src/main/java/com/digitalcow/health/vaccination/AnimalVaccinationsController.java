package com.digitalcow.health.vaccination;

import com.digitalcow.health.vaccination.dto.VaccinationResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Helper para timeline de vacunaciones de un animal. */
@RestController
@RequestMapping("/api/v1/animals/{animalId}/vaccinations")
public class AnimalVaccinationsController {

    private final VaccinationService service;

    public AnimalVaccinationsController(VaccinationService service) {
        this.service = service;
    }

    /** Este metodo lista las vacunaciones del animal. */
    @GetMapping
    public List<VaccinationResponseDto> list(@PathVariable Long animalId) {
        return service.listByAnimal(animalId);
    }
}
