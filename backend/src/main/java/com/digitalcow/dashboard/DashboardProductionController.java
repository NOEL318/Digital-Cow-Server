package com.digitalcow.dashboard;

import com.digitalcow.dashboard.dto.DashboardProductionDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint REST del widget de produccion del dashboard. */
@RestController
@RequestMapping("/api/v1/dashboard/production")
public class DashboardProductionController {

    private final DashboardProductionService service;

    public DashboardProductionController(DashboardProductionService service) {
        this.service = service;
    }

    /** Este metodo devuelve el dashboard de produccion. */
    @GetMapping
    public DashboardProductionDto get() {
        return service.build();
    }
}
