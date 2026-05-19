package com.digitalcow.reproduction.semen;

import com.digitalcow.reproduction.semen.dto.SemenStrawCreateDto;
import com.digitalcow.reproduction.semen.dto.SemenStrawResponseDto;
import com.digitalcow.reproduction.semen.dto.SemenStrawUpdateDto;
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

/** Endpoints REST de SemenStraw. */
@RestController
@RequestMapping("/api/v1/reproduction/semen-straws")
public class SemenStrawController {

    private final SemenStrawService service;

    public SemenStrawController(SemenStrawService service) {
        this.service = service;
    }

    /** Este metodo lista las pajillas de semen. */
    @GetMapping
    public List<SemenStrawResponseDto> list() {
        return service.list();
    }

    /** Este metodo devuelve la pajilla de semen. */
    @GetMapping("/{id}")
    public SemenStrawResponseDto get(@PathVariable Long id) {
        return service.get(id);
    }

    /** Este metodo crea la pajilla de semen. */
    @PostMapping
    public ResponseEntity<SemenStrawResponseDto> create(@Valid @RequestBody SemenStrawCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza la pajilla de semen. */
    @PatchMapping("/{id}")
    public SemenStrawResponseDto update(@PathVariable Long id, @Valid @RequestBody SemenStrawUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina la pajilla de semen. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
