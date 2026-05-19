package com.digitalcow.finance.milksale;

import com.digitalcow.finance.milksale.dto.MilkSaleCreateDto;
import com.digitalcow.finance.milksale.dto.MilkSaleResponseDto;
import com.digitalcow.finance.milksale.dto.MilkSaleUpdateDto;
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

/** Endpoints REST de MilkSale. */
@RestController
@RequestMapping("/api/v1/finance/milk-sales")
public class MilkSaleController {

    private final MilkSaleService service;

    public MilkSaleController(MilkSaleService service) {
        this.service = service;
    }

    /** Este metodo lista las ventas de leche. */
    @GetMapping
    public Page<MilkSaleResponseDto> list(Pageable pageable) {
        return service.list(pageable);
    }

    /** Este metodo devuelve la venta de leche. */
    @GetMapping("/{id}")
    public MilkSaleResponseDto get(@PathVariable Long id) {
        return service.get(id);
    }

    /** Este metodo crea la venta de leche. */
    @PostMapping
    public ResponseEntity<MilkSaleResponseDto> create(@Valid @RequestBody MilkSaleCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza la venta de leche. */
    @PatchMapping("/{id}")
    public MilkSaleResponseDto update(@PathVariable Long id, @Valid @RequestBody MilkSaleUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina la venta de leche. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
