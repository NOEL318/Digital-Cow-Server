package com.digitalcow.health.alerts;

import com.digitalcow.animal.AnimalRepository;
import com.digitalcow.health.alerts.dto.AlertItemDto;
import com.digitalcow.health.alerts.dto.HealthAlertsDto;
import com.digitalcow.health.diagnosis.Diagnosis;
import com.digitalcow.health.diagnosis.DiagnosisRepository;
import com.digitalcow.health.diagnosis.event.DiagnosisChangedEvent;
import com.digitalcow.health.treatment.Treatment;
import com.digitalcow.health.treatment.TreatmentRepository;
import com.digitalcow.health.treatment.event.TreatmentChangedEvent;
import com.digitalcow.health.vaccination.Vaccination;
import com.digitalcow.health.vaccination.VaccinationRepository;
import com.digitalcow.health.vaccination.event.VaccinationChangedEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Calculo de alertas sanitarias del tenant. Cacheable por accountId.
 * Se invalida cuando cambian vacunaciones, tratamientos o diagnosticos.
 * FUTURE: missing mandatory vaccinations (correlacionar animal_health_plan + step + vaccination).
 */
@Service
@Transactional(readOnly = true)
public class HealthAlertsService {

    private final VaccinationRepository vaccinationRepository;
    private final TreatmentRepository treatmentRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final AnimalRepository animalRepository;

    public HealthAlertsService(VaccinationRepository vaccinationRepository,
                               TreatmentRepository treatmentRepository,
                               DiagnosisRepository diagnosisRepository,
                               AnimalRepository animalRepository) {
        this.vaccinationRepository = vaccinationRepository;
        this.treatmentRepository = treatmentRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.animalRepository = animalRepository;
    }

    /** Construye el set de alertas para el tenant activo. */
    @Cacheable(value = "health-alerts", keyGenerator = "tenantKeyGenerator")
    public HealthAlertsDto build() {
        LocalDate today = LocalDate.now();
        LocalDate in7 = today.plusDays(7);
        LocalDate in30 = today.plusDays(30);

        List<AlertItemDto> up7 = vaccinationRepository.findUpcomingDoses(today, in7).stream()
            .map(v -> toVacAlert(v, "UPCOMING_VACCINATION")).toList();

        List<AlertItemDto> up30 = vaccinationRepository.findUpcomingDoses(today, in30).stream()
            .map(v -> toVacAlert(v, "UPCOMING_VACCINATION")).toList();

        List<AlertItemDto> milk = treatmentRepository.findActiveMilkWithdrawals(today).stream()
            .map(t -> toTreatAlert(t, t.getWithdrawalMilkUntil(), "WITHDRAWAL_MILK")).toList();

        List<AlertItemDto> meat = treatmentRepository.findActiveMeatWithdrawals(today).stream()
            .map(t -> toTreatAlert(t, t.getWithdrawalMeatUntil(), "WITHDRAWAL_MEAT")).toList();

        List<AlertItemDto> diag = diagnosisRepository.findActiveWithoutTreatment().stream()
            .map(this::toDiagAlert).toList();

        return new HealthAlertsDto(up7, up30, milk, meat, diag);
    }

    /** Invalida el cache al cambiar una vacunacion. */
    @EventListener
    @CacheEvict(value = "health-alerts", allEntries = true)
    public void onVaccination(VaccinationChangedEvent event) { /* invalidate */ }

    /** Invalida el cache al cambiar un tratamiento. */
    @EventListener
    @CacheEvict(value = "health-alerts", allEntries = true)
    public void onTreatment(TreatmentChangedEvent event) { /* invalidate */ }

    /** Invalida el cache al cambiar un diagnostico. */
    @EventListener
    @CacheEvict(value = "health-alerts", allEntries = true)
    public void onDiagnosis(DiagnosisChangedEvent event) { /* invalidate */ }

    private AlertItemDto toVacAlert(Vaccination v, String type) {
        return new AlertItemDto(type, v.getAnimalId(), animalTag(v.getAnimalId()),
            "Proxima dosis", v.getNextDoseDue(), v.getId());
    }

    private AlertItemDto toTreatAlert(Treatment t, LocalDate until, String type) {
        return new AlertItemDto(type, t.getAnimalId(), animalTag(t.getAnimalId()),
            "Retiro activo", until, t.getId());
    }

    private AlertItemDto toDiagAlert(Diagnosis d) {
        return new AlertItemDto(
            "ACTIVE_DIAGNOSIS_NO_TREATMENT",
            d.getAnimalId(),
            animalTag(d.getAnimalId()),
            "Sin tratamiento",
            d.getDiagnosedAt(),
            d.getId()
        );
    }

    private String animalTag(Long animalId) {
        if (animalId == null) return "";
        return animalRepository.findById(animalId).map(a -> a.getInternalTag()).orElse("");
    }
}
