package com.digitalcow.reproduction.pregnancy;

import com.digitalcow.reproduction.pregnancy.dto.PregnancyCheckCreateDto;
import com.digitalcow.reproduction.pregnancy.dto.PregnancyCheckResponseDto;
import com.digitalcow.reproduction.pregnancy.dto.PregnancyCheckUpdateDto;
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

/** Endpoints REST de PregnancyCheck. */
@RestController
@RequestMapping("/api/v1/reproduction/pregnancy-checks")
public class PregnancyCheckController {

    private final PregnancyCheckService service;

    public PregnancyCheckController(PregnancyCheckService service) {
        this.service = service;
    }

    /** Este metodo lista los chequeos de prenez. */
    @GetMapping
    public List<PregnancyCheckResponseDto> list() {
        return service.list();
    }

    /** Este metodo crea el chequeo de prenez. */
    @PostMapping
    public ResponseEntity<PregnancyCheckResponseDto> create(@Valid @RequestBody PregnancyCheckCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza el chequeo de prenez. */
    @PatchMapping("/{id}")
    public PregnancyCheckResponseDto update(@PathVariable Long id, @Valid @RequestBody PregnancyCheckUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina el chequeo de prenez. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
