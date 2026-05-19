package com.digitalcow.dashboard;

import com.digitalcow.dashboard.dto.DashboardReproductionDto;
import com.digitalcow.reproduction.calving.event.CalvingChangedEvent;
import com.digitalcow.reproduction.heat.event.HeatChangedEvent;
import com.digitalcow.reproduction.pregnancy.event.PregnancyCheckChangedEvent;
import com.digitalcow.reproduction.service.event.ServiceEventChangedEvent;
import com.digitalcow.tenancy.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Servicio del widget de reproduccion del dashboard.
 * Queries agregadas con native SQL, cacheable por tenant.
 */
@Service
@Transactional(readOnly = true)
public class DashboardReproductionService {

    @PersistenceContext
    private EntityManager em;

    /** Construye el resumen reproductivo para el tenant activo. */
    @Cacheable(value = "dashboard-reproduction", keyGenerator = "tenantKeyGenerator")
    public DashboardReproductionDto build() {
        LocalDate today = LocalDate.now();
        LocalDate in21 = today.plusDays(21);

        long pregnant = pregnantConfirmed();
        long upcoming = upcomingCalvings(today, in21);
        long open = openCows();
        Double avgOpen = avgDaysOpen(today);

        return new DashboardReproductionDto(pregnant, upcoming, open, avgOpen);
    }

    /** Este metodo invalida el cache cuando cambia un celo. */
    @EventListener
    @CacheEvict(value = "dashboard-reproduction", allEntries = true)
    public void onHeat(HeatChangedEvent event) { /* invalidate */ }

    /** Este metodo invalida el cache cuando cambia un servicio reproductivo. */
    @EventListener
    @CacheEvict(value = "dashboard-reproduction", allEntries = true)
    public void onService(ServiceEventChangedEvent event) { /* invalidate */ }

    /** Este metodo invalida el cache cuando cambia un chequeo de prenez. */
    @EventListener
    @CacheEvict(value = "dashboard-reproduction", allEntries = true)
    public void onPregnancyCheck(PregnancyCheckChangedEvent event) { /* invalidate */ }

    /** Este metodo invalida el cache cuando cambia un parto. */
    @EventListener
    @CacheEvict(value = "dashboard-reproduction", allEntries = true)
    public void onCalving(CalvingChangedEvent event) { /* invalidate */ }

    /** Animales con un check POSITIVE sin parto/aborto posterior. */
    private long pregnantConfirmed() {
        String sql = "SELECT COUNT(DISTINCT pc.animal_id) "
            + "FROM pregnancy_check pc "
            + "WHERE pc.account_id = :a AND pc.result = 'POSITIVE' "
            + "AND NOT EXISTS ( "
            + "  SELECT 1 FROM calving c WHERE c.account_id = :a "
            + "  AND c.animal_id = pc.animal_id AND c.calved_at > pc.checked_at "
            + ") "
            + "AND NOT EXISTS ( "
            + "  SELECT 1 FROM abortion ab WHERE ab.account_id = :a "
            + "  AND ab.animal_id = pc.animal_id AND ab.aborted_at > pc.checked_at "
            + ")";
        Object o = em.createNativeQuery(sql)
            .setParameter("a", TenantContext.get())
            .getSingleResult();
        return o == null ? 0L : ((Number) o).longValue();
    }

    private long upcomingCalvings(LocalDate from, LocalDate to) {
        String sql = "SELECT COUNT(*) FROM pregnancy_check pc "
            + "WHERE pc.account_id = :a AND pc.result = 'POSITIVE' "
            + "AND pc.estimated_calving_date BETWEEN :from AND :to";
        Object o = em.createNativeQuery(sql)
            .setParameter("a", TenantContext.get())
            .setParameter("from", from)
            .setParameter("to", to)
            .getSingleResult();
        return o == null ? 0L : ((Number) o).longValue();
    }

    /** Vacas activas con al menos una calving y sin check POSITIVE posterior. */
    private long openCows() {
        String sql = "SELECT COUNT(*) FROM ( "
            + "  SELECT DISTINCT c.animal_id FROM calving c "
            + "  JOIN animal a ON a.id = c.animal_id "
            + "  WHERE c.account_id = :a AND a.account_id = :a "
            + "  AND a.status = 'ACTIVE' AND a.sex = 'FEMALE' "
            + "  AND NOT EXISTS ( "
            + "    SELECT 1 FROM pregnancy_check pc WHERE pc.account_id = :a "
            + "    AND pc.animal_id = c.animal_id AND pc.result = 'POSITIVE' "
            + "    AND pc.checked_at > c.calved_at "
            + "  ) "
            + ") sub";
        Object o = em.createNativeQuery(sql)
            .setParameter("a", TenantContext.get())
            .getSingleResult();
        return o == null ? 0L : ((Number) o).longValue();
    }

    /**
     * Promedio de dias entre la ultima calving y hoy para vacas activas
     * que aun no han concebido (open cows). Retorna null si no hay datos.
     */
    private Double avgDaysOpen(LocalDate today) {
        String sql = "SELECT AVG(DATEDIFF(:today, last_c.calved_at)) "
            + "FROM ( "
            + "  SELECT c.animal_id, MAX(c.calved_at) AS calved_at "
            + "  FROM calving c WHERE c.account_id = :a "
            + "  GROUP BY c.animal_id "
            + ") last_c "
            + "JOIN animal a ON a.id = last_c.animal_id "
            + "WHERE a.account_id = :a AND a.status = 'ACTIVE' AND a.sex = 'FEMALE' "
            + "AND NOT EXISTS ( "
            + "  SELECT 1 FROM pregnancy_check pc WHERE pc.account_id = :a "
            + "  AND pc.animal_id = last_c.animal_id AND pc.result = 'POSITIVE' "
            + "  AND pc.checked_at > last_c.calved_at "
            + ")";
        Object o = em.createNativeQuery(sql)
            .setParameter("a", TenantContext.get())
            .setParameter("today", today)
            .getSingleResult();
        if (o == null) return null;
        return ((Number) o).doubleValue();
    }
}
