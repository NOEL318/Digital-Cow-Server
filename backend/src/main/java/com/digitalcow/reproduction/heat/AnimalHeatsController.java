package com.digitalcow.reproduction.heat;

import com.digitalcow.reproduction.heat.dto.HeatResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Helper para timeline de celos de un animal. */
@RestController
@RequestMapping("/api/v1/animals/{animalId}/heats")
public class AnimalHeatsController {

    private final HeatService service;

    public AnimalHeatsController(HeatService service) {
        this.service = service;
    }

    /** Este metodo lista los celos del animal. */
    @GetMapping
    public List<HeatResponseDto> list(@PathVariable Long animalId) {
        return service.listByAnimal(animalId);
    }
}
