package com.digitalcow.finance.costunit;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.finance.costunit.dto.CostPerUnitDto;
import com.digitalcow.tenancy.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Costo por unidad producida.
 *
 * DAIRY: divide los costos del periodo asociados a animales DAIRY/DUAL entre la suma de
 * litros producidos (milking.liters). Costos asociados = treatment + vaccination + service
 * + manual expense ligados a esos animales, mas feeding_record de lotes que contienen
 * al menos un animal DAIRY/DUAL (aproximacion).
 *
 * BEEF: divide los costos del periodo asociados a animales BEEF/DUAL entre la suma de kg
 * ganados (diferencia entre primer y ultimo weighing por animal beef activo en el periodo).
 *
 * Simplificacion: si no hay datos suficientes, totalUnits = 0 y costPerUnit = null.
 */
@Service
@Transactional(readOnly = true)
public class CostPerUnitService {

    @PersistenceContext
    private EntityManager em;

    /** Construye el costo por unidad del periodo segun proposito. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','VIEWER')")
    public CostPerUnitDto build(LocalDate from, LocalDate to, String purpose) {
        if (from == null || to == null) {
            throw BusinessException.badRequest(ErrorCode.VALIDATION_ERROR, "from and to are required");
        }
        if (purpose == null) {
            throw BusinessException.badRequest(ErrorCode.VALIDATION_ERROR, "purpose is required");
        }
        String p = purpose.toUpperCase();
        if (!p.equals("BEEF") && !p.equals("DAIRY")) {
            throw BusinessException.badRequest(ErrorCode.VALIDATION_ERROR, "purpose must be BEEF or DAIRY");
        }

        Long a = TenantContext.requireAccountId();
        BigDecimal totalCost = sumCostsForPurpose(a, from, to, p);
        BigDecimal totalUnits = p.equals("DAIRY")
            ? sumLiters(a, from, to)
            : sumBeefKgGain(a, from, to);

        BigDecimal costPerUnit = totalUnits.signum() > 0
            ? totalCost.divide(totalUnits, 4, RoundingMode.HALF_UP)
            : null;

        return new CostPerUnitDto(from, to, p, totalCost, totalUnits, costPerUnit);
    }

    /**
     * Suma costos del periodo de animales con el proposito dado.
     * Incluye treatment/vaccination/service/manual expense ligados a esos animales,
     * mas feeding_record de lotes con al menos un animal del proposito (aproximacion simple).
     */
    private BigDecimal sumCostsForPurpose(Long a, LocalDate from, LocalDate to, String purpose) {
        String purposeList = purpose.equals("DAIRY") ? "'DAIRY','DUAL'" : "'BEEF','DUAL'";
        BigDecimal treat = scalar(
            "SELECT COALESCE(SUM(t.cost), 0) FROM treatment t "
                + "JOIN animal an ON an.id = t.animal_id "
                + "WHERE t.account_id = :a AND t.started_at BETWEEN :f AND :t "
                + "AND an.purpose IN (" + purposeList + ")", a, from, to);
        BigDecimal vac = scalar(
            "SELECT COALESCE(SUM(v.cost), 0) FROM vaccination v "
                + "JOIN animal an ON an.id = v.animal_id "
                + "WHERE v.account_id = :a AND v.applied_at BETWEEN :f AND :t "
                + "AND an.purpose IN (" + purposeList + ")", a, from, to);
        BigDecimal svc = scalar(
            "SELECT COALESCE(SUM(s.cost), 0) FROM service_event s "
                + "JOIN animal an ON an.id = s.animal_id "
                + "WHERE s.account_id = :a AND s.service_date BETWEEN :f AND :t "
                + "AND an.purpose IN (" + purposeList + ")", a, from, to);
        BigDecimal manual = scalar(
            "SELECT COALESCE(SUM(e.amount), 0) FROM expense e "
                + "JOIN animal an ON an.id = e.animal_id "
                + "WHERE e.account_id = :a AND e.incurred_at BETWEEN :f AND :t "
                + "AND an.purpose IN (" + purposeList + ")", a, from, to);
        BigDecimal feeding = scalar(
            "SELECT COALESCE(SUM(fr.cost), 0) FROM feeding_record fr "
                + "WHERE fr.account_id = :a AND fr.consumed_at BETWEEN :f AND :t "
                + "AND EXISTS (SELECT 1 FROM animal an "
                + "  WHERE an.lot_id = fr.lot_id AND an.purpose IN (" + purposeList + "))",
            a, from, to);
        return treat.add(vac).add(svc).add(manual).add(feeding);
    }

    private BigDecimal sumLiters(Long a, LocalDate from, LocalDate to) {
        return scalar(
            "SELECT COALESCE(SUM(liters), 0) FROM milking "
                + "WHERE account_id = :a AND milking_date BETWEEN :f AND :t", a, from, to);
    }

    /**
     * Suma de kg ganados por animales BEEF/DUAL activos en el periodo.
     * Por cada animal, kg_ganados = MAX(weight_kg) - MIN(weight_kg) en el periodo (>=0).
     */
    @SuppressWarnings("unchecked")
    private BigDecimal sumBeefKgGain(Long a, LocalDate from, LocalDate to) {
        String sql = "SELECT w.animal_id, MIN(w.weight_kg) AS min_w, MAX(w.weight_kg) AS max_w "
            + "FROM weighing w JOIN animal an ON an.id = w.animal_id "
            + "WHERE w.account_id = :a AND w.weighed_at BETWEEN :f AND :t "
            + "AND an.status = 'ACTIVE' AND an.purpose IN ('BEEF','DUAL') "
            + "GROUP BY w.animal_id HAVING COUNT(*) >= 2";
        List<Object[]> rows = em.createNativeQuery(sql)
            .setParameter("a", a)
            .setParameter("f", from)
            .setParameter("t", to)
            .getResultList();
        BigDecimal total = BigDecimal.ZERO;
        for (Object[] r : rows) {
            BigDecimal min = toBigDecimal(r[1]);
            BigDecimal max = toBigDecimal(r[2]);
            BigDecimal diff = max.subtract(min);
            if (diff.signum() > 0) total = total.add(diff);
        }
        return total;
    }

    private BigDecimal scalar(String sql, Long a, LocalDate from, LocalDate to) {
        Object o = em.createNativeQuery(sql)
            .setParameter("a", a)
            .setParameter("f", from)
            .setParameter("t", to)
            .getSingleResult();
        if (o == null) return BigDecimal.ZERO;
        return o instanceof BigDecimal bd ? bd : new BigDecimal(o.toString());
    }

    private static BigDecimal toBigDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        return o instanceof BigDecimal bd ? bd : new BigDecimal(o.toString());
    }
}
