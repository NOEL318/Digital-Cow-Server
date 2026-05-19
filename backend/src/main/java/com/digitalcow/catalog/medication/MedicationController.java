package com.digitalcow.catalog.medication;

import com.digitalcow.catalog.medication.dto.MedicationDto;
import com.digitalcow.catalog.medication.dto.MedicationUpsertRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints del catalogo de medicamentos. Listado y lookup abiertos a
 * cualquier usuario autenticado del tenant; CRUD restringido a roles
 * OWNER, ADMIN y MANAGER.
 */
@RestController
@RequestMapping("/api/v1/catalog/medications")
public class MedicationController {

    private final MedicationService service;

    public MedicationController(MedicationService service) {
        this.service = service;
    }

    /** Lista visible para el tenant actual: seeds globales mas propias. */
    @GetMapping
    public List<MedicationDto> list() {
        return service.list();
    }

    /** Detalle por id. */
    @GetMapping("/{id}")
    public MedicationDto get(@PathVariable Long id) {
        return service.get(id);
    }

    /**
     * Lookup por codigo de barras. Devuelve 200 con el DTO si existe
     * o 404 vacio. La UI usa el 404 para sugerir crear el medicamento.
     */
    @GetMapping("/by-barcode/{barcode}")
    public ResponseEntity<MedicationDto> byBarcode(@PathVariable String barcode) {
        return service.findByBarcode(barcode)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Este metodo crea el medicamento. */
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public MedicationDto create(@Valid @RequestBody MedicationUpsertRequest req) {
        return service.create(req);
    }

    /** Este metodo actualiza el medicamento. */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public MedicationDto update(@PathVariable Long id, @Valid @RequestBody MedicationUpsertRequest req) {
        return service.update(id, req);
    }

    /** Este metodo elimina el medicamento. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
