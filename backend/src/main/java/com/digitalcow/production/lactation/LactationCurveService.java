package com.digitalcow.production.lactation;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.production.milking.Milking;
import com.digitalcow.production.milking.MilkingRepository;
import com.digitalcow.reproduction.calving.CalvingRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calcula la curva de lactancia agregando litros por dia desde el inicio de lactancia
 * del animal. Si no se pasa lactationStartDate, se usa el ultimo Calving registrado.
 */
@Service
@Transactional(readOnly = true)
public class LactationCurveService {

    private final MilkingRepository milkingRepository;
    private final CalvingRepository calvingRepository;

    public LactationCurveService(MilkingRepository milkingRepository,
                                 CalvingRepository calvingRepository) {
        this.milkingRepository = milkingRepository;
        this.calvingRepository = calvingRepository;
    }

    /** Construye la curva. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public LactationCurveDto build(Long animalId, LocalDate lactationStartDate) {
        LocalDate start = lactationStartDate;
        if (start == null) {
            start = calvingRepository.findFirstByAnimalIdOrderByCalvedAtDesc(animalId)
                .map(c -> c.getCalvedAt())
                .orElseThrow(() -> BusinessException.badRequest(ErrorCode.VALIDATION_ERROR,
                    "lactationStartDate not provided and no calving found for animal"));
        }

        List<Milking> milkings = milkingRepository
            .findByAnimalIdAndMilkingDateGreaterThanEqualOrderByMilkingDateAsc(animalId, start);

        Map<LocalDate, BigDecimal> byDay = new LinkedHashMap<>();
        for (Milking m : milkings) {
            byDay.merge(m.getMilkingDate(), m.getLiters(), BigDecimal::add);
        }

        List<LactationCurveDto.LactationPoint> points = new ArrayList<>(byDay.size());
        for (Map.Entry<LocalDate, BigDecimal> e : byDay.entrySet()) {
            int dol = (int) ChronoUnit.DAYS.between(start, e.getKey()) + 1;
            points.add(new LactationCurveDto.LactationPoint(dol, e.getKey(), e.getValue()));
        }
        return new LactationCurveDto(animalId, start, points);
    }
}
