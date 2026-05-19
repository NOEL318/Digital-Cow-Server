package com.digitalcow.production.milking;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.production.milking.dto.MilkingBulkDto;
import com.digitalcow.production.milking.dto.MilkingCreateDto;
import com.digitalcow.production.milking.dto.MilkingResponseDto;
import com.digitalcow.production.milking.dto.MilkingUpdateDto;
import com.digitalcow.production.milking.event.MilkingChangedEvent;
import com.digitalcow.production.milking.mapper.MilkingMapper;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio CRUD de Milking. La UQ (account_id, animal_id, milking_date, session)
 * se enforza en la BD; aqui capturamos DataIntegrityViolationException y traducimos
 * a un conflict tipado.
 */
@Service
@Transactional
public class MilkingService {

    private final MilkingRepository repository;
    private final MilkingMapper mapper;
    private final ApplicationEventPublisher events;

    public MilkingService(MilkingRepository repository,
                          MilkingMapper mapper,
                          ApplicationEventPublisher events) {
        this.repository = repository;
        this.mapper = mapper;
        this.events = events;
    }

    /** Lista todos los ordenos de la cuenta. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<MilkingResponseDto> list() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    /** Lista ordenos de un animal en orden cronologico inverso. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<MilkingResponseDto> listByAnimal(Long animalId) {
        return repository.findByAnimalIdOrderByMilkingDateDesc(animalId).stream()
            .map(mapper::toDto)
            .toList();
    }

    /** Crea un ordeno; conflicto si ya existe un registro para la misma sesion. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public MilkingResponseDto create(MilkingCreateDto dto) {
        Milking entity = mapper.fromCreate(dto);
        if (entity.getSession() == null) entity.setSession(MilkingSession.TOTAL);
        try {
            Milking saved = repository.saveAndFlush(entity);
            events.publishEvent(new MilkingChangedEvent(TenantContext.requireAccountId()));
            return mapper.toDto(saved);
        } catch (DataIntegrityViolationException ex) {
            throw BusinessException.conflict(ErrorCode.CONFLICT,
                "Milking already exists for animal " + dto.animalId()
                    + " on " + dto.milkingDate() + " session " + entity.getSession());
        }
    }

    /** Actualiza un ordeno. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public MilkingResponseDto update(Long id, MilkingUpdateDto dto) {
        Milking entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Milking not found"));
        mapper.applyUpdate(dto, entity);
        try {
            repository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw BusinessException.conflict(ErrorCode.CONFLICT,
                "Milking already exists for that animal/date/session");
        }
        events.publishEvent(new MilkingChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(entity);
    }

    /** Borra un ordeno. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        Milking entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Milking not found"));
        repository.delete(entity);
        events.publishEvent(new MilkingChangedEvent(TenantContext.requireAccountId()));
    }

    /**
     * Registra N ordenos en una sola transaccion. Si alguno duplica, rollback completo
     * con un mensaje que indica que animal fallo.
     */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public List<MilkingResponseDto> createBulk(MilkingBulkDto dto) {
        List<Milking> toSave = new ArrayList<>(dto.animals().size());
        for (MilkingBulkDto.AnimalMilking am : dto.animals()) {
            Milking m = new Milking();
            m.setAnimalId(am.animalId());
            m.setMilkingDate(dto.milkingDate());
            m.setSession(dto.session());
            m.setLiters(am.liters());
            toSave.add(m);
        }
        try {
            List<Milking> saved = repository.saveAll(toSave);
            repository.flush();
            events.publishEvent(new MilkingChangedEvent(TenantContext.requireAccountId()));
            return saved.stream().map(mapper::toDto).toList();
        } catch (DataIntegrityViolationException ex) {
            throw BusinessException.conflict(ErrorCode.CONFLICT,
                "Bulk milking failed: duplicate exists for date " + dto.milkingDate()
                    + " session " + dto.session());
        }
    }
}
