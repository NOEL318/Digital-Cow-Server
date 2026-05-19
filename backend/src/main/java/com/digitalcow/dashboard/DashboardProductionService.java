package com.digitalcow.dashboard;

import com.digitalcow.dashboard.dto.DashboardProductionDto;
import com.digitalcow.production.milking.event.MilkingChangedEvent;
import com.digitalcow.production.weighing.event.WeighingChangedEvent;
import com.digitalcow.tenancy.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Servicio del widget de produccion del dashboard.
 * Queries agregadas con native SQL, cacheable por tenant.
 */
@Service
@Transactional(readOnly = true)
public class DashboardProductionService {

    @PersistenceContext
    private EntityManager em;

    /** Construye el resumen de produccion para el tenant activo. */
    @Cacheable(value = "dashboard-production", keyGenerator = "tenantKeyGenerator")
    public DashboardProductionDto build() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate last30 = today.minusDays(30);

        BigDecimal todayMilk = sumLiters(today, today);
        BigDecimal mtdMilk = sumLiters(monthStart, today);
        Double avgAdg = avgAdgThisMonth(monthStart, today);
        long activeCows = activeMilkingCows(last30);

        return new DashboardProductionDto(todayMilk, mtdMilk, avgAdg, activeCows);
    }

    /** Este metodo invalida el cache cuando cambia un ordeno. */
    @EventListener
    @CacheEvict(value = "dashboard-production", allEntries = true)
    public void onMilking(MilkingChangedEvent event) { /* invalidate */ }

    /** Este metodo invalida el cache cuando cambia una pesada. */
    @EventListener
    @CacheEvict(value = "dashboard-production", allEntries = true)
    public void onWeighing(WeighingChangedEvent event) { /* invalidate */ }

    private BigDecimal sumLiters(LocalDate from, LocalDate to) {
        String sql = "SELECT COALESCE(SUM(liters), 0) FROM milking "
            + "WHERE account_id = :a AND milking_date BETWEEN :f AND :t";
        Object o = em.createNativeQuery(sql)
            .setParameter("a", TenantContext.get())
            .setParameter("f", from)
            .setParameter("t", to)
            .getSingleResult();
        if (o == null) return BigDecimal.ZERO;
        return o instanceof BigDecimal bd ? bd : new BigDecimal(o.toString());
    }

    /** Promedio de ADG por animal con >=2 pesajes en el mes. */
    @SuppressWarnings("unchecked")
    private Double avgAdgThisMonth(LocalDate from, LocalDate to) {
        String sql = "SELECT animal_id, MIN(weighed_at) AS min_d, MAX(weighed_at) AS max_d "
            + "FROM weighing WHERE account_id = :a AND weighed_at BETWEEN :f AND :t "
            + "GROUP BY animal_id HAVING COUNT(*) >= 2";
        List<Object[]> rows = em.createNativeQuery(sql)
            .setParameter("a", TenantContext.get())
            .setParameter("f", from)
            .setParameter("t", to)
            .getResultList();
        if (rows.isEmpty()) return null;

        double sum = 0;
        int count = 0;
        for (Object[] r : rows) {
            Long animalId = ((Number) r[0]).longValue();
            java.sql.Date minD = (java.sql.Date) r[1];
            java.sql.Date maxD = (java.sql.Date) r[2];
            if (minD == null || maxD == null) continue;
            LocalDate first = minD.toLocalDate();
            LocalDate last = maxD.toLocalDate();
            long days = ChronoUnit.DAYS.between(first, last);
            if (days <= 0) continue;
            BigDecimal firstW = weightAt(animalId, first);
            BigDecimal lastW = weightAt(animalId, last);
            if (firstW == null || lastW == null) continue;
            double adg = lastW.subtract(firstW).doubleValue() / days;
            sum += adg;
            count++;
        }
        if (count == 0) return null;
        return BigDecimal.valueOf(sum / count)
            .setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

    private BigDecimal weightAt(Long animalId, LocalDate date) {
        String sql = "SELECT weight_kg FROM weighing "
            + "WHERE account_id = :a AND animal_id = :id AND weighed_at = :d "
            + "ORDER BY id ASC LIMIT 1";
        List<?> list = em.createNativeQuery(sql)
            .setParameter("a", TenantContext.get())
            .setParameter("id", animalId)
            .setParameter("d", date)
            .getResultList();
        if (list.isEmpty()) return null;
        Object o = list.get(0);
        return o instanceof BigDecimal bd ? bd : new BigDecimal(o.toString());
    }

    /** Vacas DAIRY activas con al menos un ordeno desde la fecha indicada. */
    private long activeMilkingCows(LocalDate sinceInclusive) {
        String sql = "SELECT COUNT(DISTINCT m.animal_id) FROM milking m "
            + "JOIN animal a ON a.id = m.animal_id "
            + "WHERE m.account_id = :a AND a.account_id = :a "
            + "AND a.status = 'ACTIVE' AND a.purpose IN ('DAIRY','DUAL') "
            + "AND m.milking_date >= :since";
        Object o = em.createNativeQuery(sql)
            .setParameter("a", TenantContext.get())
            .setParameter("since", sinceInclusive)
            .getSingleResult();
        return o == null ? 0L : ((Number) o).longValue();
    }
}
