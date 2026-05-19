package com.digitalcow.animal.comparison;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Endpoint comparativo por animal. Devuelve series mensuales con peso
 * promedio, consumo de alimento estimado (proxy via lote actual),
 * gasto e ingreso. Util para construir graficas multi-serie en la UI.
 */
@RestController
public class AnimalComparisonController {

    private final AnimalComparisonService service;

    public AnimalComparisonController(AnimalComparisonService service) {
        this.service = service;
    }

    /** Este metodo devuelve la comparacion de animales. */
    @GetMapping("/api/v1/animals/{id}/comparison")
    public AnimalComparisonResponse get(
            @PathVariable("id") Long animalId,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate effectiveTo = to == null ? LocalDate.now() : to;
        LocalDate effectiveFrom = from == null ? effectiveTo.minusMonths(11).withDayOfMonth(1) : from;
        return service.compute(animalId, effectiveFrom, effectiveTo);
    }
}
