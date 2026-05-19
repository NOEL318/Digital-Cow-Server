package com.digitalcow.ranch;

import com.digitalcow.ranch.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Endpoints de lotes (anidados bajo /ranches/{ranchId}). */
@RestController
public class LotController {

    private final LotService svc;

    public LotController(LotService svc) { this.svc = svc; }

    /** Este metodo lista los lotes. */
    @GetMapping("/api/v1/ranches/{ranchId}/lots")
    public List<LotDto> list(@PathVariable Long ranchId) { return svc.listByRanch(ranchId); }

    /** Este metodo crea el lote. */
    @PostMapping("/api/v1/ranches/{ranchId}/lots")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public LotDto create(@PathVariable Long ranchId, @Valid @RequestBody LotUpsertRequest req) {
        return svc.create(ranchId, req);
    }

    /** Este metodo actualiza el lote. */
    @PatchMapping("/api/v1/lots/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public LotDto update(@PathVariable Long id, @Valid @RequestBody LotUpsertRequest req) {
        return svc.update(id, req);
    }

    /** Este metodo elimina el lote. */
    @DeleteMapping("/api/v1/lots/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }
}
