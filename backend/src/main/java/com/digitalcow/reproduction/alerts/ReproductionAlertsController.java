package com.digitalcow.reproduction.alerts;

import com.digitalcow.reproduction.alerts.dto.ReproductionAlertsDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint REST de alertas reproductivas del tenant. */
@RestController
@RequestMapping("/api/v1/reproduction/alerts")
public class ReproductionAlertsController {

    private final ReproductionAlertsService service;

    public ReproductionAlertsController(ReproductionAlertsService service) {
        this.service = service;
    }

    /** Este metodo devuelve las alertas de reproduccion. */
    @GetMapping
    public ReproductionAlertsDto get() {
        return service.build();
    }
}
