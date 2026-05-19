package com.digitalcow.production.kpis;

import com.digitalcow.production.kpis.dto.ProductionKpisDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Endpoint REST de KPIs de produccion. */
@RestController
@RequestMapping("/api/v1/production/kpis")
public class ProductionKpisController {

    private final ProductionKpisService service;

    public ProductionKpisController(ProductionKpisService service) {
        this.service = service;
    }

    /** Este metodo devuelve los indicadores de produccion. */
    @GetMapping
    public ProductionKpisDto get(
        @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return service.build(from, to);
    }
}
