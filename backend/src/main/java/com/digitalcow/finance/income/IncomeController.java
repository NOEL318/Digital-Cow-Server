package com.digitalcow.finance.income;

import com.digitalcow.finance.income.dto.IncomeCreateDto;
import com.digitalcow.finance.income.dto.IncomeResponseDto;
import com.digitalcow.finance.income.dto.IncomeUpdateDto;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Endpoints REST de Income. */
@RestController
@RequestMapping("/api/v1/finance/incomes")
public class IncomeController {

    private final IncomeService service;

    public IncomeController(IncomeService service) {
        this.service = service;
    }

    /** Este metodo lista los ingresos. */
    @GetMapping
    public Page<IncomeResponseDto> list(
        @RequestParam(required = false) LocalDate from,
        @RequestParam(required = false) LocalDate to,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Long ranchId,
        @RequestParam(required = false) Long lotId,
        @RequestParam(required = false) Long animalId,
        @RequestParam(required = false) IncomeSourceType sourceType,
        Pageable pageable
    ) {
        return service.list(from, to, categoryId, ranchId, lotId, animalId, sourceType, pageable);
    }

    /** Este metodo devuelve el ingreso. */
    @GetMapping("/{id}")
    public IncomeResponseDto get(@PathVariable Long id) {
        return service.get(id);
    }

    /** Este metodo crea el ingreso. */
    @PostMapping
    public ResponseEntity<IncomeResponseDto> create(@Valid @RequestBody IncomeCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza el ingreso. */
    @PatchMapping("/{id}")
    public IncomeResponseDto update(@PathVariable Long id, @Valid @RequestBody IncomeUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina el ingreso. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
