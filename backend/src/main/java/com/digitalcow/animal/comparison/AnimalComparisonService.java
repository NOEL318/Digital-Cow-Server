package com.digitalcow.animal.comparison;

import com.digitalcow.animal.Animal;
import com.digitalcow.animal.AnimalRepository;
import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calcula la serie mensual comparativa por animal. Hace hasta cuatro
 * queries agregadas (peso, alimento del lote actual, gasto, ingreso)
 * usando funciones JPA estandar YEAR y MONTH (portables entre bases
 * de datos), y las une en un solo payload normalizado por mes. No
 * materializa vistas; el costo por consulta es bajo para los rangos
 * tipicos (12 a 24 meses).
 */
@Service
public class AnimalComparisonService {

    private final EntityManager em;
    private final AnimalRepository animalRepository;

    public AnimalComparisonService(EntityManager em, AnimalRepository animalRepository) {
        this.em = em;
        this.animalRepository = animalRepository;
    }

    /** Este metodo calcula la comparacion de animales. */
    @Transactional(readOnly = true)
    public AnimalComparisonResponse compute(Long animalId, LocalDate from, LocalDate to) {
        Animal animal = animalRepository.findById(animalId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Animal not found"));

        Map<String, BigDecimal> weight = safeRun(() -> aggregate(
            "SELECT YEAR(w.weighedAt), MONTH(w.weighedAt), AVG(w.weightKg) "
                + "FROM Weighing w "
                + "WHERE w.animalId = :animalId AND w.weighedAt BETWEEN :from AND :to "
                + "GROUP BY YEAR(w.weighedAt), MONTH(w.weighedAt)",
            Map.of("animalId", animalId, "from", from, "to", to)
        ));
        Map<String, BigDecimal> expense = safeRun(() -> aggregate(
            "SELECT YEAR(e.incurredAt), MONTH(e.incurredAt), SUM(e.amount) "
                + "FROM Expense e "
                + "WHERE e.animalId = :animalId AND e.incurredAt BETWEEN :from AND :to "
                + "GROUP BY YEAR(e.incurredAt), MONTH(e.incurredAt)",
            Map.of("animalId", animalId, "from", from, "to", to)
        ));
        Map<String, BigDecimal> income = safeRun(() -> aggregate(
            "SELECT YEAR(i.receivedAt), MONTH(i.receivedAt), SUM(i.amount) "
                + "FROM Income i "
                + "WHERE i.animalId = :animalId AND i.receivedAt BETWEEN :from AND :to "
                + "GROUP BY YEAR(i.receivedAt), MONTH(i.receivedAt)",
            Map.of("animalId", animalId, "from", from, "to", to)
        ));
        Map<String, BigDecimal> feed = animal.getLotId() == null
            ? Map.of()
            : safeRun(() -> aggregate(
                "SELECT YEAR(f.consumedAt), MONTH(f.consumedAt), SUM(f.totalKg) "
                    + "FROM FeedingRecord f "
                    + "WHERE f.lotId = :lotId AND f.consumedAt BETWEEN :from AND :to "
                    + "GROUP BY YEAR(f.consumedAt), MONTH(f.consumedAt)",
                Map.of("lotId", animal.getLotId(), "from", from, "to", to)
            ));

        List<AnimalComparisonResponse.MonthPoint> points = new ArrayList<>();
        YearMonth start = YearMonth.from(from);
        YearMonth end = YearMonth.from(to);
        for (YearMonth ym = start; !ym.isAfter(end); ym = ym.plusMonths(1)) {
            String key = ym.toString();
            points.add(new AnimalComparisonResponse.MonthPoint(
                key,
                weight.get(key),
                feed.get(key),
                expense.get(key),
                income.get(key)
            ));
        }
        return new AnimalComparisonResponse(animalId, from, to, points);
    }

    /**
     * Ejecuta una query JPQL agregada cuyo resultado son tres columnas:
     * year (Integer), month (Integer), value (Number). Devuelve un mapa
     * con clave "YYYY-MM".
     */
    private Map<String, BigDecimal> aggregate(String jpql, Map<String, Object> params) {
        var query = em.createQuery(jpql, Object[].class);
        params.forEach(query::setParameter);
        Map<String, BigDecimal> out = new HashMap<>();
        for (Object[] row : query.getResultList()) {
            if (row == null || row[0] == null || row[1] == null || row[2] == null) continue;
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            Number raw = (Number) row[2];
            BigDecimal value = raw instanceof BigDecimal bd
                ? bd
                : BigDecimal.valueOf(raw.doubleValue());
            String key = String.format("%04d-%02d", year, month);
            out.put(key, value.setScale(2, RoundingMode.HALF_UP));
        }
        return out;
    }

    /**
     * Cualquier fallo de query (por ejemplo si una entidad no esta
     * cargada en el contexto de tenant aun) devuelve un mapa vacio
     * en vez de propagar el error. La grafica simplemente no muestra
     * esa serie.
     */
    private Map<String, BigDecimal> safeRun(java.util.function.Supplier<Map<String, BigDecimal>> q) {
        try {
            return q.get();
        } catch (Exception e) {
            return Map.of();
        }
    }
}
