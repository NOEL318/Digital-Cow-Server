package com.digitalcow.finance.cashflow;

import com.digitalcow.finance.cashflow.dto.CashFlowDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Endpoint REST del flujo de caja anual. */
@RestController
@RequestMapping("/api/v1/finance/cash-flow")
public class CashFlowController {

    private final CashFlowService service;

    public CashFlowController(CashFlowService service) {
        this.service = service;
    }

    /** Este metodo devuelve el flujo de caja. */
    @GetMapping
    public CashFlowDto get(@RequestParam(required = false) Integer year) {
        int y = year != null ? year : LocalDate.now().getYear();
        return service.build(y);
    }
}
