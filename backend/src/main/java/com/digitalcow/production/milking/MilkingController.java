package com.digitalcow.production.milking;

import com.digitalcow.production.milking.dto.MilkingBulkDto;
import com.digitalcow.production.milking.dto.MilkingCreateDto;
import com.digitalcow.production.milking.dto.MilkingResponseDto;
import com.digitalcow.production.milking.dto.MilkingUpdateDto;
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

/** Endpoints REST de Milking. */
@RestController
@RequestMapping("/api/v1/production/milkings")
public class MilkingController {

    private final MilkingService service;

    public MilkingController(MilkingService service) {
        this.service = service;
    }

    /** Este metodo lista los ordenos. */
    @GetMapping
    public List<MilkingResponseDto> list() {
        return service.list();
    }

    /** Este metodo crea el ordeno. */
    @PostMapping
    public ResponseEntity<MilkingResponseDto> create(@Valid @RequestBody MilkingCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo registra varios ordenos en lote para un grupo de animales. */
    @PostMapping("/bulk")
    public ResponseEntity<List<MilkingResponseDto>> createBulk(@Valid @RequestBody MilkingBulkDto dto) {
        return ResponseEntity.status(201).body(service.createBulk(dto));
    }

    /** Este metodo actualiza el ordeno. */
    @PatchMapping("/{id}")
    public MilkingResponseDto update(@PathVariable Long id, @Valid @RequestBody MilkingUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina el ordeno. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
