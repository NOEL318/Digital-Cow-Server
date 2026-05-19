package com.digitalcow.reproduction.alerts;

import com.digitalcow.animal.AnimalRepository;
import com.digitalcow.reproduction.alerts.dto.AlertItemDto;
import com.digitalcow.reproduction.alerts.dto.ReproductionAlertsDto;
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

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Calculo de alertas reproductivas del tenant. Cacheable por accountId.
 * Se invalida cuando cambian heats, services, pregnancy checks o calvings.
 */
@Service
@Transactional(readOnly = true)
public class ReproductionAlertsService {

    @PersistenceContext
    private EntityManager em;

    private final AnimalRepository animalRepository;

    public ReproductionAlertsService(AnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }

    /** Construye el set de alertas para el tenant activo. */
    @Cacheable(value = "reproduction-alerts", keyGenerator = "tenantKeyGenerator")
    public ReproductionAlertsDto build() {
        LocalDate today = LocalDate.now();
        LocalDate in21 = today.plusDays(21);

        List<AlertItemDto> upcoming = upcomingCalvings(today, in21);
        List<AlertItemDto> dryOff = dryOffDue(today);
        List<AlertItemDto> served = servedWithoutCheck(today);
        List<AlertItemDto> open = openTooLong(today);

        return new ReproductionAlertsDto(upcoming, dryOff, served, open);
    }

    /** Este metodo invalida el cache de alertas cuando cambia un celo. */
    @EventListener
    @CacheEvict(value = "reproduction-alerts", allEntries = true)
    public void onHeat(HeatChangedEvent event) { /* invalidate */ }

    /** Este metodo invalida el cache de alertas cuando cambia un servicio reproductivo. */
    @EventListener
    @CacheEvict(value = "reproduction-alerts", allEntries = true)
    public void onService(ServiceEventChangedEvent event) { /* invalidate */ }

    /** Este metodo invalida el cache de alertas cuando cambia un chequeo de prenez. */
    @EventListener
    @CacheEvict(value = "reproduction-alerts", allEntries = true)
    public void onPregnancyCheck(PregnancyCheckChangedEvent event) { /* invalidate */ }

    /** Este metodo invalida el cache de alertas cuando cambia un parto. */
    @EventListener
    @CacheEvict(value = "reproduction-alerts", allEntries = true)
    public void onCalving(CalvingChangedEvent event) { /* invalidate */ }

    /** Pregnancy checks POSITIVE con estimated_calving_date entre hoy y hoy+21. */
    @SuppressWarnings("unchecked")
    private List<AlertItemDto> upcomingCalvings(LocalDate today, LocalDate in21) {
        String sql = "SELECT pc.id, pc.animal_id, pc.estimated_calving_date "
            + "FROM pregnancy_check pc "
            + "WHERE pc.account_id = :a AND pc.result = 'POSITIVE' "
            + "AND pc.estimated_calving_date BETWEEN :from AND :to "
            + "ORDER BY pc.estimated_calving_date";
        List<Object[]> rows = em.createNativeQuery(sql)
            .setParameter("a", TenantContext.get())
            .setParameter("from", today)
            .setParameter("to", in21)
            .getResultList();
        List<AlertItemDto> out = new ArrayList<>();
        for (Object[] r : rows) {
            Long id = ((Number) r[0]).longValue();
            Long animalId = ((Number) r[1]).longValue();
            LocalDate date = toLocalDate(r[2]);
            out.add(new AlertItemDto("UPCOMING_CALVING", animalId, animalTag(animalId),
                "Parto estimado", date, id));
        }
        return out;
    }

