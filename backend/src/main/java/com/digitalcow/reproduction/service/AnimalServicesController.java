package com.digitalcow.reproduction.service;

import com.digitalcow.reproduction.service.dto.ServiceEventResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Helper para timeline reproductivo de un animal.
 */
@RestController
@RequestMapping("/api/v1/animals/{animalId}/services")
public class AnimalServicesController {

    private final ServiceEventService service;

    public AnimalServicesController(ServiceEventService service) {
        this.service = service;
    }

    /** Este metodo lista los servicios reproductivos del animal. */
    @GetMapping
    public List<ServiceEventResponseDto> list(@PathVariable Long animalId) {
        return service.listByAnimal(animalId);
    }
}
