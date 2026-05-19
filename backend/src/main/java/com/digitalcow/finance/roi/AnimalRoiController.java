package com.digitalcow.finance.roi;

import com.digitalcow.finance.roi.dto.AnimalRoiDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint REST del ROI por animal. */
@RestController
@RequestMapping("/api/v1/finance/animal-roi")
public class AnimalRoiController {

    private final AnimalRoiService service;

    public AnimalRoiController(AnimalRoiService service) {
        this.service = service;
    }

    /** Este metodo devuelve el retorno sobre inversion del animal. */
    @GetMapping("/{animalId}")
    public AnimalRoiDto get(@PathVariable Long animalId) {
        return service.compute(animalId);
    }
}
