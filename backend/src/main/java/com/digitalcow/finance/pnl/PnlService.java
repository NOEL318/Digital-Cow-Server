package com.digitalcow.finance.pnl;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.finance.pnl.dto.PnlDto;
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
 * P&L. Suma ingresos (income.amount) y egresos compuestos:
 * expense.amount + treatment.cost + vaccination.cost + pest_control.cost
 * + vet_visit.total_cost + feeding_record.cost + service_event.cost.
 *
 * El agrupamiento es:
 *   - "month": DATE_FORMAT(fecha, '%Y-%m') sobre union de income+expense+costos
 *   - "category": JOIN con expense_category sobre los expense; los costos importados se reportan
 *     como un bucket adicional "imported".
 */
@Service
@Transactional(readOnly = true)
public class PnlService {

    @PersistenceContext
    private EntityManager em;

    /** Construye el P&L para el periodo. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','VIEWER')")
    public PnlDto build(LocalDate from, LocalDate to, String groupBy) {
        if (from == null || to == null) {
            throw BusinessException.badRequest(ErrorCode.VALIDATION_ERROR, "from and to are required");
        }
        String g = groupBy == null ? "month" : groupBy.toLowerCase();
        if (!g.equals("month") && !g.equals("category")) {
            throw BusinessException.badRequest(ErrorCode.VALIDATION_ERROR, "groupBy must be month or category");
        }

        BigDecimal totalIncome = sumIncome(from, to);
        PnlDto.BreakdownDto breakdown = importedBreakdown(from, to);
        BigDecimal totalExpenseManual = sumExpense(from, to);
        BigDecimal totalExpense = totalExpenseManual
            .add(nz(breakdown.treatments()))
            .add(nz(breakdown.vaccinations()))
            .add(nz(breakdown.pestControls()))
            .add(nz(breakdown.vetVisits()))
            .add(nz(breakdown.feedingRecords()))
            .add(nz(breakdown.services()));
        BigDecimal margin = totalIncome.subtract(totalExpense);

        List<PnlDto.PnlBucket> buckets = g.equals("month")
            ? bucketsByMonth(from, to)
            : bucketsByCategory(from, to, breakdown);

        return new PnlDto(from, to, g, totalIncome, totalExpense, margin, buckets, breakdown);
    }

    private BigDecimal sumIncome(LocalDate from, LocalDate to) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM income "
            + "WHERE account_id = :a AND received_at BETWEEN :f AND :t";
        return asBigDecimal(em.createNativeQuery(sql)
            .setParameter("a", TenantContext.requireAccountId())
            .setParameter("f", from)
            .setParameter("t", to)
            .getSingleResult());
    }

    private BigDecimal sumExpense(LocalDate from, LocalDate to) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM expense "
            + "WHERE account_id = :a AND incurred_at BETWEEN :f AND :t";
        return asBigDecimal(em.createNativeQuery(sql)
            .setParameter("a", TenantContext.requireAccountId())
            .setParameter("f", from)
            .setParameter("t", to)
            .getSingleResult());
    }

    /** Desglose de costos importados (no estan en expense; vienen de las fases 2-4). */
    private PnlDto.BreakdownDto importedBreakdown(LocalDate from, LocalDate to) {
        Long a = TenantContext.requireAccountId();
        BigDecimal treat = sumColumn("SELECT COALESCE(SUM(cost), 0) FROM treatment "
            + "WHERE account_id = :a AND started_at BETWEEN :f AND :t", a, from, to);
        BigDecimal vac = sumColumn("SELECT COALESCE(SUM(cost), 0) FROM vaccination "
            + "WHERE account_id = :a AND applied_at BETWEEN :f AND :t", a, from, to);
        BigDecimal pest = sumColumn("SELECT COALESCE(SUM(cost), 0) FROM pest_control "
            + "WHERE account_id = :a AND applied_at BETWEEN :f AND :t", a, from, to);
        BigDecimal vet = sumColumn("SELECT COALESCE(SUM(total_cost), 0) FROM vet_visit "
            + "WHERE account_id = :a AND visited_at BETWEEN :f AND :t", a, from, to);
        BigDecimal feed = sumColumn("SELECT COALESCE(SUM(cost), 0) FROM feeding_record "
            + "WHERE account_id = :a AND consumed_at BETWEEN :f AND :t", a, from, to);
        BigDecimal svc = sumColumn("SELECT COALESCE(SUM(cost), 0) FROM service_event "
            + "WHERE account_id = :a AND service_date BETWEEN :f AND :t", a, from, to);
        return new PnlDto.BreakdownDto(treat, vac, pest, vet, feed, svc);
    }

