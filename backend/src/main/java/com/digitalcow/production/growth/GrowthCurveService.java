package com.digitalcow.production.growth;

import com.digitalcow.production.weighing.Weighing;
import com.digitalcow.production.weighing.WeighingRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Calcula la curva de crecimiento y el ADG (kg/dia) entre pesajes consecutivos
 * de un animal.
 */
@Service
@Transactional(readOnly = true)
public class GrowthCurveService {

    private final WeighingRepository weighingRepository;

    public GrowthCurveService(WeighingRepository weighingRepository) {
        this.weighingRepository = weighingRepository;
    }

    /** Construye la curva con los pesajes ordenados cronologicamente ascendentes. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public GrowthCurveDto build(Long animalId) {
        List<Weighing> weighings = weighingRepository.findByAnimalIdOrderByWeighedAtAsc(animalId);
        List<GrowthCurveDto.GrowthPoint> points = new ArrayList<>(weighings.size());
        Weighing prev = null;
        for (Weighing w : weighings) {
            BigDecimal adg = null;
            if (prev != null) {
                long days = ChronoUnit.DAYS.between(prev.getWeighedAt(), w.getWeighedAt());
                if (days > 0) {
                    adg = w.getWeightKg().subtract(prev.getWeightKg())
                        .divide(BigDecimal.valueOf(days), 4, RoundingMode.HALF_UP);
                }
            }
            points.add(new GrowthCurveDto.GrowthPoint(w.getWeighedAt(), w.getWeightKg(), adg));
            prev = w;
        }
        return new GrowthCurveDto(animalId, points);
    }
}
