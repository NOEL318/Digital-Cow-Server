package com.digitalcow.reproduction.pregnancy;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.reproduction.pregnancy.dto.PregnancyCheckCreateDto;
import com.digitalcow.reproduction.pregnancy.dto.PregnancyCheckResponseDto;
import com.digitalcow.reproduction.pregnancy.dto.PregnancyCheckUpdateDto;
import com.digitalcow.reproduction.pregnancy.event.PregnancyCheckChangedEvent;
import com.digitalcow.reproduction.pregnancy.mapper.PregnancyCheckMapper;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio CRUD de PregnancyCheck con autorizacion por rol.
 * Calcula estimatedCalvingDate cuando el resultado es POSITIVE y hay dias de gestacion estimados.
 */
@Service
@Transactional
public class PregnancyCheckService {

    /** Duracion promedio de gestacion bovina en dias. */
    private static final int GESTATION_DAYS = 283;

    private final PregnancyCheckRepository repository;
    private final PregnancyCheckMapper mapper;
    private final ApplicationEventPublisher events;

    public PregnancyCheckService(PregnancyCheckRepository repository,
                                 PregnancyCheckMapper mapper,
                                 ApplicationEventPublisher events) {
        this.repository = repository;
        this.mapper = mapper;
        this.events = events;
    }

    /** Lista todos los diagnosticos de la cuenta. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<PregnancyCheckResponseDto> list() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    /** Lista los diagnosticos de un animal en orden cronologico inverso. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<PregnancyCheckResponseDto> listByAnimal(Long animalId) {
        return repository.findByAnimalIdOrderByCheckedAtDesc(animalId).stream()
            .map(mapper::toDto)
            .toList();
    }

    /**
     * Crea un diagnostico. Si result=POSITIVE y estimatedGestationDays != null,
     * calcula estimatedCalvingDate = checkedAt + (GESTATION_DAYS - estimatedGestationDays).
     */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public PregnancyCheckResponseDto create(PregnancyCheckCreateDto dto) {
        PregnancyCheck entity = mapper.fromCreate(dto);
        if (dto.result() == PregnancyResult.POSITIVE && dto.estimatedGestationDays() != null) {
            int daysToCalving = GESTATION_DAYS - dto.estimatedGestationDays();
            entity.setEstimatedCalvingDate(dto.checkedAt().plusDays(daysToCalving));
        }
        PregnancyCheck saved = repository.save(entity);
        events.publishEvent(new PregnancyCheckChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(saved);
    }

    /** Actualiza un diagnostico. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public PregnancyCheckResponseDto update(Long id, PregnancyCheckUpdateDto dto) {
        PregnancyCheck entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Pregnancy check not found"));
        mapper.applyUpdate(dto, entity);
        events.publishEvent(new PregnancyCheckChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(entity);
    }

    /** Borra un diagnostico. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        PregnancyCheck entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Pregnancy check not found"));
        repository.delete(entity);
        events.publishEvent(new PregnancyCheckChangedEvent(TenantContext.requireAccountId()));
    }
}
