package com.digitalcow.dashboard;

import com.digitalcow.config.CacheConfig;
import com.digitalcow.dashboard.dto.DashboardSummary;
import com.digitalcow.tenancy.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/** Genera el summary del dashboard. Cache 60s por accountId (Caffeine). */
@Service
public class DashboardService {

    @PersistenceContext
    private EntityManager em;

    /** Cache key = accountId del TenantContext. */
    @Cacheable(value = CacheConfig.DASHBOARD_CACHE, key = "T(com.digitalcow.tenancy.TenantContext).get()")
    public DashboardSummary summary() {
        long total = scalar("select count(*) from animal where account_id = :a");
        long active = scalar("select count(*) from animal where account_id = :a and status = 'ACTIVE'");
        int year = LocalDate.now().getYear();
        long sold = scalar("select count(*) from animal where account_id = :a and status = 'SOLD' and year(updated_at) = " + year);
        long dead = scalar("select count(*) from animal where account_id = :a and status = 'DEAD' and year(updated_at) = " + year);
        long ranches = scalar("select count(*) from ranch where account_id = :a");
        long lots = scalar("select count(*) from lot where account_id = :a");

        List<DashboardSummary.ByRanchItem> byRanch = groupByRanch();
        List<DashboardSummary.ByBreedItem> byBreed = groupByBreed();
        Map<String, Long> bySex = groupBySimple("sex");
        Map<String, Long> byPurpose = groupBySimple("purpose");
        DashboardSummary.RecentAdditions recent = recentAdditions();

        return new DashboardSummary(
            new DashboardSummary.Totals(total, active, sold, dead, ranches, lots),
            byRanch, byBreed, bySex, byPurpose, recent
        );
    }

    private long scalar(String sql) {
        Object o = em.createNativeQuery(sql).setParameter("a", TenantContext.get()).getSingleResult();
        return ((Number) o).longValue();
    }

    @SuppressWarnings("unchecked")
    private List<DashboardSummary.ByRanchItem> groupByRanch() {
        List<Object[]> rows = em.createNativeQuery(
            "select r.id, r.name, count(a.id) from ranch r " +
            "left join animal a on a.ranch_id = r.id and a.account_id = :acc " +
            "where r.account_id = :acc group by r.id, r.name")
            .setParameter("acc", TenantContext.get()).getResultList();
        List<DashboardSummary.ByRanchItem> out = new ArrayList<>();
        for (Object[] r : rows) out.add(new DashboardSummary.ByRanchItem(
            ((Number) r[0]).longValue(), (String) r[1], ((Number) r[2]).longValue()));
        return out;
    }

    @SuppressWarnings("unchecked")
    private List<DashboardSummary.ByBreedItem> groupByBreed() {
        List<Object[]> rows = em.createNativeQuery(
            "select b.id, b.code, count(a.id) from breed b " +
            "left join animal a on a.breed_id = b.id and a.account_id = :acc " +
            "group by b.id, b.code")
            .setParameter("acc", TenantContext.get()).getResultList();
        List<DashboardSummary.ByBreedItem> out = new ArrayList<>();
        for (Object[] r : rows) out.add(new DashboardSummary.ByBreedItem(
            ((Number) r[0]).longValue(), (String) r[1], ((Number) r[2]).longValue()));
        return out;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Long> groupBySimple(String column) {
        List<Object[]> rows = em.createNativeQuery(
            "select " + column + ", count(*) from animal where account_id = :a group by " + column)
            .setParameter("a", TenantContext.get()).getResultList();
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] r : rows) map.put(String.valueOf(r[0]), ((Number) r[1]).longValue());
        return map;
    }

    @SuppressWarnings("unchecked")
    private DashboardSummary.RecentAdditions recentAdditions() {
        List<Object[]> rows = em.createNativeQuery(
            "select date(created_at), count(*) from animal " +
            "where account_id = :a and created_at >= (now() - interval 30 day) " +
            "group by date(created_at) order by 1")
            .setParameter("a", TenantContext.get()).getResultList();
        List<String> labels = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        for (Object[] r : rows) {
            labels.add(r[0].toString());
            counts.add(((Number) r[1]).longValue());
        }
        return new DashboardSummary.RecentAdditions(labels, counts);
    }
}
