package com.digitalcow.health.vetvisit;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.health.vetvisit.dto.VetVisitCreateDto;
import com.digitalcow.health.vetvisit.dto.VetVisitResponseDto;
import com.digitalcow.health.vetvisit.dto.VetVisitUpdateDto;
import com.digitalcow.health.vetvisit.mapper.VetVisitMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Servicio CRUD de VetVisit con autorizacion por rol. */
@Service
@Transactional
public class VetVisitService {

    private final VetVisitRepository repository;
    private final VetVisitMapper mapper;

    public VetVisitService(VetVisitRepository repository, VetVisitMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /** Lista todas las visitas de la cuenta del usuario actual. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<VetVisitResponseDto> list() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    /** Crea una visita. Worker o superior. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public VetVisitResponseDto create(VetVisitCreateDto dto) {
        VetVisit entity = mapper.fromCreate(dto);
        VetVisit saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    /** Actualiza una visita por id. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public VetVisitResponseDto update(Long id, VetVisitUpdateDto dto) {
        VetVisit entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Vet visit not found"));
        mapper.applyUpdate(dto, entity);
        return mapper.toDto(entity);
    }

    /** Borra una visita. Falla si tiene eventos asociados. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public void delete(Long id) {
        VetVisit entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Vet visit not found"));
        try {
            repository.delete(entity);
        } catch (DataIntegrityViolationException ex) {
            throw BusinessException.conflict(ErrorCode.CONFLICT, "Vet visit has associated events");
        }
    }
}
