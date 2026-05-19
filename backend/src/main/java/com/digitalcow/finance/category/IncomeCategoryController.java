package com.digitalcow.finance.category;

import com.digitalcow.finance.category.dto.IncomeCategoryCreateDto;
import com.digitalcow.finance.category.dto.IncomeCategoryResponseDto;
import com.digitalcow.finance.category.dto.IncomeCategoryUpdateDto;
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

/** Endpoints REST de IncomeCategory. */
@RestController
@RequestMapping("/api/v1/finance/income-categories")
public class IncomeCategoryController {

    private final IncomeCategoryService service;

    public IncomeCategoryController(IncomeCategoryService service) {
        this.service = service;
    }

    /** Este metodo lista las categorias de ingreso. */
    @GetMapping
    public List<IncomeCategoryResponseDto> list() {
        return service.list();
    }

    /** Este metodo devuelve la categoria de ingreso. */
    @GetMapping("/{id}")
    public IncomeCategoryResponseDto get(@PathVariable Long id) {
        return service.get(id);
    }

    /** Este metodo crea la categoria de ingreso. */
    @PostMapping
    public ResponseEntity<IncomeCategoryResponseDto> create(@Valid @RequestBody IncomeCategoryCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza la categoria de ingreso. */
    @PatchMapping("/{id}")
    public IncomeCategoryResponseDto update(@PathVariable Long id, @Valid @RequestBody IncomeCategoryUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina la categoria de ingreso. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
