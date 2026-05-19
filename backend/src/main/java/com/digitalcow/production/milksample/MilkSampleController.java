package com.digitalcow.production.milksample;

import com.digitalcow.production.milksample.dto.MilkSampleCreateDto;
import com.digitalcow.production.milksample.dto.MilkSampleResponseDto;
import com.digitalcow.production.milksample.dto.MilkSampleUpdateDto;
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

/** Endpoints REST de MilkSample. */
@RestController
@RequestMapping("/api/v1/production/milk-samples")
public class MilkSampleController {

    private final MilkSampleService service;

    public MilkSampleController(MilkSampleService service) {
        this.service = service;
    }

    /** Este metodo lista las muestras de leche. */
    @GetMapping
    public List<MilkSampleResponseDto> list() {
        return service.list();
    }

    /** Este metodo crea la muestra de leche. */
    @PostMapping
    public ResponseEntity<MilkSampleResponseDto> create(@Valid @RequestBody MilkSampleCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza la muestra de leche. */
    @PatchMapping("/{id}")
    public MilkSampleResponseDto update(@PathVariable Long id, @Valid @RequestBody MilkSampleUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina la muestra de leche. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