    /** Buckets mensuales: union de income + expense + costos importados agregados por '%Y-%m'. */
    @SuppressWarnings("unchecked")
    private List<PnlDto.PnlBucket> bucketsByMonth(LocalDate from, LocalDate to) {
        Long a = TenantContext.requireAccountId();
        String sql = "SELECT ym, SUM(inc) AS income_sum, SUM(exp) AS expense_sum FROM ( "
            + "  SELECT DATE_FORMAT(received_at, '%Y-%m') AS ym, amount AS inc, 0 AS exp "
            + "    FROM income WHERE account_id = :a AND received_at BETWEEN :f AND :t "
            + "  UNION ALL "
            + "  SELECT DATE_FORMAT(incurred_at, '%Y-%m') AS ym, 0 AS inc, amount AS exp "
            + "    FROM expense WHERE account_id = :a AND incurred_at BETWEEN :f AND :t "
            + "  UNION ALL "
            + "  SELECT DATE_FORMAT(started_at, '%Y-%m'), 0, COALESCE(cost,0) "
            + "    FROM treatment WHERE account_id = :a AND started_at BETWEEN :f AND :t "
            + "  UNION ALL "
            + "  SELECT DATE_FORMAT(applied_at, '%Y-%m'), 0, COALESCE(cost,0) "
            + "    FROM vaccination WHERE account_id = :a AND applied_at BETWEEN :f AND :t "
            + "  UNION ALL "
            + "  SELECT DATE_FORMAT(applied_at, '%Y-%m'), 0, COALESCE(cost,0) "
            + "    FROM pest_control WHERE account_id = :a AND applied_at BETWEEN :f AND :t "
            + "  UNION ALL "
            + "  SELECT DATE_FORMAT(visited_at, '%Y-%m'), 0, COALESCE(total_cost,0) "
            + "    FROM vet_visit WHERE account_id = :a AND visited_at BETWEEN :f AND :t "
            + "  UNION ALL "
            + "  SELECT DATE_FORMAT(consumed_at, '%Y-%m'), 0, COALESCE(cost,0) "
            + "    FROM feeding_record WHERE account_id = :a AND consumed_at BETWEEN :f AND :t "
            + "  UNION ALL "
            + "  SELECT DATE_FORMAT(service_date, '%Y-%m'), 0, COALESCE(cost,0) "
            + "    FROM service_event WHERE account_id = :a AND service_date BETWEEN :f AND :t "
            + ") u GROUP BY ym ORDER BY ym";
        List<Object[]> rows = em.createNativeQuery(sql)
            .setParameter("a", a)
            .setParameter("f", from)
            .setParameter("t", to)
            .getResultList();
        List<PnlDto.PnlBucket> out = new ArrayList<>();
        for (Object[] r : rows) {
            String ym = (String) r[0];
            BigDecimal inc = toBigDecimal(r[1]);
            BigDecimal exp = toBigDecimal(r[2]);
            out.add(new PnlDto.PnlBucket(ym, ym, inc, exp, inc.subtract(exp)));
        }
        return out;
    }

    /**
     * Buckets por categoria. Cada expense_category produce un bucket con su gasto.
     * Los costos importados se agrupan en un bucket especial "imported".
     */
    @SuppressWarnings("unchecked")
    private List<PnlDto.PnlBucket> bucketsByCategory(LocalDate from, LocalDate to, PnlDto.BreakdownDto br) {
        Long a = TenantContext.requireAccountId();
        String sql = "SELECT c.code, c.name_es, COALESCE(SUM(e.amount), 0) "
            + "FROM expense e "
            + "LEFT JOIN expense_category c ON c.id = e.expense_category_id "
            + "WHERE e.account_id = :a AND e.incurred_at BETWEEN :f AND :t "
            + "GROUP BY c.code, c.name_es ORDER BY SUM(e.amount) DESC";
        List<Object[]> rows = em.createNativeQuery(sql)
            .setParameter("a", a)
            .setParameter("f", from)
            .setParameter("t", to)
            .getResultList();
        List<PnlDto.PnlBucket> out = new ArrayList<>();
        for (Object[] r : rows) {
            String code = r[0] == null ? "UNKNOWN" : (String) r[0];
            String name = r[1] == null ? code : (String) r[1];
            BigDecimal exp = toBigDecimal(r[2]);
            out.add(new PnlDto.PnlBucket(code, name, BigDecimal.ZERO, exp, exp.negate()));
        }
        BigDecimal imported = nz(br.treatments())
            .add(nz(br.vaccinations()))
            .add(nz(br.pestControls()))
            .add(nz(br.vetVisits()))
            .add(nz(br.feedingRecords()))
            .add(nz(br.services()));
        if (imported.signum() != 0) {
            out.add(new PnlDto.PnlBucket("IMPORTED", "Costos importados", BigDecimal.ZERO, imported, imported.negate()));
        }
        return out;
    }

    private BigDecimal sumColumn(String sql, Long a, LocalDate f, LocalDate t) {
        return asBigDecimal(em.createNativeQuery(sql)
            .setParameter("a", a)
            .setParameter("f", f)
            .setParameter("t", t)
            .getSingleResult());
    }

    private static BigDecimal asBigDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        return o instanceof BigDecimal bd ? bd : new BigDecimal(o.toString());
    }

    private static BigDecimal toBigDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        return o instanceof BigDecimal bd ? bd : new BigDecimal(o.toString());
    }

    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
}
