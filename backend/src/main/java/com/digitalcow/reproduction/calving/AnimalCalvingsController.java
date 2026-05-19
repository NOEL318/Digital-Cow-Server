package com.digitalcow.reproduction.calving;

import com.digitalcow.reproduction.calving.dto.CalvingResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Helper para timeline de partos de un animal. */
@RestController
@RequestMapping("/api/v1/animals/{animalId}/calvings")
public class AnimalCalvingsController {

    private final CalvingService service;

    public AnimalCalvingsController(CalvingService service) {
        this.service = service;
    }

    /** Este metodo lista los partos del animal. */
    @GetMapping
    public List<CalvingResponseDto> list(@PathVariable Long animalId) {
        return service.listByAnimal(animalId);
    }
}
