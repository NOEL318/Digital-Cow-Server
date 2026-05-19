package com.digitalcow.finance.costunit;

import com.digitalcow.finance.costunit.dto.CostPerUnitDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Endpoint REST del costo por unidad. */
@RestController
@RequestMapping("/api/v1/finance/cost-per-unit")
public class CostPerUnitController {

    private final CostPerUnitService service;

    public CostPerUnitController(CostPerUnitService service) {
        this.service = service;
    }

    /** Este metodo devuelve el costo por unidad. */
    @GetMapping
    public CostPerUnitDto get(
        @RequestParam LocalDate from,
        @RequestParam LocalDate to,
        @RequestParam String purpose
    ) {
        return service.build(from, to, purpose);
    }
}
