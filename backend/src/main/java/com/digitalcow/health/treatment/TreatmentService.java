package com.digitalcow.health.treatment;

import com.digitalcow.catalog.medication.Medication;
import com.digitalcow.catalog.medication.MedicationRepository;
import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.health.treatment.dto.TreatmentCreateDto;
import com.digitalcow.health.treatment.dto.TreatmentResponseDto;
import com.digitalcow.health.treatment.dto.TreatmentUpdateDto;
import com.digitalcow.health.treatment.event.TreatmentChangedEvent;
import com.digitalcow.health.treatment.mapper.TreatmentMapper;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de Treatment. Calcula withdrawal_milk_until y withdrawal_meat_until
 * basado en la medicacion catalogada.
 */
@Service
@Transactional
public class TreatmentService {

    private final TreatmentRepository repository;
    private final MedicationRepository medicationRepository;
    private final TreatmentMapper mapper;
    private final ApplicationEventPublisher events;

    public TreatmentService(TreatmentRepository repository,
                            MedicationRepository medicationRepository,
                            TreatmentMapper mapper,
                            ApplicationEventPublisher events) {
        this.repository = repository;
        this.medicationRepository = medicationRepository;
        this.mapper = mapper;
        this.events = events;
    }

    /** Lista tratamientos de un animal en orden cronologico inverso. */
    @Transactional(readOnly = true)
    public List<TreatmentResponseDto> listByAnimal(Long animalId) {
        return repository.findByAnimalIdOrderByStartedAtDesc(animalId).stream()
            .map(this::toDto).toList();
    }

    /** Lista todos los tratamientos de la cuenta. */
    @Transactional(readOnly = true)
    public List<TreatmentResponseDto> list() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    /**
     * Este metodo crea un tratamiento y calcula las fechas en las que termina
     * el periodo de retiro de leche y de carne, basandose en el medicamento
     * elegido. La medicacion se busca con la consulta filtrada por cuenta
     * para evitar referenciar medicamentos de otros tenants.
     */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public TreatmentResponseDto create(TreatmentCreateDto dto) {
        Medication med = findMedicationForCurrentTenant(dto.medicationId());
        Treatment t = mapper.fromCreate(dto);
        applyWithdrawals(t, med);
        Treatment saved = repository.save(t);
        events.publishEvent(new TreatmentChangedEvent(TenantContext.requireAccountId()));
        return toDto(saved);
    }

    /**
     * Este metodo actualiza un tratamiento y recalcula las fechas de retiro.
     * La medicacion se vuelve a leer con el filtro de tenant para no aceptar
     * cambios que apunten a una medicacion de otra cuenta.
     */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public TreatmentResponseDto update(Long id, TreatmentUpdateDto dto) {
        Treatment t = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Treatment not found"));
        mapper.applyUpdate(dto, t);
        Medication med = findMedicationForCurrentTenant(t.getMedicationId());
        applyWithdrawals(t, med);
        events.publishEvent(new TreatmentChangedEvent(TenantContext.requireAccountId()));
        return toDto(t);
    }

    /**
     * Este metodo busca una medicacion verificando que pertenezca a la cuenta
     * actual o sea un seed global. Evita el camino donde un usuario podria
     * referenciar la medicacion de otro tenant pasando un id arbitrario.
     */
    private Medication findMedicationForCurrentTenant(Long medicationId) {
        Long accountId = TenantContext.requireAccountId();
        return medicationRepository.findVisibleByIdForAccount(medicationId, accountId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Medication not found"));
    }

    /** Borra un tratamiento. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        Treatment t = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Treatment not found"));
        repository.delete(t);
        events.publishEvent(new TreatmentChangedEvent(TenantContext.requireAccountId()));
    }

    /**
     * Setea withdrawal_milk_until y withdrawal_meat_until.
     * Base date = endedAt si existe, sino startedAt.
     */
    private void applyWithdrawals(Treatment t, Medication med) {
        LocalDate base = t.getEndedAt() != null ? t.getEndedAt() : t.getStartedAt();
        if (med.getWithdrawalMilkDays() > 0) {
            t.setWithdrawalMilkUntil(base.plusDays(med.getWithdrawalMilkDays()));
        } else {
            t.setWithdrawalMilkUntil(null);
        }
        if (med.getWithdrawalMeatDays() > 0) {
            t.setWithdrawalMeatUntil(base.plusDays(med.getWithdrawalMeatDays()));
        } else {
            t.setWithdrawalMeatUntil(null);
        }
    }

    private TreatmentResponseDto toDto(Treatment t) {
        // En la conversion a DTO solo se lee la medicacion para enriquecer la respuesta.
        // Se filtra por cuenta para que no aparezcan datos de medicamentos ajenos en caso
        // de que existieran inconsistencias historicas en la base.
        Long accountId = TenantContext.get();
        Medication med = accountId == null
            ? medicationRepository.findById(t.getMedicationId()).orElse(null)
            : medicationRepository.findVisibleByIdForAccount(t.getMedicationId(), accountId).orElse(null);
        return mapper.toDto(t, med);
    }
}
