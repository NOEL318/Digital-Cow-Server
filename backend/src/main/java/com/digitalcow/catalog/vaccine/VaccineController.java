package com.digitalcow.catalog.vaccine;

import com.digitalcow.catalog.vaccine.dto.VaccineDto;
import com.digitalcow.catalog.vaccine.mapper.VaccineMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Endpoint de listado de vacunas globales. Cacheable. */
@RestController
@RequestMapping("/api/v1/catalog/vaccines")
public class VaccineController {

    private final VaccineRepository repository;
    private final VaccineMapper mapper;

    public VaccineController(VaccineRepository repository, VaccineMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /** Devuelve todas las vacunas ordenadas por code. */
    @GetMapping
    @Cacheable("catalog-vaccines")
    public List<VaccineDto> list() {
        return repository.findAll(Sort.by("code"))
            .stream().map(mapper::toDto).toList();
    }
}
