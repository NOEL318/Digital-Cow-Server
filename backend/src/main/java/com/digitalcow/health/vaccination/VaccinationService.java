package com.digitalcow.health.vaccination;

import com.digitalcow.animal.Animal;
import com.digitalcow.animal.AnimalRepository;
import com.digitalcow.animal.AnimalStatus;
import com.digitalcow.catalog.vaccine.Vaccine;
import com.digitalcow.catalog.vaccine.VaccineRepository;
import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.health.vaccination.dto.VaccinationBulkDto;
import com.digitalcow.health.vaccination.dto.VaccinationCreateDto;
import com.digitalcow.health.vaccination.dto.VaccinationResponseDto;
import com.digitalcow.health.vaccination.dto.VaccinationUpdateDto;
import com.digitalcow.health.vaccination.event.VaccinationChangedEvent;
import com.digitalcow.health.vaccination.mapper.VaccinationMapper;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de Vaccination. Soporta vacunacion individual y por lote.
 * El bulk expande a una fila por animal activo del lote.
 */
@Service
@Transactional
public class VaccinationService {

    private final VaccinationRepository repository;
    private final VaccineRepository vaccineRepository;
    private final AnimalRepository animalRepository;
    private final VaccinationMapper mapper;
    private final ApplicationEventPublisher events;

    public VaccinationService(VaccinationRepository repository,
                              VaccineRepository vaccineRepository,
                              AnimalRepository animalRepository,
                              VaccinationMapper mapper,
                              ApplicationEventPublisher events) {
        this.repository = repository;
        this.vaccineRepository = vaccineRepository;
        this.animalRepository = animalRepository;
        this.mapper = mapper;
        this.events = events;
    }

    /** Lista vacunaciones de un animal en orden cronologico inverso. */
    @Transactional(readOnly = true)
    public List<VaccinationResponseDto> listByAnimal(Long animalId) {
        return repository.findByAnimalIdOrderByAppliedAtDesc(animalId).stream()
            .map(this::toDto)
            .toList();
    }

    /** Lista todas las vacunaciones de la cuenta del usuario actual. */
    @Transactional(readOnly = true)
    public List<VaccinationResponseDto> list() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    /** Crea vacunacion individual y calcula next_dose_due segun catalogo. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public VaccinationResponseDto create(VaccinationCreateDto dto) {
        Vaccine vaccine = vaccineRepository.findById(dto.vaccineId())
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Vaccine not found"));
        Vaccination entity = mapper.fromCreate(dto);
        entity.setNextDoseDue(computeNextDoseDue(dto.appliedAt(), vaccine));
        Vaccination saved = repository.save(entity);
        events.publishEvent(new VaccinationChangedEvent(TenantContext.requireAccountId()));
        return toDto(saved);
    }

    /** Crea vacunaciones masivas: una fila por animal activo del lote. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public List<VaccinationResponseDto> createBulk(VaccinationBulkDto dto) {
        Vaccine vaccine = vaccineRepository.findById(dto.vaccineId())
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Vaccine not found"));
        List<Animal> animals = animalRepository.findByLotIdAndStatus(dto.lotId(), AnimalStatus.ACTIVE);
        if (animals.isEmpty()) {
            throw BusinessException.conflict(ErrorCode.CONFLICT, "Lot has no active animals");
        }
        LocalDate nextDose = computeNextDoseDue(dto.appliedAt(), vaccine);
        List<Vaccination> rows = animals.stream().map(animal -> {
            Vaccination v = new Vaccination();
            v.setAnimalId(animal.getId());
            v.setLotId(dto.lotId());
            v.setVaccineId(dto.vaccineId());
            v.setBatchNumber(dto.batchNumber());
            v.setAppliedAt(dto.appliedAt());
            v.setDoseMl(dto.doseMl());
            v.setRoute(dto.route());
            v.setNextDoseDue(nextDose);
            v.setCost(dto.cost());
            v.setVetVisitId(dto.vetVisitId());
            v.setNotes(dto.notes());
            return v;
        }).toList();
        List<Vaccination> saved = repository.saveAll(rows);
        events.publishEvent(new VaccinationChangedEvent(TenantContext.requireAccountId()));
        return saved.stream().map(this::toDto).toList();
    }

    /** Actualiza una vacunacion existente. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public VaccinationResponseDto update(Long id, VaccinationUpdateDto dto) {
        Vaccination entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Vaccination not found"));
        mapper.applyUpdate(dto, entity);
        events.publishEvent(new VaccinationChangedEvent(TenantContext.requireAccountId()));
        return toDto(entity);
    }

    /** Borra una vacunacion. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        Vaccination entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Vaccination not found"));
        repository.delete(entity);
        events.publishEvent(new VaccinationChangedEvent(TenantContext.requireAccountId()));
    }

    /** Calcula la fecha de proxima dosis sumando recommended_frequency_months. */
    private LocalDate computeNextDoseDue(LocalDate appliedAt, Vaccine vaccine) {
        Short months = vaccine.getRecommendedFrequencyMonths();
        return months != null && months > 0 ? appliedAt.plusMonths(months) : null;
    }

    private VaccinationResponseDto toDto(Vaccination v) {
        Vaccine vaccine = vaccineRepository.findById(v.getVaccineId()).orElse(null);
        return mapper.toDto(v, vaccine);
    }
}
