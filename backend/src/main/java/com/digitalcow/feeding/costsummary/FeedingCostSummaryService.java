package com.digitalcow.feeding.costsummary;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.feeding.costsummary.dto.FeedingCostSummaryDto;
import com.digitalcow.tenancy.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Resumen de costos de alimentacion. groupBy:
 *  - "lot": agrupado por lot.id (label = lot.name)
 *  - "ranch": agrupado por lot.ranch_id (label = ranch.name)
 *  - "month": agrupado por YYYY-MM (label = YYYY-MM)
 */
@Service
@Transactional(readOnly = true)
public class FeedingCostSummaryService {

    @PersistenceContext
    private EntityManager em;

    /** Construye el resumen. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public FeedingCostSummaryDto build(LocalDate from, LocalDate to, String groupBy) {
        String gb = groupBy == null ? "lot" : groupBy.toLowerCase();
        List<FeedingCostSummaryDto.CostBucket> buckets = switch (gb) {
            case "lot" -> groupByLot(from, to);
            case "ranch" -> groupByRanch(from, to);
            case "month" -> groupByMonth(from, to);
            default -> throw BusinessException.badRequest(ErrorCode.VALIDATION_ERROR,
                "groupBy must be lot|ranch|month");
        };
        return new FeedingCostSummaryDto(from, to, gb, buckets);
    }

    @SuppressWarnings("unchecked")
    private List<FeedingCostSummaryDto.CostBucket> groupByLot(LocalDate from, LocalDate to) {
        String sql = "SELECT l.id, l.name, COALESCE(SUM(fr.cost),0) AS c, COALESCE(SUM(fr.total_kg),0) AS k "
            + "FROM feeding_record fr JOIN lot l ON l.id = fr.lot_id "
            + "WHERE fr.account_id = :a AND fr.consumed_at BETWEEN :f AND :t "
            + "GROUP BY l.id, l.name ORDER BY c DESC";
        List<Object[]> rows = em.createNativeQuery(sql)
            .setParameter("a", TenantContext.get())
            .setParameter("f", from)
            .setParameter("t", to)
            .getResultList();
        List<FeedingCostSummaryDto.CostBucket> out = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            String key = String.valueOf(((Number) r[0]).longValue());
            String label = (String) r[1];
            BigDecimal cost = asBigDecimal(r[2]);
            BigDecimal kg = asBigDecimal(r[3]);
            out.add(new FeedingCostSummaryDto.CostBucket(key, label, cost, kg));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private List<FeedingCostSummaryDto.CostBucket> groupByRanch(LocalDate from, LocalDate to) {
        String sql = "SELECT r.id, r.name, COALESCE(SUM(fr.cost),0) AS c, COALESCE(SUM(fr.total_kg),0) AS k "
            + "FROM feeding_record fr "
            + "JOIN lot l ON l.id = fr.lot_id "
            + "JOIN ranch r ON r.id = l.ranch_id "
            + "WHERE fr.account_id = :a AND fr.consumed_at BETWEEN :f AND :t "
            + "GROUP BY r.id, r.name ORDER BY c DESC";
        List<Object[]> rows = em.createNativeQuery(sql)
            .setParameter("a", TenantContext.get())
            .setParameter("f", from)
            .setParameter("t", to)
            .getResultList();
        List<FeedingCostSummaryDto.CostBucket> out = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            String key = String.valueOf(((Number) r[0]).longValue());
            String label = (String) r[1];
            BigDecimal cost = asBigDecimal(r[2]);
            BigDecimal kg = asBigDecimal(r[3]);
            out.add(new FeedingCostSummaryDto.CostBucket(key, label, cost, kg));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private List<FeedingCostSummaryDto.CostBucket> groupByMonth(LocalDate from, LocalDate to) {
        String sql = "SELECT DATE_FORMAT(consumed_at, '%Y-%m') AS m, "
            + "COALESCE(SUM(cost),0) AS c, COALESCE(SUM(total_kg),0) AS k "
            + "FROM feeding_record "
            + "WHERE account_id = :a AND consumed_at BETWEEN :f AND :t "
            + "GROUP BY m ORDER BY m ASC";
        List<Object[]> rows = em.createNativeQuery(sql)
            .setParameter("a", TenantContext.get())
            .setParameter("f", from)
            .setParameter("t", to)
            .getResultList();
        List<FeedingCostSummaryDto.CostBucket> out = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            String key = (String) r[0];
            BigDecimal cost = asBigDecimal(r[1]);
            BigDecimal kg = asBigDecimal(r[2]);
            out.add(new FeedingCostSummaryDto.CostBucket(key, key, cost, kg));
        }
        return out;
    }

    private static BigDecimal asBigDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        return o instanceof BigDecimal bd ? bd : new BigDecimal(o.toString());
    }
}
