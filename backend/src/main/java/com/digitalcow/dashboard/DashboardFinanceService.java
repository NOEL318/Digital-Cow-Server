package com.digitalcow.dashboard;

import com.digitalcow.dashboard.dto.DashboardFinanceDto;
import com.digitalcow.finance.animalsale.event.AnimalSaleChangedEvent;
import com.digitalcow.finance.expense.event.ExpenseChangedEvent;
import com.digitalcow.finance.income.event.IncomeChangedEvent;
import com.digitalcow.finance.milksale.event.MilkSaleChangedEvent;
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
 * Servicio del widget de finanzas del dashboard.
 * Queries agregadas con native SQL, cacheable por tenant.
 * Egreso = expense manual + treatment.cost + vaccination.cost + pest_control.cost
 *          + vet_visit.total_cost + feeding_record.cost + service_event.cost.
 */
@Service
@Transactional(readOnly = true)
public class DashboardFinanceService {

    @PersistenceContext
    private EntityManager em;

    /** Construye el resumen financiero para el tenant activo. */
    @Cacheable(value = "dashboard-finance", keyGenerator = "tenantKeyGenerator")
    public DashboardFinanceDto build() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate yearStart = today.withDayOfYear(1);

        BigDecimal mtdIncome = sumIncome(monthStart, today);
        BigDecimal mtdExpense = sumExpenseComposite(monthStart, today);
        BigDecimal mtdMargin = mtdIncome.subtract(mtdExpense);

        BigDecimal ytdIncome = sumIncome(yearStart, today);
        BigDecimal ytdExpense = sumExpenseComposite(yearStart, today);
        BigDecimal ytdMargin = ytdIncome.subtract(ytdExpense);

        List<DashboardFinanceDto.TopCategoryDto> top = topExpenseCategories(monthStart, today);

        return new DashboardFinanceDto(mtdIncome, mtdExpense, mtdMargin, ytdMargin, top);
    }

    /** Invalida el cache al cambiar gastos, ingresos o ventas. */
    @EventListener
    @CacheEvict(value = "dashboard-finance", allEntries = true)
    public void onExpense(ExpenseChangedEvent event) { /* invalidate */ }

    /** Este metodo invalida el cache cuando cambia un ingreso. */
    @EventListener
    @CacheEvict(value = "dashboard-finance", allEntries = true)
    public void onIncome(IncomeChangedEvent event) { /* invalidate */ }

    /** Este metodo invalida el cache cuando cambia una venta de animal. */
    @EventListener
    @CacheEvict(value = "dashboard-finance", allEntries = true)
    public void onAnimalSale(AnimalSaleChangedEvent event) { /* invalidate */ }

    /** Este metodo invalida el cache cuando cambia una venta de leche. */
    @EventListener
    @CacheEvict(value = "dashboard-finance", allEntries = true)
    public void onMilkSale(MilkSaleChangedEvent event) { /* invalidate */ }

    private BigDecimal sumIncome(LocalDate from, LocalDate to) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM income "
            + "WHERE account_id = :a AND received_at BETWEEN :f AND :t";
        return scalar(sql, from, to);
    }

    /** Suma compuesta: expense manual + costos importados de Fases 2-4. */
    private BigDecimal sumExpenseComposite(LocalDate from, LocalDate to) {
        String sql = "SELECT COALESCE(SUM(c), 0) FROM ( "
            + "  SELECT amount AS c FROM expense "
            + "    WHERE account_id = :a AND incurred_at BETWEEN :f AND :t "
            + "  UNION ALL SELECT COALESCE(cost, 0) FROM treatment "
            + "    WHERE account_id = :a AND started_at BETWEEN :f AND :t "
            + "  UNION ALL SELECT COALESCE(cost, 0) FROM vaccination "
            + "    WHERE account_id = :a AND applied_at BETWEEN :f AND :t "
            + "  UNION ALL SELECT COALESCE(cost, 0) FROM pest_control "
            + "    WHERE account_id = :a AND applied_at BETWEEN :f AND :t "
            + "  UNION ALL SELECT COALESCE(total_cost, 0) FROM vet_visit "
            + "    WHERE account_id = :a AND visited_at BETWEEN :f AND :t "
            + "  UNION ALL SELECT COALESCE(cost, 0) FROM feeding_record "
            + "    WHERE account_id = :a AND consumed_at BETWEEN :f AND :t "
            + "  UNION ALL SELECT COALESCE(cost, 0) FROM service_event "
            + "    WHERE account_id = :a AND service_date BETWEEN :f AND :t "
            + ") sub";
        return scalar(sql, from, to);
    }

    /** Top 3 categorias del mes por monto. */
    @SuppressWarnings("unchecked")
    private List<DashboardFinanceDto.TopCategoryDto> topExpenseCategories(LocalDate from, LocalDate to) {
        String sql = "SELECT c.code, c.name_es, c.name_en, COALESCE(SUM(e.amount), 0) AS total "
            + "FROM expense e LEFT JOIN expense_category c ON c.id = e.expense_category_id "
            + "WHERE e.account_id = :a AND e.incurred_at BETWEEN :f AND :t "
            + "GROUP BY c.code, c.name_es, c.name_en ORDER BY total DESC LIMIT 3";
        List<Object[]> rows = em.createNativeQuery(sql)
            .setParameter("a", TenantContext.requireAccountId())
            .setParameter("f", from)
            .setParameter("t", to)
            .getResultList();
        List<DashboardFinanceDto.TopCategoryDto> out = new ArrayList<>();
        for (Object[] r : rows) {
            BigDecimal total = r[3] instanceof BigDecimal bd ? bd : new BigDecimal(r[3].toString());
            out.add(new DashboardFinanceDto.TopCategoryDto(
                (String) r[0],
                (String) r[1],
                (String) r[2],
                total
            ));
        }
        return out;
    }

    private BigDecimal scalar(String sql, LocalDate from, LocalDate to) {
        Object o = em.createNativeQuery(sql)
            .setParameter("a", TenantContext.requireAccountId())
            .setParameter("f", from)
            .setParameter("t", to)
            .getSingleResult();
        if (o == null) return BigDecimal.ZERO;
        return o instanceof BigDecimal bd ? bd : new BigDecimal(o.toString());
    }
}
