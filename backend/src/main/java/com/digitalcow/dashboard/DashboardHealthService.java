package com.digitalcow.dashboard;

import com.digitalcow.dashboard.dto.DashboardHealthDto;
import com.digitalcow.health.diagnosis.event.DiagnosisChangedEvent;
import com.digitalcow.health.treatment.event.TreatmentChangedEvent;
import com.digitalcow.health.vaccination.event.VaccinationChangedEvent;
import com.digitalcow.tenancy.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio del widget de salud del dashboard.
 * Queries agregadas con native SQL, cacheable por tenant.
 */
@Service
@Transactional(readOnly = true)
public class DashboardHealthService {

    @PersistenceContext
    private EntityManager em;

    /** Construye el resumen de salud para el tenant activo. */
    @Cacheable(value = "dashboard-health", keyGenerator = "tenantKeyGenerator")
    public DashboardHealthDto build() {
        LocalDate today = LocalDate.now();
        LocalDate in7 = today.plusDays(7);
        LocalDate in30 = today.plusDays(30);
        LocalDate quarterStart = today.minusMonths(3);

        long up7 = countNative(
            "SELECT COUNT(*) FROM vaccination WHERE account_id = :a AND next_dose_due BETWEEN :from AND :to",
            today, in7);
        long up30 = countNative(
            "SELECT COUNT(*) FROM vaccination WHERE account_id = :a AND next_dose_due BETWEEN :from AND :to",
            today, in30);
        long activeDiag = countScalar(
            "SELECT COUNT(*) FROM diagnosis WHERE account_id = :a AND status = 'ACTIVE'");
        long activeTreat = countScalar(
            "SELECT COUNT(*) FROM treatment WHERE account_id = :a AND ended_at IS NULL");
        BigDecimal monthSpend = monthVetSpend(today);
        List<DashboardHealthDto.TopDiseaseDto> topDiseases = topDiseases(quarterStart);

        return new DashboardHealthDto(up7, up30, activeDiag, activeTreat, monthSpend, topDiseases);
    }

    /** Invalida el cache al cambiar vacunaciones, tratamientos o diagnosticos. */
    @EventListener
    @CacheEvict(value = "dashboard-health", allEntries = true)
    public void onVaccination(VaccinationChangedEvent event) { /* invalidate */ }

    /** Este metodo invalida el cache cuando cambia un tratamiento. */
    @EventListener
    @CacheEvict(value = "dashboard-health", allEntries = true)
    public void onTreatment(TreatmentChangedEvent event) { /* invalidate */ }

    /** Este metodo invalida el cache cuando cambia un diagnostico. */
    @EventListener
    @CacheEvict(value = "dashboard-health", allEntries = true)
    public void onDiagnosis(DiagnosisChangedEvent event) { /* invalidate */ }

    private long countScalar(String sql) {
        Object o = em.createNativeQuery(sql)
            .setParameter("a", TenantContext.get())
            .getSingleResult();
        return ((Number) o).longValue();
    }

    private long countNative(String sql, LocalDate from, LocalDate to) {
        Object o = em.createNativeQuery(sql)
            .setParameter("a", TenantContext.get())
            .setParameter("from", from)
            .setParameter("to", to)
            .getSingleResult();
        return ((Number) o).longValue();
    }

    /**
     * Suma costos del mes en vet_visit, vaccination, treatment, pest_control.
     */
    private BigDecimal monthVetSpend(LocalDate today) {
        LocalDate monthStart = today.withDayOfMonth(1);
        String sql = "SELECT COALESCE(SUM(c), 0) FROM ( "
            + " SELECT total_cost AS c FROM vet_visit WHERE account_id = :a AND visited_at >= :ms "
            + " UNION ALL "
            + " SELECT cost FROM vaccination WHERE account_id = :a AND applied_at >= :ms "
            + " UNION ALL "
            + " SELECT cost FROM treatment WHERE account_id = :a AND started_at >= :ms "
            + " UNION ALL "
            + " SELECT cost FROM pest_control WHERE account_id = :a AND applied_at >= :ms "
            + ") sub";
        Object o = em.createNativeQuery(sql)
            .setParameter("a", TenantContext.get())
            .setParameter("ms", monthStart)
            .getSingleResult();
        if (o == null) return BigDecimal.ZERO;
        return o instanceof BigDecimal bd ? bd : new BigDecimal(o.toString());
    }

    /** Top 5 enfermedades del trimestre por numero de diagnosticos. */
    @SuppressWarnings("unchecked")
    private List<DashboardHealthDto.TopDiseaseDto> topDiseases(LocalDate from) {
        String sql = "SELECT d.code, d.name_es, COUNT(dg.id) AS cnt "
            + "FROM diagnosis dg JOIN disease d ON d.id = dg.disease_id "
            + "WHERE dg.account_id = :a AND dg.diagnosed_at >= :from "
            + "GROUP BY d.code, d.name_es ORDER BY cnt DESC LIMIT 5";
        List<Object[]> rows = em.createNativeQuery(sql)
            .setParameter("a", TenantContext.get())
            .setParameter("from", from)
            .getResultList();
        List<DashboardHealthDto.TopDiseaseDto> out = new ArrayList<>();
        for (Object[] r : rows) {
            out.add(new DashboardHealthDto.TopDiseaseDto(
                (String) r[0],
                (String) r[1],
                ((Number) r[2]).longValue()
            ));
        }
        return out;
    }
}
