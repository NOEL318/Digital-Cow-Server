package com.digitalcow.ranch;

import com.digitalcow.ranch.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Endpoints CRUD de ranchos del tenant. */
@RestController
@RequestMapping("/api/v1/ranches")
public class RanchController {

    private final RanchService svc;

    public RanchController(RanchService svc) { this.svc = svc; }

    /** Este metodo lista los ranchos. */
    @GetMapping
    public List<RanchDto> list() { return svc.list(); }

    /** Este metodo devuelve el rancho. */
    @GetMapping("/{id}")
    public RanchDto get(@PathVariable Long id) { return svc.get(id); }

    /** Este metodo crea el rancho. */
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public RanchDto create(@Valid @RequestBody RanchUpsertRequest req) { return svc.create(req); }

    /** Este metodo actualiza el rancho. */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public RanchDto update(@PathVariable Long id, @Valid @RequestBody RanchUpsertRequest req) {
        return svc.update(id, req);
    }

    /** Este metodo elimina el rancho. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }
}
