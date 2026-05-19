package com.digitalcow.animal.purchase;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de compra atomica de animal. Crea el animal y el gasto
 * en una sola transaccion del backend; si la creacion del gasto
 * falla, el animal tambien hace rollback.
 */
@RestController
public class AnimalPurchaseController {

    private final AnimalPurchaseService service;

    public AnimalPurchaseController(AnimalPurchaseService service) {
        this.service = service;
    }

    /** Este metodo crea la compra del animal. */
    @PostMapping("/api/v1/animals/with-purchase")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    public AnimalPurchaseResponse create(@Valid @RequestBody AnimalPurchaseRequest req) {
        return service.purchase(req);
    }
}
