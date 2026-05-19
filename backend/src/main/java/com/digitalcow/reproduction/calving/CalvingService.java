package com.digitalcow.reproduction.calving;

import com.digitalcow.animal.Animal;
import com.digitalcow.animal.AnimalRepository;
import com.digitalcow.animal.AnimalStatus;
import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.reproduction.bull.Bull;
import com.digitalcow.reproduction.bull.BullRepository;
import com.digitalcow.reproduction.bull.BullSource;
import com.digitalcow.reproduction.calving.dto.CalvingCreateDto;
import com.digitalcow.reproduction.calving.dto.CalvingResponseDto;
import com.digitalcow.reproduction.calving.dto.CalvingUpdateDto;
import com.digitalcow.reproduction.calving.event.CalvingChangedEvent;
import com.digitalcow.reproduction.calving.mapper.CalvingMapper;
import com.digitalcow.reproduction.service.ServiceEvent;
import com.digitalcow.reproduction.service.ServiceEventRepository;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio CRUD de Calving con creacion opcional de un Animal hijo.
 * Cuando createCalfAnimal=true en el dto, registra el becerro como nuevo Animal
 * y enlaza dam_id con la madre y sire_id (o external_sire_name) con el toro
 * del ultimo servicio registrado de la madre.
 */
@Service
@Transactional
public class CalvingService {

    private final CalvingRepository repository;
    private final CalvingMapper mapper;
    private final AnimalRepository animalRepository;
    private final ServiceEventRepository serviceEventRepository;
    private final BullRepository bullRepository;
    private final ApplicationEventPublisher events;

    public CalvingService(CalvingRepository repository,
                          CalvingMapper mapper,
                          AnimalRepository animalRepository,
                          ServiceEventRepository serviceEventRepository,
                          BullRepository bullRepository,
                          ApplicationEventPublisher events) {
        this.repository = repository;
        this.mapper = mapper;
        this.animalRepository = animalRepository;
        this.serviceEventRepository = serviceEventRepository;
        this.bullRepository = bullRepository;
        this.events = events;
    }

    /** Lista todos los partos de la cuenta. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<CalvingResponseDto> list() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    /** Lista los partos de un animal en orden cronologico inverso. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<CalvingResponseDto> listByAnimal(Long animalId) {
        return repository.findByAnimalIdOrderByCalvedAtDesc(animalId).stream()
            .map(mapper::toDto)
            .toList();
    }

    /**
     * Crea un parto. Si createCalfAnimal=true, registra el becerro como Animal nuevo
     * enlazado por dam_id a la madre y por sire_id/external_sire_name al toro del
     * ultimo servicio.
     */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public CalvingResponseDto create(CalvingCreateDto dto) {
        Calving entity = mapper.fromCreate(dto);

        if (dto.createCalfAnimal()) {
            if (dto.calfInternalTag() == null || dto.calfRanchId() == null
                    || dto.calfBreedId() == null || dto.calfPurpose() == null
                    || dto.calfSex() == null) {
                throw BusinessException.badRequest(ErrorCode.VALIDATION_ERROR,
                    "Missing calf data: internalTag, ranchId, breedId, purpose and sex are required");
            }
            Animal calf = buildCalf(dto);
            Animal savedCalf = animalRepository.save(calf);
            entity.setCalfAnimalId(savedCalf.getId());
        }

        Calving saved = repository.save(entity);
        events.publishEvent(new CalvingChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(saved);
    }

    /** Actualiza un parto. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public CalvingResponseDto update(Long id, CalvingUpdateDto dto) {
        Calving entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Calving not found"));
        mapper.applyUpdate(dto, entity);
        events.publishEvent(new CalvingChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(entity);
    }

    /** Borra un parto. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        Calving entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Calving not found"));
        repository.delete(entity);
        events.publishEvent(new CalvingChangedEvent(TenantContext.requireAccountId()));
    }

    private Animal buildCalf(CalvingCreateDto dto) {
        Animal calf = new Animal();
        calf.setInternalTag(dto.calfInternalTag());
        calf.setRanchId(dto.calfRanchId());
        calf.setLotId(dto.calfLotId());
        calf.setBreedId(dto.calfBreedId());
        calf.setPurpose(dto.calfPurpose());
        calf.setSex(dto.calfSex());
        calf.setBirthDate(dto.calvedAt());
        calf.setBirthDateEstimated(false);
        calf.setBirthWeightKg(dto.calfBirthWeightKg());
        calf.setDamId(dto.animalId());
        calf.setStatus(AnimalStatus.ACTIVE);
        if (dto.createdByUserId() != null) {
            calf.setCreatedByUserId(dto.createdByUserId());
        }
        applySireFromLastService(calf, dto.animalId());
        return calf;
    }

    /**
     * Aplica sire_id o external_sire_name al becerro tomando el toro del ultimo
     * servicio registrado de la madre. Si el toro es OWN y tiene animalId, usa
     * sireId; si es EXTERNAL, usa externalSireName con el nombre del toro.
     */
    private void applySireFromLastService(Animal calf, Long damId) {
        Optional<ServiceEvent> lastService = serviceEventRepository
            .findByAnimalIdOrderByServiceDateDesc(damId)
            .stream().findFirst();
        if (lastService.isEmpty()) return;
        ServiceEvent svc = lastService.get();
        if (svc.getBullId() == null) return;
        Bull bull = bullRepository.findById(svc.getBullId()).orElse(null);
        if (bull == null) return;
        if (bull.getSource() == BullSource.OWN && bull.getAnimalId() != null) {
            calf.setSireId(bull.getAnimalId());
        } else {
            calf.setExternalSireName(bull.getName());
        }
    }
}
