package com.digitalcow.feeding.feeditem;

import com.digitalcow.feeding.feeditem.dto.FeedItemCreateDto;
import com.digitalcow.feeding.feeditem.dto.FeedItemResponseDto;
import com.digitalcow.feeding.feeditem.dto.FeedItemUpdateDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Endpoints REST de FeedItem. */
@RestController
@RequestMapping("/api/v1/feeding/items")
public class FeedItemController {

    private final FeedItemService service;

    public FeedItemController(FeedItemService service) {
        this.service = service;
    }

    /** Este metodo lista los insumos de alimentacion. */
    @GetMapping
    public List<FeedItemResponseDto> list() {
        return service.list();
    }

    /** Este metodo devuelve el insumo de alimentacion. */
    @GetMapping("/{id}")
    public FeedItemResponseDto get(@PathVariable Long id) {
        return service.get(id);
    }

    /** Este metodo crea el insumo de alimentacion. */
    @PostMapping
    public ResponseEntity<FeedItemResponseDto> create(@Valid @RequestBody FeedItemCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza el insumo de alimentacion. */
    @PatchMapping("/{id}")
    public FeedItemResponseDto update(@PathVariable Long id, @Valid @RequestBody FeedItemUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina el insumo de alimentacion. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
