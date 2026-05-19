package com.digitalcow.ranch.condition;

import com.digitalcow.ranch.condition.dto.LotConditionCreateRequest;
import com.digitalcow.ranch.condition.dto.LotConditionDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints para registrar condiciones del corral (lodo, lluvia,
 * plagas, agua, etc.). El listado es por lote.
 */
@RestController
public class LotConditionController {

    private final LotConditionService service;

    public LotConditionController(LotConditionService service) {
        this.service = service;
    }

    /** Este metodo lista las condiciones registradas para un lote. */
    @GetMapping("/api/v1/lot-conditions")
    public List<LotConditionDto> listByLot(@RequestParam("lotId") Long lotId) {
        return service.listByLot(lotId);
    }

    /** Este metodo crea la condicion del lote. */
    @PostMapping("/api/v1/lot-conditions")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    @ResponseStatus(HttpStatus.CREATED)
    public LotConditionDto create(@Valid @RequestBody LotConditionCreateRequest req) {
        return service.create(req);
    }

    /** Este metodo elimina la condicion del lote. */
    @DeleteMapping("/api/v1/lot-conditions/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
