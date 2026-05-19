package com.digitalcow.reproduction.service;

import com.digitalcow.reproduction.service.dto.ServiceEventCreateDto;
import com.digitalcow.reproduction.service.dto.ServiceEventResponseDto;
import com.digitalcow.reproduction.service.dto.ServiceEventUpdateDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints REST de servicios reproductivos.
 */
@RestController
@RequestMapping("/api/v1/reproduction/services")
public class ServiceEventController {

    private final ServiceEventService service;

    public ServiceEventController(ServiceEventService service) {
        this.service = service;
    }

    /** Este metodo crea el servicio reproductivo. */
    @PostMapping
    public ResponseEntity<ServiceEventResponseDto> create(@Valid @RequestBody ServiceEventCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza el servicio reproductivo. */
    @PatchMapping("/{id}")
    public ServiceEventResponseDto update(@PathVariable Long id, @Valid @RequestBody ServiceEventUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina el servicio reproductivo. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
