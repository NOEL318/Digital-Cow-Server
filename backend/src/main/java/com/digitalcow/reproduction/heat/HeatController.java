package com.digitalcow.reproduction.heat;

import com.digitalcow.reproduction.heat.dto.HeatCreateDto;
import com.digitalcow.reproduction.heat.dto.HeatResponseDto;
import com.digitalcow.reproduction.heat.dto.HeatUpdateDto;
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

/** Endpoints REST de Heat. */
@RestController
@RequestMapping("/api/v1/reproduction/heats")
public class HeatController {

    private final HeatService service;

    public HeatController(HeatService service) {
        this.service = service;
    }

    /** Este metodo lista los celos. */
    @GetMapping
    public List<HeatResponseDto> list() {
        return service.list();
    }

    /** Este metodo crea el celo. */
    @PostMapping
    public ResponseEntity<HeatResponseDto> create(@Valid @RequestBody HeatCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza el celo. */
    @PatchMapping("/{id}")
    public HeatResponseDto update(@PathVariable Long id, @Valid @RequestBody HeatUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina el celo. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
