package com.digitalcow.finance.roi;

import com.digitalcow.animal.Animal;
import com.digitalcow.animal.AnimalRepository;
import com.digitalcow.animal.AnimalStatus;
import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.finance.roi.dto.AnimalRoiDto;
import com.digitalcow.tenancy.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * ROI por animal.
 *
 * Estrategia de imputacion de costos:
 *  - treatments: SUM(treatment.cost WHERE animal_id = ?)
 *  - vaccinationsIndividual: SUM(vaccination.cost WHERE animal_id = ? AND lot_id IS NULL)
 *  - vaccinationsProportionalLot: SUM(vaccination.cost WHERE animal_id = ? AND lot_id IS NOT NULL)
 *    (la expansion por lote de Fase 2 ya crea una fila por animal con su cost propio,
 *    por lo que aqui simplemente sumamos lo que corresponde al animal; la division
 *    proporcional ya esta implicita en el modelo)
 *  - services: SUM(service_event.cost WHERE animal_id = ?)
 *  - manualExpenses: SUM(expense.amount WHERE animal_id = ?)
 *  - feedingProportional: por cada feeding_record del lote actual, asignar cost / N_animales_activos
 *    Aproximacion: snapshot historico es Fase futura.
 *
 * Ingreso: SUM(income.amount WHERE animal_id = ?), incluye la venta automatica si existe.
 */
@Service
@Transactional(readOnly = true)
public class AnimalRoiService {

    @PersistenceContext
    private EntityManager em;

    private final AnimalRepository animalRepository;

    public AnimalRoiService(AnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }

    /** Calcula el ROI completo del animal. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public AnimalRoiDto compute(Long animalId) {
        Animal animal = animalRepository.findById(animalId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Animal not found"));

        Long a = TenantContext.requireAccountId();

        BigDecimal treatments = sum(
            "SELECT COALESCE(SUM(cost), 0) FROM treatment WHERE account_id = :a AND animal_id = :id", a, animalId);
        BigDecimal vacInd = sum(
            "SELECT COALESCE(SUM(cost), 0) FROM vaccination "
                + "WHERE account_id = :a AND animal_id = :id AND lot_id IS NULL", a, animalId);
        BigDecimal vacLot = sum(
            "SELECT COALESCE(SUM(cost), 0) FROM vaccination "
                + "WHERE account_id = :a AND animal_id = :id AND lot_id IS NOT NULL", a, animalId);
        BigDecimal services = sum(
            "SELECT COALESCE(SUM(cost), 0) FROM service_event WHERE account_id = :a AND animal_id = :id", a, animalId);
        BigDecimal manual = sum(
            "SELECT COALESCE(SUM(amount), 0) FROM expense WHERE account_id = :a AND animal_id = :id", a, animalId);

        BigDecimal feeding = computeFeedingProportional(animal, a);
        BigDecimal income = sum(
            "SELECT COALESCE(SUM(amount), 0) FROM income WHERE account_id = :a AND animal_id = :id", a, animalId);

        BigDecimal totalCost = treatments.add(vacInd).add(vacLot).add(services).add(manual).add(feeding);
        BigDecimal roi = income.subtract(totalCost);

        return new AnimalRoiDto(
            animalId,
            income,
            totalCost,
            roi,
            new AnimalRoiDto.CostBreakdown(treatments, vacInd, vacLot, services, manual, feeding)
        );
    }

    /**
     * Calcula alimentacion proporcional: feeding_record.cost del lote dividido entre
     * el conteo actual de animales activos del lote. Aproximacion: snapshot historico es Fase futura.
     */
    private BigDecimal computeFeedingProportional(Animal animal, Long accountId) {
        if (animal.getLotId() == null) return BigDecimal.ZERO;
        long activeCount = animalRepository.countByLotIdAndStatus(animal.getLotId(), AnimalStatus.ACTIVE);
        if (activeCount <= 0) return BigDecimal.ZERO;
        BigDecimal totalLotCost = sum(
            "SELECT COALESCE(SUM(cost), 0) FROM feeding_record "
                + "WHERE account_id = :a AND lot_id = :id", accountId, animal.getLotId());
        return totalLotCost.divide(BigDecimal.valueOf(activeCount), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(String sql, Long accountId, Long id) {
        Object o = em.createNativeQuery(sql)
            .setParameter("a", accountId)
            .setParameter("id", id)
            .getSingleResult();
        if (o == null) return BigDecimal.ZERO;
        return o instanceof BigDecimal bd ? bd : new BigDecimal(o.toString());
    }
}
