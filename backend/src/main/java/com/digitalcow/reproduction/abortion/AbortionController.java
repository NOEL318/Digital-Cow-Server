package com.digitalcow.reproduction.abortion;

import com.digitalcow.reproduction.abortion.dto.AbortionCreateDto;
import com.digitalcow.reproduction.abortion.dto.AbortionResponseDto;
import com.digitalcow.reproduction.abortion.dto.AbortionUpdateDto;
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

/** Endpoints REST de Abortion. */
@RestController
@RequestMapping("/api/v1/reproduction/abortions")
public class AbortionController {

    private final AbortionService service;

    public AbortionController(AbortionService service) {
        this.service = service;
    }

    /** Este metodo lista los abortos. */
    @GetMapping
    public List<AbortionResponseDto> list() {
        return service.list();
    }

    /** Este metodo crea el aborto. */
    @PostMapping
    public ResponseEntity<AbortionResponseDto> create(@Valid @RequestBody AbortionCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza el aborto. */
    @PatchMapping("/{id}")
    public AbortionResponseDto update(@PathVariable Long id, @Valid @RequestBody AbortionUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina el aborto. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
