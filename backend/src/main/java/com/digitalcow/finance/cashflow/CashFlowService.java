package com.digitalcow.finance.cashflow;

import com.digitalcow.finance.cashflow.dto.CashFlowDto;
import com.digitalcow.tenancy.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Flujo de caja por mes del año. Suma income + expense (manuales) + costos importados,
 * agrupado por MONTH(fecha). Devuelve 12 meses incluso si algunos son cero.
 */
@Service
@Transactional(readOnly = true)
public class CashFlowService {

    @PersistenceContext
    private EntityManager em;

    /** Construye el flujo de caja del año indicado. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','VIEWER')")
    public CashFlowDto build(int year) {
        Map<Integer, BigDecimal> income = sumByMonth("income", "received_at", year);
        Map<Integer, BigDecimal> expense = sumByMonth("expense", "incurred_at", year);
        addImported(expense, year);

        List<CashFlowDto.MonthFlow> months = new ArrayList<>(12);
        for (int m = 1; m <= 12; m++) {
            BigDecimal inc = income.getOrDefault(m, BigDecimal.ZERO);
            BigDecimal exp = expense.getOrDefault(m, BigDecimal.ZERO);
            months.add(new CashFlowDto.MonthFlow(m, inc, exp, inc.subtract(exp)));
        }
        return new CashFlowDto(year, months);
    }

    /** Acumula los costos importados (treatment, vaccination, etc.) en el mapa de expense. */
    private void addImported(Map<Integer, BigDecimal> expense, int year) {
        addInto(expense, year, "treatment", "started_at", "cost");
        addInto(expense, year, "vaccination", "applied_at", "cost");
        addInto(expense, year, "pest_control", "applied_at", "cost");
        addInto(expense, year, "vet_visit", "visited_at", "total_cost");
        addInto(expense, year, "feeding_record", "consumed_at", "cost");
        addInto(expense, year, "service_event", "service_date", "cost");
    }

    private void addInto(Map<Integer, BigDecimal> acc, int year, String table, String dateCol, String amountCol) {
        for (Map.Entry<Integer, BigDecimal> e : sumByMonth(table, dateCol, amountCol, year).entrySet()) {
            acc.merge(e.getKey(), e.getValue(), BigDecimal::add);
        }
    }

    private Map<Integer, BigDecimal> sumByMonth(String table, String dateCol, int year) {
        return sumByMonth(table, dateCol, "amount", year);
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, BigDecimal> sumByMonth(String table, String dateCol, String amountCol, int year) {
        String sql = "SELECT MONTH(" + dateCol + ") AS m, COALESCE(SUM(" + amountCol + "), 0) "
            + "FROM " + table + " "
            + "WHERE account_id = :a AND YEAR(" + dateCol + ") = :y "
            + "GROUP BY MONTH(" + dateCol + ")";
        List<Object[]> rows = em.createNativeQuery(sql)
            .setParameter("a", TenantContext.requireAccountId())
            .setParameter("y", year)
            .getResultList();
        Map<Integer, BigDecimal> out = new HashMap<>();
        for (Object[] r : rows) {
            int m = ((Number) r[0]).intValue();
            BigDecimal v = r[1] instanceof BigDecimal bd ? bd : new BigDecimal(r[1].toString());
            out.put(m, v);
        }
        return out;
    }
}
