package com.digitalcow.dashboard;

import com.digitalcow.dashboard.dto.DashboardFinanceDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint REST del widget de finanzas del dashboard. */
@RestController
@RequestMapping("/api/v1/dashboard/finance")
public class DashboardFinanceController {

    private final DashboardFinanceService service;

    public DashboardFinanceController(DashboardFinanceService service) {
        this.service = service;
    }

    /** Este metodo devuelve el dashboard de finanzas. */
    @GetMapping
    public DashboardFinanceDto get() {
        return service.build();
    }
}
