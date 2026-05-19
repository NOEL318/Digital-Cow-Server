package com.digitalcow.reproduction.dryoff;

import com.digitalcow.reproduction.dryoff.dto.DryOffCreateDto;
import com.digitalcow.reproduction.dryoff.dto.DryOffResponseDto;
import com.digitalcow.reproduction.dryoff.dto.DryOffUpdateDto;
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

/** Endpoints REST de DryOff. */
@RestController
@RequestMapping("/api/v1/reproduction/dry-offs")
public class DryOffController {

    private final DryOffService service;

    public DryOffController(DryOffService service) {
        this.service = service;
    }

    /** Este metodo lista los secados. */
    @GetMapping
    public List<DryOffResponseDto> list() {
        return service.list();
    }

    /** Este metodo crea el secado. */
    @PostMapping
    public ResponseEntity<DryOffResponseDto> create(@Valid @RequestBody DryOffCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza el secado. */
    @PatchMapping("/{id}")
    public DryOffResponseDto update(@PathVariable Long id, @Valid @RequestBody DryOffUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina el secado. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
