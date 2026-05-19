package com.digitalcow.production.milksample;

import com.digitalcow.production.milksample.dto.MilkSampleResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Helper para timeline de muestras de leche de un animal. */
@RestController
@RequestMapping("/api/v1/animals/{animalId}/milk-samples")
public class AnimalMilkSamplesController {

    private final MilkSampleService service;

    public AnimalMilkSamplesController(MilkSampleService service) {
        this.service = service;
    }

    /** Este metodo lista las muestras de leche del animal. */
    @GetMapping
    public List<MilkSampleResponseDto> list(@PathVariable Long animalId) {
        return service.listByAnimal(animalId);
    }
}
