package com.digitalcow.reproduction.calving;

import com.digitalcow.reproduction.calving.dto.CalvingCreateDto;
import com.digitalcow.reproduction.calving.dto.CalvingResponseDto;
import com.digitalcow.reproduction.calving.dto.CalvingUpdateDto;
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

/** Endpoints REST de Calving. */
@RestController
@RequestMapping("/api/v1/reproduction/calvings")
public class CalvingController {

    private final CalvingService service;

    public CalvingController(CalvingService service) {
        this.service = service;
    }

    /** Este metodo lista los partos. */
    @GetMapping
    public List<CalvingResponseDto> list() {
        return service.list();
    }

    /** Este metodo crea el parto. */
    @PostMapping
    public ResponseEntity<CalvingResponseDto> create(@Valid @RequestBody CalvingCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza el parto. */
    @PatchMapping("/{id}")
    public CalvingResponseDto update(@PathVariable Long id, @Valid @RequestBody CalvingUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina el parto. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
