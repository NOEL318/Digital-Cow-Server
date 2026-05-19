package com.digitalcow.finance.category;

import com.digitalcow.finance.category.dto.ExpenseCategoryCreateDto;
import com.digitalcow.finance.category.dto.ExpenseCategoryResponseDto;
import com.digitalcow.finance.category.dto.ExpenseCategoryUpdateDto;
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

/** Endpoints REST de ExpenseCategory. */
@RestController
@RequestMapping("/api/v1/finance/expense-categories")
public class ExpenseCategoryController {

    private final ExpenseCategoryService service;

    public ExpenseCategoryController(ExpenseCategoryService service) {
        this.service = service;
    }

    /** Este metodo lista las categorias de gasto. */
    @GetMapping
    public List<ExpenseCategoryResponseDto> list() {
        return service.list();
    }

    /** Este metodo devuelve la categoria de gasto. */
    @GetMapping("/{id}")
    public ExpenseCategoryResponseDto get(@PathVariable Long id) {
        return service.get(id);
    }

    /** Este metodo crea la categoria de gasto. */
    @PostMapping
    public ResponseEntity<ExpenseCategoryResponseDto> create(@Valid @RequestBody ExpenseCategoryCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza la categoria de gasto. */
    @PatchMapping("/{id}")
    public ExpenseCategoryResponseDto update(@PathVariable Long id, @Valid @RequestBody ExpenseCategoryUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina la categoria de gasto. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
