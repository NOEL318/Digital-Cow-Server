package com.digitalcow.production.kpis;

import com.digitalcow.production.kpis.dto.ProductionKpisDto;
import com.digitalcow.tenancy.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * KPIs de produccion del tenant para el periodo [from, to].
 * Usa native SQL para agregaciones; calcula avgAdgKgDay con dos pesajes por animal.
 */
@Service
@Transactional(readOnly = true)
public class ProductionKpisService {

    @PersistenceContext
    private EntityManager em;

    /** Construye los KPIs del periodo. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public ProductionKpisDto build(LocalDate from, LocalDate to) {
        BigDecimal total = totalMilkLiters(from, to);
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        BigDecimal avgDaily = days > 0
            ? total.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        Double avgAdg = avgAdgKgDay(from, to);
        List<ProductionKpisDto.TopProducer> top = topProducers(from, to);
        return new ProductionKpisDto(from, to, total, avgDaily, avgAdg, top);
    }

    private BigDecimal totalMilkLiters(LocalDate from, LocalDate to) {
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

    /**
     * Promedio de ADG por animal en el periodo. Para cada animal con >=2 pesajes
     * en el rango, calcula (peso_ultimo - peso_primero) / dias, luego promedia.
     */
    @SuppressWarnings("unchecked")
    private Double avgAdgKgDay(LocalDate from, LocalDate to) {
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
        for (Object[] row : rows) {
            Long animalId = ((Number) row[0]).longValue();
            java.sql.Date minD = (java.sql.Date) row[1];
            java.sql.Date maxD = (java.sql.Date) row[2];
            if (minD == null || maxD == null) continue;
            LocalDate first = minD.toLocalDate();
            LocalDate last = maxD.toLocalDate();
            long daysBetween = ChronoUnit.DAYS.between(first, last);
            if (daysBetween <= 0) continue;
            BigDecimal firstW = weighingAt(animalId, first);
            BigDecimal lastW = weighingAt(animalId, last);
            if (firstW == null || lastW == null) continue;
            double adg = lastW.subtract(firstW).doubleValue() / daysBetween;
            sum += adg;
            count++;
        }
        if (count == 0) return null;
        return sum / count;
    }

    private BigDecimal weighingAt(Long animalId, LocalDate date) {
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

    /** Top 5 animales por litros producidos en el periodo. */
    @SuppressWarnings("unchecked")
    private List<ProductionKpisDto.TopProducer> topProducers(LocalDate from, LocalDate to) {
        String sql = "SELECT m.animal_id, a.internal_tag, SUM(m.liters) AS total "
            + "FROM milking m JOIN animal a ON a.id = m.animal_id "
            + "WHERE m.account_id = :a AND m.milking_date BETWEEN :f AND :t "
            + "GROUP BY m.animal_id, a.internal_tag "
            + "ORDER BY total DESC LIMIT 5";
        List<Object[]> rows = em.createNativeQuery(sql)
            .setParameter("a", TenantContext.get())
            .setParameter("f", from)
            .setParameter("t", to)
            .getResultList();
        List<ProductionKpisDto.TopProducer> out = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            Long animalId = ((Number) r[0]).longValue();
            String tag = (String) r[1];
            Object o = r[2];
            BigDecimal liters = o instanceof BigDecimal bd ? bd : new BigDecimal(o.toString());
            out.add(new ProductionKpisDto.TopProducer(animalId, tag, liters));
        }
        return out;
    }
}
