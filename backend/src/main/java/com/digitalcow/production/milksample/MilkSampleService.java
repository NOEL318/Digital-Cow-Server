package com.digitalcow.production.milksample;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.production.milksample.dto.MilkSampleCreateDto;
import com.digitalcow.production.milksample.dto.MilkSampleResponseDto;
import com.digitalcow.production.milksample.dto.MilkSampleUpdateDto;
import com.digitalcow.production.milksample.event.MilkSampleChangedEvent;
import com.digitalcow.production.milksample.mapper.MilkSampleMapper;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Servicio CRUD de MilkSample. */
@Service
@Transactional
public class MilkSampleService {

    private final MilkSampleRepository repository;
    private final MilkSampleMapper mapper;
    private final ApplicationEventPublisher events;

    public MilkSampleService(MilkSampleRepository repository,
                             MilkSampleMapper mapper,
                             ApplicationEventPublisher events) {
        this.repository = repository;
        this.mapper = mapper;
        this.events = events;
    }

    /** Este metodo lista las muestras de leche. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<MilkSampleResponseDto> list() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    /** Este metodo lista el animal. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<MilkSampleResponseDto> listByAnimal(Long animalId) {
        return repository.findByAnimalIdOrderBySampledAtDesc(animalId).stream()
            .map(mapper::toDto).toList();
    }

    /** Este metodo crea la muestra de leche. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public MilkSampleResponseDto create(MilkSampleCreateDto dto) {
        MilkSample entity = mapper.fromCreate(dto);
        MilkSample saved = repository.save(entity);
        events.publishEvent(new MilkSampleChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(saved);
    }

    /** Este metodo actualiza la muestra de leche. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public MilkSampleResponseDto update(Long id, MilkSampleUpdateDto dto) {
        MilkSample entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Milk sample not found"));
        mapper.applyUpdate(dto, entity);
        events.publishEvent(new MilkSampleChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(entity);
    }

    /** Este metodo elimina la muestra de leche. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        MilkSample entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Milk sample not found"));
        repository.delete(entity);
        events.publishEvent(new MilkSampleChangedEvent(TenantContext.requireAccountId()));
    }
}
