package com.digitalcow.reproduction.bull;

import com.digitalcow.reproduction.bull.dto.BullCreateDto;
import com.digitalcow.reproduction.bull.dto.BullResponseDto;
import com.digitalcow.reproduction.bull.dto.BullUpdateDto;
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

/** Endpoints REST de Bull. */
@RestController
@RequestMapping("/api/v1/reproduction/bulls")
public class BullController {

    private final BullService service;

    public BullController(BullService service) {
        this.service = service;
    }

    /** Este metodo lista los toros. */
    @GetMapping
    public List<BullResponseDto> list() {
        return service.list();
    }

    /** Este metodo devuelve el toro. */
    @GetMapping("/{id}")
    public BullResponseDto get(@PathVariable Long id) {
        return service.get(id);
    }

    /** Este metodo crea el toro. */
    @PostMapping
    public ResponseEntity<BullResponseDto> create(@Valid @RequestBody BullCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza el toro. */
    @PatchMapping("/{id}")
    public BullResponseDto update(@PathVariable Long id, @Valid @RequestBody BullUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina el toro. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
