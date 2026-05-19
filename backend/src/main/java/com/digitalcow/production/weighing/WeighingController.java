package com.digitalcow.production.weighing;

import com.digitalcow.production.weighing.dto.WeighingCreateDto;
import com.digitalcow.production.weighing.dto.WeighingResponseDto;
import com.digitalcow.production.weighing.dto.WeighingUpdateDto;
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

/** Endpoints REST de Weighing. */
@RestController
@RequestMapping("/api/v1/production/weighings")
public class WeighingController {

    private final WeighingService service;

    public WeighingController(WeighingService service) {
        this.service = service;
    }

    /** Este metodo lista las pesadas. */
    @GetMapping
    public List<WeighingResponseDto> list() {
        return service.list();
    }

    /** Este metodo crea la pesada. */
    @PostMapping
    public ResponseEntity<WeighingResponseDto> create(@Valid @RequestBody WeighingCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza la pesada. */
    @PatchMapping("/{id}")
    public WeighingResponseDto update(@PathVariable Long id, @Valid @RequestBody WeighingUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina la pesada. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
