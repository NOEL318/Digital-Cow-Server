package com.digitalcow.health.vaccination;

import com.digitalcow.health.vaccination.dto.VaccinationBulkDto;
import com.digitalcow.health.vaccination.dto.VaccinationCreateDto;
import com.digitalcow.health.vaccination.dto.VaccinationResponseDto;
import com.digitalcow.health.vaccination.dto.VaccinationUpdateDto;
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

/** Endpoints REST de Vaccination (individual y bulk). */
@RestController
@RequestMapping("/api/v1/health/vaccinations")
public class VaccinationController {

    private final VaccinationService service;

    public VaccinationController(VaccinationService service) {
        this.service = service;
    }

    /** Lista todas las vacunaciones de la cuenta. */
    @GetMapping
    public List<VaccinationResponseDto> list() {
        return service.list();
    }

    /** Este metodo crea la vacunacion. */
    @PostMapping
    public ResponseEntity<VaccinationResponseDto> create(@Valid @RequestBody VaccinationCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo registra varias vacunaciones en lote para un grupo de animales. */
    @PostMapping("/bulk")
    public ResponseEntity<List<VaccinationResponseDto>> createBulk(@Valid @RequestBody VaccinationBulkDto dto) {
        return ResponseEntity.status(201).body(service.createBulk(dto));
    }

    /** Este metodo actualiza la vacunacion. */
    @PatchMapping("/{id}")
    public VaccinationResponseDto update(@PathVariable Long id, @Valid @RequestBody VaccinationUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina la vacunacion. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
