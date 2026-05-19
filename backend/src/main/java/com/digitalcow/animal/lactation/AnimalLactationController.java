package com.digitalcow.animal.lactation;

import com.digitalcow.tenancy.TenantContext;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Endpoint que devuelve el estado lactacional del animal:
 * fecha del ultimo parto, dias en leche (DEL), si esta seca,
 * y ultimo litraje promedio si esta en ordeño.
 */
@RestController
public class AnimalLactationController {

    private final EntityManager em;

    public AnimalLactationController(EntityManager em) {
        this.em = em;
    }

    public record LactationStatus(
        Long animalId,
        LocalDate lastCalving,
        Integer daysInMilk,
        LocalDate lastDryOff,
        boolean dry,
        BigDecimal recentAvgLiters
    ) { }

    /** Este metodo devuelve la lactancia del animal. */
    @GetMapping("/api/v1/animals/{id}/lactation")
    @Transactional(readOnly = true)
    public LactationStatus get(@PathVariable("id") Long animalId) {
        TenantContext.requireAccountId();
        LocalDate lastCalving = scalarDate(
            "SELECT MAX(c.calvedAt) FROM Calving c WHERE c.animalId = :id", animalId);
        LocalDate lastDryOff = scalarDate(
            "SELECT MAX(d.driedOffAt) FROM DryOff d WHERE d.animalId = :id", animalId);
        boolean dry = lastDryOff != null
            && (lastCalving == null || lastDryOff.isAfter(lastCalving));
        Integer del = (lastCalving != null && !dry)
            ? (int) ChronoUnit.DAYS.between(lastCalving, LocalDate.now())
            : null;
        BigDecimal recent = scalarNumber(
            "SELECT AVG(m.liters) FROM Milking m "
                + "WHERE m.animalId = :id AND m.milkingDate >= :since",
            animalId, LocalDate.now().minusDays(7));
        return new LactationStatus(animalId, lastCalving, del, lastDryOff, dry, recent);
    }

    private LocalDate scalarDate(String jpql, Long id) {
        try {
            return Optional.ofNullable(
                em.createQuery(jpql, LocalDate.class).setParameter("id", id).getSingleResult()
            ).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal scalarNumber(String jpql, Long id, LocalDate since) {
        try {
            Number n = em.createQuery(jpql, Number.class)
                .setParameter("id", id)
                .setParameter("since", since)
                .getSingleResult();
            return n == null ? null : BigDecimal.valueOf(n.doubleValue()).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return null;
        }
    }
}
