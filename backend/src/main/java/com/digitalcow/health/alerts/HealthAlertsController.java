package com.digitalcow.health.alerts;

import com.digitalcow.health.alerts.dto.HealthAlertsDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint REST de alertas sanitarias del tenant. */
@RestController
@RequestMapping("/api/v1/health/alerts")
public class HealthAlertsController {

    private final HealthAlertsService service;

    public HealthAlertsController(HealthAlertsService service) {
        this.service = service;
    }

    /** Este metodo devuelve las alertas de salud. */
    @GetMapping
    public HealthAlertsDto get() {
        return service.build();
    }
}
