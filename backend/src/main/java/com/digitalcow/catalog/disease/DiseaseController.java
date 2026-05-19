package com.digitalcow.catalog.disease;

import com.digitalcow.catalog.disease.dto.DiseaseDto;
import com.digitalcow.catalog.disease.mapper.DiseaseMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Endpoint de listado de enfermedades globales. Cacheable. */
@RestController
@RequestMapping("/api/v1/catalog/diseases")
public class DiseaseController {

    private final DiseaseRepository repository;
    private final DiseaseMapper mapper;

    public DiseaseController(DiseaseRepository repository, DiseaseMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /** Devuelve todas las enfermedades ordenadas por code. */
    @GetMapping
    @Cacheable("catalog-diseases")
    public List<DiseaseDto> list() {
        return repository.findAll(Sort.by("code"))
            .stream().map(mapper::toDto).toList();
    }
}