    /**
     * Vacas DAIRY con ultima calving entre today-305 y today-280 sin dry_off posterior.
     * Heuristica: estan en el rango de lactancia donde corresponde secar.
     */
    @SuppressWarnings("unchecked")
    private List<AlertItemDto> dryOffDue(LocalDate today) {
        LocalDate from = today.minusDays(305);
        LocalDate to = today.minusDays(280);
        String sql = "SELECT c.id, c.animal_id, c.calved_at "
            + "FROM calving c "
            + "JOIN animal a ON a.id = c.animal_id "
            + "WHERE c.account_id = :acc AND a.account_id = :acc "
            + "AND a.purpose = 'DAIRY' "
            + "AND c.calved_at BETWEEN :from AND :to "
            + "AND NOT EXISTS ( "
            + "  SELECT 1 FROM dry_off d WHERE d.account_id = :acc "
            + "  AND d.animal_id = c.animal_id AND d.dried_off_at > c.calved_at "
            + ") "
            + "ORDER BY c.calved_at";
        List<Object[]> rows = em.createNativeQuery(sql)
            .setParameter("acc", TenantContext.get())
            .setParameter("from", from)
            .setParameter("to", to)
            .getResultList();
        List<AlertItemDto> out = new ArrayList<>();
        for (Object[] r : rows) {
            Long id = ((Number) r[0]).longValue();
            Long animalId = ((Number) r[1]).longValue();
            LocalDate date = toLocalDate(r[2]);
            out.add(new AlertItemDto("DRY_OFF_DUE", animalId, animalTag(animalId),
                "Pendiente de secado", date, id));
        }
        return out;
    }

    /** Servicios con mas de 40 dias sin un pregnancy_check posterior. */
    @SuppressWarnings("unchecked")
    private List<AlertItemDto> servedWithoutCheck(LocalDate today) {
        LocalDate cutoff = today.minusDays(40);
        String sql = "SELECT s.id, s.animal_id, s.service_date "
            + "FROM service_event s "
            + "WHERE s.account_id = :acc AND s.service_date <= :cutoff "
            + "AND NOT EXISTS ( "
            + "  SELECT 1 FROM pregnancy_check pc WHERE pc.account_id = :acc "
            + "  AND pc.animal_id = s.animal_id AND pc.checked_at >= s.service_date "
            + ") "
            + "ORDER BY s.service_date";
        List<Object[]> rows = em.createNativeQuery(sql)
            .setParameter("acc", TenantContext.get())
            .setParameter("cutoff", cutoff)
            .getResultList();
        List<AlertItemDto> out = new ArrayList<>();
        for (Object[] r : rows) {
            Long id = ((Number) r[0]).longValue();
            Long animalId = ((Number) r[1]).longValue();
            LocalDate date = toLocalDate(r[2]);
            out.add(new AlertItemDto("SERVED_WITHOUT_CHECK", animalId, animalTag(animalId),
                "Servida sin diagnostico", date, id));
        }
        return out;
    }

    /**
     * Vacas con ultima calving hace mas de 120 dias y sin pregnancy_check POSITIVE posterior.
     * Aproximacion de "open days" superando el umbral.
     */
    @SuppressWarnings("unchecked")
    private List<AlertItemDto> openTooLong(LocalDate today) {
        LocalDate cutoff = today.minusDays(120);
        String sql = "SELECT lc.calving_id, lc.animal_id, lc.last_calved_at "
            + "FROM ( "
            + "  SELECT c.id AS calving_id, c.animal_id, c.calved_at AS last_calved_at, "
            + "    ROW_NUMBER() OVER (PARTITION BY c.animal_id ORDER BY c.calved_at DESC) AS rn "
            + "  FROM calving c WHERE c.account_id = :acc "
            + ") lc "
            + "WHERE lc.rn = 1 AND lc.last_calved_at <= :cutoff "
            + "AND NOT EXISTS ( "
            + "  SELECT 1 FROM pregnancy_check pc WHERE pc.account_id = :acc "
            + "  AND pc.animal_id = lc.animal_id AND pc.result = 'POSITIVE' "
            + "  AND pc.checked_at > lc.last_calved_at "
            + ") "
            + "ORDER BY lc.last_calved_at";
        List<Object[]> rows = em.createNativeQuery(sql)
            .setParameter("acc", TenantContext.get())
            .setParameter("cutoff", cutoff)
            .getResultList();
        List<AlertItemDto> out = new ArrayList<>();
        for (Object[] r : rows) {
            Long id = ((Number) r[0]).longValue();
            Long animalId = ((Number) r[1]).longValue();
            LocalDate date = toLocalDate(r[2]);
            out.add(new AlertItemDto("OPEN_TOO_LONG", animalId, animalTag(animalId),
                "Vacia mas de 120 dias", date, id));
        }
        return out;
    }

    private String animalTag(Long animalId) {
        if (animalId == null) return "";
        return animalRepository.findById(animalId).map(a -> a.getInternalTag()).orElse("");
    }

    private LocalDate toLocalDate(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDate ld) return ld;
        if (o instanceof Date d) return d.toLocalDate();
        return LocalDate.parse(o.toString());
    }
}
