package com.digitalcow.animal;

import com.digitalcow.animal.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Endpoints CRUD y listado paginado de animales. */
@RestController
@RequestMapping("/api/v1/animals")
public class AnimalController {

    private final AnimalService svc;

    public AnimalController(AnimalService svc) { this.svc = svc; }

    /** Este metodo lista los animales. */
    @GetMapping
    public Page<AnimalListItem> list(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Long ranchId,
        @RequestParam(required = false) Long lotId,
        @RequestParam(required = false) Long breedId,
        @RequestParam(required = false) Sex sex,
        @RequestParam(required = false) Purpose purpose,
        @RequestParam(required = false) AnimalStatus status,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return svc.list(search, ranchId, lotId, breedId, sex, purpose, status, pageable);
    }

    /** Este metodo devuelve el animal. */
    @GetMapping("/{id}")
    public AnimalResponse get(@PathVariable Long id) { return svc.get(id); }

    /** Este metodo crea el animal. */
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public AnimalResponse create(@Valid @RequestBody AnimalCreateRequest req) { return svc.create(req); }

    /** Este metodo actualiza el animal. */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public AnimalResponse update(@PathVariable Long id, @Valid @RequestBody AnimalUpdateRequest req) {
        return svc.update(id, req);
    }

    /** Este metodo elimina el animal. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }
}
