package com.digitalcow.agenda;

import com.digitalcow.tenancy.TenantContext;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Construye el feed "Hoy" del rancho. No crea entidades nuevas: lee
 * de los modulos existentes (vacunas con proxima dosis vencida,
 * chequeos de prenez pendientes, partos esperados, animales sin
 * pesar hace mucho) y los normaliza como AgendaItem.
 *
 * Usa queries JPQL nativas con safeRun (cualquier fallo se silencia
 * para que un modulo con datos faltantes no rompa toda la agenda).
 */
@Service
public class AgendaService {

    private static final int WEIGHING_OVERDUE_DAYS = 45;

    private final EntityManager em;

    public AgendaService(EntityManager em) {
        this.em = em;
    }

    /** Este metodo devuelve la agenda con los eventos del rancho para los proximos siete dias. */
    @Transactional(readOnly = true)
    public List<AgendaItem> today() {
        // Aseguramos que el filtro de tenant este aplicado.
        TenantContext.requireAccountId();
        LocalDate now = LocalDate.now();
        LocalDate horizon = now.plusDays(7);

        List<AgendaItem> out = new ArrayList<>();
        out.addAll(safeRun(() -> upcomingVaccinations(now, horizon)));
        out.addAll(safeRun(() -> upcomingCalvings(now, horizon)));
        out.addAll(safeRun(() -> overdueWeighings(now)));
        out.addAll(safeRun(() -> overdueTreatments(now)));

        out.sort(Comparator.comparing(AgendaItem::dueDate,
            Comparator.nullsLast(Comparator.naturalOrder())));
        return out;
    }

    private List<AgendaItem> upcomingVaccinations(LocalDate now, LocalDate horizon) {
        var rows = em.createQuery(
            "SELECT v.animalId, a.internalTag, a.lotId, v.nextDoseDue "
                + "FROM Vaccination v JOIN Animal a ON a.id = v.animalId "
                + "WHERE v.nextDoseDue IS NOT NULL "
                + "  AND v.nextDoseDue BETWEEN :pastLimit AND :horizon "
                + "  AND a.status = com.digitalcow.animal.AnimalStatus.ACTIVE",
            Object[].class)
            .setParameter("pastLimit", now.minusDays(60))
            .setParameter("horizon", horizon)
            .getResultList();
        List<AgendaItem> items = new ArrayList<>();
        for (Object[] r : rows) {
            Long aid = (Long) r[0];
            String tag = (String) r[1];
            Long lotId = (Long) r[2];
            LocalDate due = (LocalDate) r[3];
            String severity = due.isBefore(now) ? "high" : (due.isBefore(now.plusDays(3)) ? "medium" : "low");
            String msg;
            if (due.isBefore(now)) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(due, now);
                msg = "Vacuna atrasada " + days + " dias";
            } else if (due.equals(now)) {
                msg = "Hay que vacunar hoy";
            } else {
                long days = java.time.temporal.ChronoUnit.DAYS.between(now, due);
                msg = "Vacuna en " + days + " dias";
            }
            items.add(new AgendaItem("VACCINATION", aid, tag, lotId, null, due, msg, severity));
        }
        return items;
    }

    private List<AgendaItem> upcomingCalvings(LocalDate now, LocalDate horizon) {
        var rows = em.createQuery(
            "SELECT p.animalId, a.internalTag, p.estimatedCalvingDate "
                + "FROM PregnancyCheck p JOIN Animal a ON a.id = p.animalId "
                + "WHERE p.result = com.digitalcow.reproduction.pregnancy.PregnancyResult.POSITIVE "
                + "  AND p.estimatedCalvingDate IS NOT NULL "
                + "  AND p.estimatedCalvingDate BETWEEN :now AND :horizon",
            Object[].class)
            .setParameter("now", now)
            .setParameter("horizon", horizon.plusDays(14))
            .getResultList();
        List<AgendaItem> items = new ArrayList<>();
        for (Object[] r : rows) {
            Long aid = (Long) r[0];
            String tag = (String) r[1];
            LocalDate due = (LocalDate) r[2];
            String severity = due.isBefore(now.plusDays(7)) ? "high" : "medium";
            long days = java.time.temporal.ChronoUnit.DAYS.between(now, due);
            String msg = days <= 0 ? "Va a parir cualquier dia"
                       : days == 1 ? "Pare manana"
                       : "Va a parir en " + days + " dias";
            items.add(new AgendaItem("CALVING", aid, tag, null, null, due, msg, severity));
        }
        return items;
    }

    private List<AgendaItem> overdueWeighings(LocalDate now) {
        var rows = em.createQuery(
            "SELECT a.id, a.internalTag, a.lotId, MAX(w.weighedAt) "
                + "FROM Animal a LEFT JOIN Weighing w ON w.animalId = a.id "
                + "WHERE a.status = com.digitalcow.animal.AnimalStatus.ACTIVE "
                + "GROUP BY a.id, a.internalTag, a.lotId "
                + "HAVING MAX(w.weighedAt) IS NULL OR MAX(w.weighedAt) < :limit",
            Object[].class)
            .setParameter("limit", now.minusDays(WEIGHING_OVERDUE_DAYS))
            .setMaxResults(20)
            .getResultList();
        List<AgendaItem> items = new ArrayList<>();
        for (Object[] r : rows) {
            Long aid = (Long) r[0];
            String tag = (String) r[1];
            Long lotId = (Long) r[2];
            LocalDate last = (LocalDate) r[3];
            String msg;
            if (last == null) {
                msg = "Nunca la han pesado";
            } else {
                long days = java.time.temporal.ChronoUnit.DAYS.between(last, now);
                msg = "Tiene " + days + " dias sin pesarse";
            }
            items.add(new AgendaItem("WEIGHING_OVERDUE", aid, tag, lotId, null, now, msg, "medium"));
        }
        return items;
    }

    private List<AgendaItem> overdueTreatments(LocalDate now) {
        var rows = em.createQuery(
            "SELECT t.animalId, a.internalTag, t.startedAt, t.endedAt "
                + "FROM Treatment t JOIN Animal a ON a.id = t.animalId "
                + "WHERE t.endedAt IS NULL AND t.startedAt < :limit",
            Object[].class)
            .setParameter("limit", now.minusDays(14))
            .setMaxResults(20)
            .getResultList();
        List<AgendaItem> items = new ArrayList<>();
        for (Object[] r : rows) {
            Long aid = (Long) r[0];
            String tag = (String) r[1];
            LocalDate started = (LocalDate) r[2];
            long days = java.time.temporal.ChronoUnit.DAYS.between(started, java.time.LocalDate.now());
            String msg = "Tratamiento abierto hace " + days + " dias";
            items.add(new AgendaItem("TREATMENT_OPEN", aid, tag, null, null, started, msg, "medium"));
        }
        return items;
    }

    @SuppressWarnings("unchecked")
    private List<AgendaItem> safeRun(java.util.function.Supplier<List<AgendaItem>> q) {
        try {
            return q.get();
        } catch (Exception e) {
            return List.of();
        }
    }
}
