package com.digitalcow.production.bulktank;

import com.digitalcow.production.bulktank.dto.BulkTankDeliveryCreateDto;
import com.digitalcow.production.bulktank.dto.BulkTankDeliveryResponseDto;
import com.digitalcow.production.bulktank.dto.BulkTankDeliveryUpdateDto;
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

/** Endpoints REST de BulkTankDelivery. */
@RestController
@RequestMapping("/api/v1/production/bulk-tank-deliveries")
public class BulkTankDeliveryController {

    private final BulkTankDeliveryService service;

    public BulkTankDeliveryController(BulkTankDeliveryService service) {
        this.service = service;
    }

    /** Este metodo lista las entregas al tanque. */
    @GetMapping
    public List<BulkTankDeliveryResponseDto> list() {
        return service.list();
    }

    /** Este metodo crea la entrega al tanque. */
    @PostMapping
    public ResponseEntity<BulkTankDeliveryResponseDto> create(@Valid @RequestBody BulkTankDeliveryCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza la entrega al tanque. */
    @PatchMapping("/{id}")
    public BulkTankDeliveryResponseDto update(@PathVariable Long id, @Valid @RequestBody BulkTankDeliveryUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina la entrega al tanque. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
