package com.digitalcow.production.milking;

import com.digitalcow.production.milking.dto.MilkingResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Helper para timeline de ordenos de un animal. */
@RestController
@RequestMapping("/api/v1/animals/{animalId}/milkings")
public class AnimalMilkingsController {

    private final MilkingService service;

    public AnimalMilkingsController(MilkingService service) {
        this.service = service;
    }

    /** Este metodo lista los ordenos del animal. */
    @GetMapping
    public List<MilkingResponseDto> list(@PathVariable Long animalId) {
        return service.listByAnimal(animalId);
    }
}
