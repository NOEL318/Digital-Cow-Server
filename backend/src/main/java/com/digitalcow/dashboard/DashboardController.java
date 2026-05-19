package com.digitalcow.dashboard;

import com.digitalcow.dashboard.dto.DashboardSummary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint del dashboard del tenant. */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService svc;

    public DashboardController(DashboardService svc) { this.svc = svc; }

    /** Este metodo devuelve el resumen general. */
    @GetMapping("/summary")
    public DashboardSummary summary() { return svc.summary(); }
}
