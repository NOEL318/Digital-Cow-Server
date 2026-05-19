package com.digitalcow.health.diagnosis;

import com.digitalcow.catalog.disease.Disease;
import com.digitalcow.catalog.disease.DiseaseRepository;
import com.digitalcow.catalog.disease.DiseaseSeverity;
import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.health.diagnosis.dto.DiagnosisCreateDto;
import com.digitalcow.health.diagnosis.dto.DiagnosisResponseDto;
import com.digitalcow.health.diagnosis.dto.DiagnosisUpdateDto;
import com.digitalcow.health.diagnosis.event.DiagnosisChangedEvent;
import com.digitalcow.health.diagnosis.mapper.DiagnosisMapper;
import com.digitalcow.health.treatment.TreatmentRepository;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de Diagnosis. Maneja transiciones de estado y autocalcula resolvedAt.
 */
@Service
@Transactional
public class DiagnosisService {

    private final DiagnosisRepository repository;
    private final DiseaseRepository diseaseRepository;
    private final TreatmentRepository treatmentRepository;
    private final DiagnosisMapper mapper;
    private final ApplicationEventPublisher events;

    public DiagnosisService(DiagnosisRepository repository,
                            DiseaseRepository diseaseRepository,
                            TreatmentRepository treatmentRepository,
                            DiagnosisMapper mapper,
                            ApplicationEventPublisher events) {
        this.repository = repository;
        this.diseaseRepository = diseaseRepository;
        this.treatmentRepository = treatmentRepository;
        this.mapper = mapper;
        this.events = events;
    }

    /** Lista diagnosticos de un animal en orden cronologico inverso. */
    @Transactional(readOnly = true)
    public List<DiagnosisResponseDto> listByAnimal(Long animalId) {
        return repository.findByAnimalIdOrderByDiagnosedAtDesc(animalId).stream()
            .map(this::toDto).toList();
    }

    /** Lista todos los diagnosticos de la cuenta. */
    @Transactional(readOnly = true)
    public List<DiagnosisResponseDto> list() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    /** Crea diagnostico. Status inicial siempre ACTIVE. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public DiagnosisResponseDto create(DiagnosisCreateDto dto) {
        diseaseRepository.findById(dto.diseaseId())
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Disease not found"));
        Diagnosis d = mapper.fromCreate(dto);
        d.setStatus(DiagnosisStatus.ACTIVE);
        if (dto.severity() != null) {
            d.setSeverity(dto.severity());
        } else {
            d.setSeverity(DiseaseSeverity.MEDIUM);
        }
        Diagnosis saved = repository.save(d);
        events.publishEvent(new DiagnosisChangedEvent(TenantContext.requireAccountId()));
        return toDto(saved);
    }

    /**
     * Actualiza un diagnostico. Si cambia a RECOVERED/CHRONIC/DECEASED y resolvedAt viene null,
     * se setea automaticamente a la fecha actual.
     */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public DiagnosisResponseDto update(Long id, DiagnosisUpdateDto dto) {
        Diagnosis d = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Diagnosis not found"));
        mapper.applyUpdate(dto, d);
        if (dto.status() != null
            && dto.status() != DiagnosisStatus.ACTIVE
            && d.getResolvedAt() == null
            && dto.resolvedAt() == null) {
            d.setResolvedAt(LocalDate.now());
        }
        events.publishEvent(new DiagnosisChangedEvent(TenantContext.requireAccountId()));
        return toDto(d);
    }

    /** Borra un diagnostico. Falla si tiene tratamientos asociados. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        Diagnosis d = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Diagnosis not found"));
        if (treatmentRepository.existsByDiagnosisId(id)) {
            throw BusinessException.conflict(ErrorCode.CONFLICT, "Diagnosis has associated treatments");
        }
        repository.delete(d);
        events.publishEvent(new DiagnosisChangedEvent(TenantContext.requireAccountId()));
    }

    private DiagnosisResponseDto toDto(Diagnosis d) {
        Disease disease = diseaseRepository.findById(d.getDiseaseId()).orElse(null);
        return mapper.toDto(d, disease);
    }
}
