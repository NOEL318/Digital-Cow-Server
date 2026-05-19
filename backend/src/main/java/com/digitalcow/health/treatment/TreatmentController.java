package com.digitalcow.health.treatment;

import com.digitalcow.health.treatment.dto.TreatmentCreateDto;
import com.digitalcow.health.treatment.dto.TreatmentResponseDto;
import com.digitalcow.health.treatment.dto.TreatmentUpdateDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Endpoints REST de Treatment. */
@RestController
@RequestMapping("/api/v1/health/treatments")
public class TreatmentController {

    private final TreatmentService service;

    public TreatmentController(TreatmentService service) {
        this.service = service;
    }

    /** Lista todos los tratamientos de la cuenta. */
    @GetMapping
    public List<TreatmentResponseDto> list() {
        return service.list();
    }

    /** Este metodo crea el tratamiento. */
    @PostMapping
    public ResponseEntity<TreatmentResponseDto> create(@Valid @RequestBody TreatmentCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza el tratamiento. */
    @PatchMapping("/{id}")
    public TreatmentResponseDto update(@PathVariable Long id, @Valid @RequestBody TreatmentUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina el tratamiento. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
