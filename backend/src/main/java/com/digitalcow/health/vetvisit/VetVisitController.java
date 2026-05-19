package com.digitalcow.health.vetvisit;

import com.digitalcow.health.vetvisit.dto.VetVisitCreateDto;
import com.digitalcow.health.vetvisit.dto.VetVisitResponseDto;
import com.digitalcow.health.vetvisit.dto.VetVisitUpdateDto;
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

/** Endpoints REST de VetVisit. */
@RestController
@RequestMapping("/api/v1/health/vet-visits")
public class VetVisitController {

    private final VetVisitService service;

    public VetVisitController(VetVisitService service) {
        this.service = service;
    }

    /** Este metodo lista las visitas veterinarias. */
    @GetMapping
    public List<VetVisitResponseDto> list() {
        return service.list();
    }

    /** Este metodo crea la visita veterinaria. */
    @PostMapping
    public ResponseEntity<VetVisitResponseDto> create(@Valid @RequestBody VetVisitCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza la visita veterinaria. */
    @PatchMapping("/{id}")
    public VetVisitResponseDto update(@PathVariable Long id, @Valid @RequestBody VetVisitUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina la visita veterinaria. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
