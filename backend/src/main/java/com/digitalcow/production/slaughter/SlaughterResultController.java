package com.digitalcow.production.slaughter;

import com.digitalcow.production.slaughter.dto.SlaughterResultCreateDto;
import com.digitalcow.production.slaughter.dto.SlaughterResultResponseDto;
import com.digitalcow.production.slaughter.dto.SlaughterResultUpdateDto;
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

/** Endpoints REST de SlaughterResult. */
@RestController
@RequestMapping("/api/v1/production/slaughter-results")
public class SlaughterResultController {

    private final SlaughterResultService service;

    public SlaughterResultController(SlaughterResultService service) {
        this.service = service;
    }

    /** Este metodo lista los resultados de faena. */
    @GetMapping
    public List<SlaughterResultResponseDto> list() {
        return service.list();
    }

    /** Este metodo crea el resultado de faena. */
    @PostMapping
    public ResponseEntity<SlaughterResultResponseDto> create(@Valid @RequestBody SlaughterResultCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza el resultado de faena. */
    @PatchMapping("/{id}")
    public SlaughterResultResponseDto update(@PathVariable Long id, @Valid @RequestBody SlaughterResultUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina el resultado de faena. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
