package com.digitalcow.dashboard;

import com.digitalcow.dashboard.dto.DashboardReproductionDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint REST del widget de reproduccion del dashboard. */
@RestController
@RequestMapping("/api/v1/dashboard/reproduction")
public class DashboardReproductionController {

    private final DashboardReproductionService service;

    public DashboardReproductionController(DashboardReproductionService service) {
        this.service = service;
    }

    /** Este metodo devuelve el dashboard de reproduccion. */
    @GetMapping
    public DashboardReproductionDto get() {
        return service.build();
    }
}
