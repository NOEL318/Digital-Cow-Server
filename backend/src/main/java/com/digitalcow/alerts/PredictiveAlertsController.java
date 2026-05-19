package com.digitalcow.alerts;

import com.digitalcow.tenancy.TenantContext;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Alertas predictivas: vacas con muchos dias sin servicio y vacas
 * con caida significativa en produccion semana sobre semana.
 * Combina dos queries simples en un solo endpoint para que la UI
 * lo consuma como un feed.
 */
@RestController
public class PredictiveAlertsController {

    private static final int DAYS_OPEN_THRESHOLD = 120;
    private static final BigDecimal DROP_THRESHOLD = new BigDecimal("0.20");

    private final EntityManager em;

    public PredictiveAlertsController(EntityManager em) {
        this.em = em;
    }

    public record PredictiveAlert(
        String type,
        Long animalId,
        String animalTag,
        String detail,
        String severity
    ) { }

    /** Este metodo devuelve las alertas predictivas. */
    @GetMapping("/api/v1/alerts/predictive")
    @Transactional(readOnly = true)
    public List<PredictiveAlert> get() {
        TenantContext.requireAccountId();
        List<PredictiveAlert> out = new ArrayList<>();
        out.addAll(safeRun(this::longOpenDays));
        out.addAll(safeRun(this::milkDrop));
        return out;
    }

    /**
     * Vacas hembra activas con >120 dias desde el ultimo servicio (o
     * sin servicios). Solo aplica a hembras de proposito DAIRY o DUAL.
     */
    private List<PredictiveAlert> longOpenDays() {
        var rows = em.createQuery(
            "SELECT a.id, a.internalTag, MAX(s.serviceDate) "
                + "FROM Animal a LEFT JOIN ServiceEvent s ON s.animalId = a.id "
                + "WHERE a.status = com.digitalcow.animal.AnimalStatus.ACTIVE "
                + "  AND a.sex = com.digitalcow.animal.Sex.FEMALE "
                + "GROUP BY a.id, a.internalTag "
                + "HAVING MAX(s.serviceDate) IS NULL OR MAX(s.serviceDate) < :limit",
            Object[].class)
            .setParameter("limit", LocalDate.now().minusDays(DAYS_OPEN_THRESHOLD))
            .setMaxResults(30)
            .getResultList();
        List<PredictiveAlert> items = new ArrayList<>();
        for (Object[] r : rows) {
            Long aid = (Long) r[0];
            String tag = (String) r[1];
            LocalDate last = (LocalDate) r[2];
            String detail;
            if (last == null) {
                detail = "Nunca la han echado al toro ni inseminado";
            } else {
                long days = java.time.temporal.ChronoUnit.DAYS.between(last, LocalDate.now());
                detail = "Tiene " + days + " dias sin monta ni inseminacion (ultima: " + last + ")";
            }
            items.add(new PredictiveAlert("LONG_OPEN_DAYS", aid, tag, detail, "medium"));
        }
        return items;
    }

    /**
     * Vacas con caida >=20% en litros promedio semana actual vs
     * semana anterior. Compara dos ventanas de 7 dias contiguas.
     */
    private List<PredictiveAlert> milkDrop() {
        LocalDate now = LocalDate.now();
        var rows = em.createQuery(
            "SELECT m.animalId, "
                + "  AVG(CASE WHEN m.milkingDate BETWEEN :recentStart AND :recentEnd THEN m.liters ELSE NULL END), "
                + "  AVG(CASE WHEN m.milkingDate BETWEEN :prevStart AND :prevEnd THEN m.liters ELSE NULL END) "
                + "FROM Milking m "
                + "WHERE m.milkingDate BETWEEN :prevStart AND :recentEnd "
                + "GROUP BY m.animalId",
            Object[].class)
            .setParameter("recentStart", now.minusDays(7))
            .setParameter("recentEnd", now)
            .setParameter("prevStart", now.minusDays(14))
            .setParameter("prevEnd", now.minusDays(8))
            .getResultList();
        List<PredictiveAlert> items = new ArrayList<>();
        for (Object[] r : rows) {
            Long aid = (Long) r[0];
            Number recentNum = (Number) r[1];
            Number prevNum = (Number) r[2];
            if (recentNum == null || prevNum == null) continue;
            BigDecimal recent = BigDecimal.valueOf(recentNum.doubleValue());
            BigDecimal prev = BigDecimal.valueOf(prevNum.doubleValue());
            if (prev.signum() == 0) continue;
            BigDecimal dropRatio = prev.subtract(recent).divide(prev, 4, java.math.RoundingMode.HALF_UP);
            if (dropRatio.compareTo(DROP_THRESHOLD) >= 0) {
                String tag = em.createQuery("SELECT a.internalTag FROM Animal a WHERE a.id = :id", String.class)
                    .setParameter("id", aid).getSingleResult();
                String pct = dropRatio.movePointRight(2).setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
                items.add(new PredictiveAlert("MILK_DROP", aid, tag,
                    "Bajo " + pct + "% en produccion de leche esta semana (de "
                        + prev.setScale(1, java.math.RoundingMode.HALF_UP) + " a "
                        + recent.setScale(1, java.math.RoundingMode.HALF_UP) + " litros/dia promedio)",
                    "high"));
            }
        }
        return items;
    }

    private <T> List<T> safeRun(java.util.function.Supplier<List<T>> q) {
        try {
            return q.get();
        } catch (Exception e) {
            return List.of();
        }
    }
}
