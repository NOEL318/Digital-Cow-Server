package com.digitalcow.production.growth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint REST de la curva de crecimiento de un animal. */
@RestController
@RequestMapping("/api/v1/production/growth-curve")
public class GrowthCurveController {

    private final GrowthCurveService service;

    public GrowthCurveController(GrowthCurveService service) {
        this.service = service;
    }

    /** Este metodo devuelve la curva de crecimiento. */
    @GetMapping("/{animalId}")
    public GrowthCurveDto get(@PathVariable Long animalId) {
        return service.build(animalId);
    }
}
