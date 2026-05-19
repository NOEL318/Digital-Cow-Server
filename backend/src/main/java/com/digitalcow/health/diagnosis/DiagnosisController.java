package com.digitalcow.health.diagnosis;

import com.digitalcow.health.diagnosis.dto.DiagnosisCreateDto;
import com.digitalcow.health.diagnosis.dto.DiagnosisResponseDto;
import com.digitalcow.health.diagnosis.dto.DiagnosisUpdateDto;
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

/** Endpoints REST de Diagnosis. */
@RestController
@RequestMapping("/api/v1/health/diagnoses")
public class DiagnosisController {

    private final DiagnosisService service;

    public DiagnosisController(DiagnosisService service) {
        this.service = service;
    }

    /** Lista todos los diagnosticos de la cuenta. */
    @GetMapping
    public List<DiagnosisResponseDto> list() {
        return service.list();
    }

    /** Este metodo crea el diagnostico. */
    @PostMapping
    public ResponseEntity<DiagnosisResponseDto> create(@Valid @RequestBody DiagnosisCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza el diagnostico. */
    @PatchMapping("/{id}")
    public DiagnosisResponseDto update(@PathVariable Long id, @Valid @RequestBody DiagnosisUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina el diagnostico. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
