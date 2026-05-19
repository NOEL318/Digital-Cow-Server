package com.digitalcow.animal.badges;

import com.digitalcow.tenancy.TenantContext;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Resumen rápido de estado por animal para mostrar chips en la
 * lista. Una query por categoria (vacuna, tratamiento abierto,
 * sin pesar, en celo, preñada, seca). Cada categoria se ejecuta
 * dentro de safeRun para que un fallo en una no rompa el resto.
 */
@RestController
public class AnimalBadgesController {

    private static final int WEIGHING_OVERDUE_DAYS = 45;

    private final EntityManager em;

    public AnimalBadgesController(EntityManager em) {
        this.em = em;
    }

    public record AnimalBadges(Long animalId, List<String> badges) { }

    /** Este metodo devuelve todos los badges del animal. */
    @GetMapping("/api/v1/animals/badges")
    @Transactional(readOnly = true)
    public List<AnimalBadges> all() {
        TenantContext.requireAccountId();
        LocalDate now = LocalDate.now();
        Map<Long, Set<String>> map = new HashMap<>();

        safeRun(() -> addBadges(map, "VACCINE_DUE",
            em.createQuery(
                "SELECT DISTINCT v.animalId FROM Vaccination v "
                    + "WHERE v.nextDoseDue IS NOT NULL AND v.nextDoseDue <= :ref",
                Long.class)
            .setParameter("ref", now)
            .getResultList()));

        safeRun(() -> addBadges(map, "TREATMENT_OPEN",
            em.createQuery(
                "SELECT DISTINCT t.animalId FROM Treatment t WHERE t.endedAt IS NULL",
                Long.class).getResultList()));

        // Sin pesar hace mucho: animales activos SIN un pesaje >= :limit.
        // Reescrito con NOT EXISTS para evitar el HAVING con parametro
        // que Hibernate no podia inferir.
        safeRun(() -> addBadges(map, "WEIGHING_DUE",
            em.createQuery(
                "SELECT a.id FROM Animal a "
                    + "WHERE a.status = com.digitalcow.animal.AnimalStatus.ACTIVE "
                    + "  AND NOT EXISTS ("
                    + "    SELECT 1 FROM Weighing w "
                    + "    WHERE w.animalId = a.id AND w.weighedAt >= :limit"
                    + "  )",
                Long.class)
            .setParameter("limit", now.minusDays(WEIGHING_OVERDUE_DAYS))
            .getResultList()));

        safeRun(() -> addBadges(map, "IN_HEAT",
            em.createQuery(
                "SELECT DISTINCT h.animalId FROM Heat h WHERE h.detectedAt >= :since",
                Long.class)
            .setParameter("since", now.minusDays(5))
            .getResultList()));

        safeRun(() -> addBadges(map, "PREGNANT",
            em.createQuery(
                "SELECT DISTINCT p.animalId FROM PregnancyCheck p "
                    + "WHERE p.result = com.digitalcow.reproduction.pregnancy.PregnancyResult.POSITIVE "
                    + "  AND p.estimatedCalvingDate IS NOT NULL "
                    + "  AND p.estimatedCalvingDate >= :ref",
                Long.class)
            .setParameter("ref", now)
            .getResultList()));

        // Seca: animal con dry-off mas reciente que su ultimo parto.
        // Reescrito con dos queries en Java para evitar comparacion
        // de fechas dentro del JPQL con literal.
        safeRun(() -> {
            List<Long> dryAnimals = em.createQuery(
                "SELECT DISTINCT d.animalId FROM DryOff d", Long.class).getResultList();
            for (Long aid : dryAnimals) {
                LocalDate lastDry = em.createQuery(
                    "SELECT MAX(d.driedOffAt) FROM DryOff d WHERE d.animalId = :aid", LocalDate.class)
                    .setParameter("aid", aid).getSingleResult();
                LocalDate lastCalv = em.createQuery(
                    "SELECT MAX(c.calvedAt) FROM Calving c WHERE c.animalId = :aid", LocalDate.class)
                    .setParameter("aid", aid).getSingleResult();
                if (lastDry != null && (lastCalv == null || lastDry.isAfter(lastCalv))) {
                    map.computeIfAbsent(aid, k -> new java.util.LinkedHashSet<>()).add("DRY");
                }
            }
            return List.<Long>of();
        });

        return map.entrySet().stream()
            .map(e -> new AnimalBadges(e.getKey(), List.copyOf(e.getValue())))
            .toList();
    }

    private List<Long> addBadges(Map<Long, Set<String>> map, String tag, List<Long> ids) {
        for (Long id : ids) {
            if (id == null) continue;
            map.computeIfAbsent(id, k -> new java.util.LinkedHashSet<>()).add(tag);
        }
        return ids;
    }

    private void safeRun(java.util.function.Supplier<List<Long>> q) {
        try { q.get(); } catch (Exception e) { /* swallow */ }
    }
}
