package com.digitalcow.reproduction.kpis;

import com.digitalcow.reproduction.kpis.dto.ReproductionKpisDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Endpoint REST de KPIs reproductivos del tenant. */
@RestController
@RequestMapping("/api/v1/reproduction/kpis")
public class ReproductionKpisController {

    private final ReproductionKpisService service;

    public ReproductionKpisController(ReproductionKpisService service) {
        this.service = service;
    }

    /** Este metodo devuelve los indicadores de reproduccion. */
    @GetMapping
    public ReproductionKpisDto get(
        @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return service.build(from, to);
    }
}
