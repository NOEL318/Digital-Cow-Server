package com.digitalcow.finance.animalsale;

import com.digitalcow.finance.animalsale.dto.AnimalSaleCreateDto;
import com.digitalcow.finance.animalsale.dto.AnimalSaleResponseDto;
import com.digitalcow.finance.animalsale.dto.AnimalSaleUpdateDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints REST de AnimalSale. */
@RestController
@RequestMapping("/api/v1/finance/animal-sales")
public class AnimalSaleController {

    private final AnimalSaleService service;

    public AnimalSaleController(AnimalSaleService service) {
        this.service = service;
    }

    /** Este metodo lista las ventas de animales. */
    @GetMapping
    public Page<AnimalSaleResponseDto> list(Pageable pageable) {
        return service.list(pageable);
    }

    /** Este metodo devuelve la venta de animal. */
    @GetMapping("/{id}")
    public AnimalSaleResponseDto get(@PathVariable Long id) {
        return service.get(id);
    }

    /** Este metodo crea la venta de animal. */
    @PostMapping
    public ResponseEntity<AnimalSaleResponseDto> create(@Valid @RequestBody AnimalSaleCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza la venta de animal. */
    @PatchMapping("/{id}")
    public AnimalSaleResponseDto update(@PathVariable Long id, @Valid @RequestBody AnimalSaleUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina la venta de animal. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
