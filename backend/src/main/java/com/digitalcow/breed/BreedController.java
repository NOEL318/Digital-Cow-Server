package com.digitalcow.breed;

import com.digitalcow.breed.dto.BreedDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Lectura publica del catalogo de razas. Cacheable por el cliente. */
@RestController
@RequestMapping("/api/v1/breeds")
public class BreedController {

    private final BreedRepository repo;

    public BreedController(BreedRepository repo) { this.repo = repo; }

    /** Este metodo lista las razas. */
    @GetMapping
    public List<BreedDto> list() {
        return repo.findAll().stream()
            .map(b -> new BreedDto(b.getId(), b.getCode(), b.getNameEs(), b.getNameEn(),
                b.getSpecies().name(), b.getCategory().name(), b.getBos().name()))
            .toList();
    }
}
