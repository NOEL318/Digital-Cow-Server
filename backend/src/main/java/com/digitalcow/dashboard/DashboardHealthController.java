package com.digitalcow.dashboard;

import com.digitalcow.dashboard.dto.DashboardHealthDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint REST del widget de salud del dashboard. */
@RestController
@RequestMapping("/api/v1/dashboard/health")
public class DashboardHealthController {

    private final DashboardHealthService service;

    public DashboardHealthController(DashboardHealthService service) {
        this.service = service;
    }

    /** Este metodo devuelve el dashboard de salud. */
    @GetMapping
    public DashboardHealthDto get() {
        return service.build();
    }
}
