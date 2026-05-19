package com.digitalcow.production.lactation;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Endpoint REST de la curva de lactancia de un animal. */
@RestController
@RequestMapping("/api/v1/production/lactation-curve")
public class LactationCurveController {

    private final LactationCurveService service;

    public LactationCurveController(LactationCurveService service) {
        this.service = service;
    }

    /** Este metodo devuelve la curva de lactancia. */
    @GetMapping("/{animalId}")
    public LactationCurveDto get(
        @PathVariable Long animalId,
        @RequestParam(value = "lactationStartDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate lactationStartDate
    ) {
        return service.build(animalId, lactationStartDate);
    }
}
