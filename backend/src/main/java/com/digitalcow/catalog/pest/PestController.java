package com.digitalcow.catalog.pest;

import com.digitalcow.catalog.pest.dto.PestDto;
import com.digitalcow.catalog.pest.mapper.PestMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Endpoint de listado de plagas globales. Cacheable. */
@RestController
@RequestMapping("/api/v1/catalog/pests")
public class PestController {

    private final PestRepository repository;
    private final PestMapper mapper;

    public PestController(PestRepository repository, PestMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /** Devuelve todas las plagas ordenadas por code. */
    @GetMapping
    @Cacheable("catalog-pests")
    public List<PestDto> list() {
        return repository.findAll(Sort.by("code"))
            .stream().map(mapper::toDto).toList();
    }
}
