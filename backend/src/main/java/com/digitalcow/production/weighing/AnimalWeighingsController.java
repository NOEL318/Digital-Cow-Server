package com.digitalcow.production.weighing;

import com.digitalcow.production.weighing.dto.WeighingResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Helper para timeline de pesajes de un animal. */
@RestController
@RequestMapping("/api/v1/animals/{animalId}/weighings")
public class AnimalWeighingsController {

    private final WeighingService service;

    public AnimalWeighingsController(WeighingService service) {
        this.service = service;
    }

    /** Este metodo lista las pesadas del animal. */
    @GetMapping
    public List<WeighingResponseDto> list(@PathVariable Long animalId) {
        return service.listByAnimal(animalId);
    }
}
