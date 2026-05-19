package com.digitalcow.reproduction.weaning;

import com.digitalcow.reproduction.weaning.dto.WeaningCreateDto;
import com.digitalcow.reproduction.weaning.dto.WeaningResponseDto;
import com.digitalcow.reproduction.weaning.dto.WeaningUpdateDto;
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

/** Endpoints REST de Weaning. */
@RestController
@RequestMapping("/api/v1/reproduction/weanings")
public class WeaningController {

    private final WeaningService service;

    public WeaningController(WeaningService service) {
        this.service = service;
    }

    /** Este metodo lista los destetes. */
    @GetMapping
    public List<WeaningResponseDto> list() {
        return service.list();
    }

    /** Este metodo crea el destete. */
    @PostMapping
    public ResponseEntity<WeaningResponseDto> create(@Valid @RequestBody WeaningCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza el destete. */
    @PatchMapping("/{id}")
    public WeaningResponseDto update(@PathVariable Long id, @Valid @RequestBody WeaningUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina el destete. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
