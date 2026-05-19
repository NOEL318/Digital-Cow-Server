package com.digitalcow.feeding.record;

import com.digitalcow.feeding.record.dto.FeedingRecordCreateDto;
import com.digitalcow.feeding.record.dto.FeedingRecordResponseDto;
import com.digitalcow.feeding.record.dto.FeedingRecordUpdateDto;
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

/** Endpoints REST de FeedingRecord. */
@RestController
@RequestMapping("/api/v1/feeding/records")
public class FeedingRecordController {

    private final FeedingRecordService service;

    public FeedingRecordController(FeedingRecordService service) {
        this.service = service;
    }

    /** Este metodo lista los registros de alimentacion. */
    @GetMapping
    public List<FeedingRecordResponseDto> list() {
        return service.list();
    }

    /** Este metodo crea el registro de alimentacion. */
    @PostMapping
    public ResponseEntity<FeedingRecordResponseDto> create(@Valid @RequestBody FeedingRecordCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    /** Este metodo actualiza el registro de alimentacion. */
    @PatchMapping("/{id}")
    public FeedingRecordResponseDto update(@PathVariable Long id, @Valid @RequestBody FeedingRecordUpdateDto dto) {
        return service.update(id, dto);
    }

    /** Este metodo elimina el registro de alimentacion. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
