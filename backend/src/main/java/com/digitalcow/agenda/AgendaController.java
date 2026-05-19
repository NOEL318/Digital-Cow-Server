package com.digitalcow.agenda;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Lista del dia: que hay que hacer hoy y manana. Agrega tareas de
 * varios modulos (vacunas atrasadas, pesajes pendientes, secados
 * proximos, partos esperados) en un solo feed simple para que el
 * ranchero abra la app y vea de inmediato sus pendientes.
 */
@RestController
public class AgendaController {

    private final AgendaService service;

    public AgendaController(AgendaService service) {
        this.service = service;
    }

    /** Este metodo devuelve los pendientes del rancho para hoy y los proximos dias. */
    @GetMapping("/api/v1/agenda/today")
    public List<AgendaItem> today() {
        return service.today();
    }
}
