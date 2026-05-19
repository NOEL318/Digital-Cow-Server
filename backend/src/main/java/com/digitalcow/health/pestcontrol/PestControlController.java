package com.digitalcow.health.pestcontrol;

import com.digitalcow.health.pestcontrol.dto.PestControlCreateDto;
import com.digitalcow.health.pestcontrol.dto.PestControlResponseDto;
import com.digitalcow.health.pestcontrol.dto.PestControlUpdateDto;
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

/** Endpoints REST de PestControl. */
@RestController
@RequestMapping("/api/v1/health/pest-controls")
public class PestControlController {

    private final PestControlService service;

    public PestControlController(PestControlService service) {
        this.service = service;
    }

    /** Este metodo lista los controles de plagas. */
    @GetMapping
    public List<PestControlResponseDto> list() {
        return service.list();
    }

    /** Este metodo crea el control de plagas. */
    @PostMapping
    public ResponseEntity<PestControlResponseDto> create(@Valid @RequestBody PestControlCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza el control de plagas. */
    @PatchMapping("/{id}")
    public PestControlResponseDto update(@PathVariable Long id, @Valid @RequestBody PestControlUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina el control de plagas. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
