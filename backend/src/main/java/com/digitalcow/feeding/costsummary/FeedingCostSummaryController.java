package com.digitalcow.feeding.costsummary;

import com.digitalcow.feeding.costsummary.dto.FeedingCostSummaryDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Endpoint REST del resumen de costos de alimentacion. */
@RestController
@RequestMapping("/api/v1/feeding/cost-summary")
public class FeedingCostSummaryController {

    private final FeedingCostSummaryService service;

    public FeedingCostSummaryController(FeedingCostSummaryService service) {
        this.service = service;
    }

    /** Este metodo devuelve el resumen de costos de alimentacion. */
    @GetMapping
    public FeedingCostSummaryDto get(
        @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(value = "groupBy", defaultValue = "lot") String groupBy
    ) {
        return service.build(from, to, groupBy);
    }
}
