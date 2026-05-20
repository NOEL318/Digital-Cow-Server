package com.digitalcow.animal.share;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint publico (sin autenticacion) que sirve la vista de solo
 * lectura de un animal a quien presente un share_token valido.
 * La ruta /api/v1/public/** debe estar permitida en SecurityConfig.
 */
@RestController
@RequestMapping("/api/v1/public/animal-share")
public class PublicAnimalShareController {

    private final PublicAnimalShareService service;

    public PublicAnimalShareController(PublicAnimalShareService service) {
        this.service = service;
    }

    /**
     * Devuelve la vista publica del animal asociada al token.
     *
     * @param token identificador opaco generado por el dueño
     * @return vista publica con datos de identificacion y estadisticas
     */
    @GetMapping("/{token}")
    public PublicAnimalShareResponse get(@PathVariable String token) {
        return service.resolve(token);
    }
}
