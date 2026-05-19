package com.digitalcow.production.weighing;

import com.digitalcow.production.weighing.dto.WeighingCreateDto;
import com.digitalcow.production.weighing.dto.WeighingResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Endpoint de creacion masiva de pesajes (para importacion desde CSV).
 * Falla rapido en el primer error, pero el frontend valida antes para
 * que esto solo ocurra ante problemas de servidor.
 */
@RestController
public class WeighingBulkController {

    private final WeighingService service;

    public WeighingBulkController(WeighingService service) {
        this.service = service;
    }

    /** Este metodo crea varias pesadas en una sola llamada para importacion masiva. */
    @PostMapping("/api/v1/production/weighings/bulk")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    @ResponseStatus(HttpStatus.CREATED)
    public List<WeighingResponseDto> bulk(@Valid @RequestBody List<WeighingCreateDto> dtos) {
        List<WeighingResponseDto> out = new ArrayList<>(dtos.size());
        for (WeighingCreateDto dto : dtos) {
            out.add(service.create(dto));
        }
        return out;
    }
}
