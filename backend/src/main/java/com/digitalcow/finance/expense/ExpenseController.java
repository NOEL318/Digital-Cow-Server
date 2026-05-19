package com.digitalcow.finance.expense;

import com.digitalcow.finance.expense.dto.ExpenseCreateDto;
import com.digitalcow.finance.expense.dto.ExpenseResponseDto;
import com.digitalcow.finance.expense.dto.ExpenseUpdateDto;
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

/** Endpoints REST de Expense. */
@RestController
@RequestMapping("/api/v1/finance/expenses")
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    /** Este metodo lista los gastos. */
    @GetMapping
    public Page<ExpenseResponseDto> list(
        @RequestParam(required = false) LocalDate from,
        @RequestParam(required = false) LocalDate to,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Long ranchId,
        @RequestParam(required = false) Long lotId,
        @RequestParam(required = false) Long animalId,
        Pageable pageable
    ) {
        return service.list(from, to, categoryId, ranchId, lotId, animalId, pageable);
    }

    /** Este metodo devuelve el gasto. */
    @GetMapping("/{id}")
    public ExpenseResponseDto get(@PathVariable Long id) {
        return service.get(id);
    }

    /** Este metodo crea el gasto. */
    @PostMapping
    public ResponseEntity<ExpenseResponseDto> create(@Valid @RequestBody ExpenseCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza el gasto. */
    @PatchMapping("/{id}")
    public ExpenseResponseDto update(@PathVariable Long id, @Valid @RequestBody ExpenseUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina el gasto. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
