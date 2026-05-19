package com.digitalcow.finance.pnl;

import com.digitalcow.finance.pnl.dto.PnlDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Endpoint REST del P&L. */
@RestController
@RequestMapping("/api/v1/finance/pnl")
public class PnlController {

    private final PnlService service;

    public PnlController(PnlService service) {
        this.service = service;
    }

    /** Este metodo devuelve el estado de resultados. */
    @GetMapping
    public PnlDto get(
        @RequestParam LocalDate from,
        @RequestParam LocalDate to,
        @RequestParam(required = false, defaultValue = "month") String groupBy
    ) {
        return service.build(from, to, groupBy);
    }
}
