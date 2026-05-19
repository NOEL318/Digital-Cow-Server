package com.digitalcow.reproduction.kpis;

import com.digitalcow.animal.Animal;
import com.digitalcow.animal.AnimalRepository;
import com.digitalcow.animal.AnimalStatus;
import com.digitalcow.reproduction.calving.Calving;
import com.digitalcow.reproduction.calving.CalvingRepository;
import com.digitalcow.reproduction.kpis.dto.ReproductionKpisDto;
import com.digitalcow.reproduction.pregnancy.PregnancyCheck;
import com.digitalcow.reproduction.pregnancy.PregnancyCheckRepository;
import com.digitalcow.reproduction.pregnancy.PregnancyResult;
import com.digitalcow.reproduction.service.ServiceEvent;
import com.digitalcow.reproduction.service.ServiceEventRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Calculo de KPIs reproductivos agregados del tenant en un periodo.
 * Carga animales activos y sus events en memoria, luego calcula con streams.
 * Para datasets pequenos-medianos es suficiente; en el futuro migrar a SQL nativo.
 */
@Service
@Transactional(readOnly = true)
public class ReproductionKpisService {

    private final AnimalRepository animalRepository;
    private final ServiceEventRepository serviceEventRepository;
    private final CalvingRepository calvingRepository;
    private final PregnancyCheckRepository pregnancyCheckRepository;

    public ReproductionKpisService(AnimalRepository animalRepository,
                                   ServiceEventRepository serviceEventRepository,
                                   CalvingRepository calvingRepository,
                                   PregnancyCheckRepository pregnancyCheckRepository) {
        this.animalRepository = animalRepository;
        this.serviceEventRepository = serviceEventRepository;
        this.calvingRepository = calvingRepository;
        this.pregnancyCheckRepository = pregnancyCheckRepository;
    }

    /** Construye los KPIs del periodo [from, to]. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public ReproductionKpisDto build(LocalDate from, LocalDate to) {
        List<Animal> animals = animalRepository.findAll().stream()
            .filter(a -> a.getStatus() == AnimalStatus.ACTIVE)
            .toList();

        List<Calving> allCalvings = calvingRepository.findAll();
        List<ServiceEvent> allServices = serviceEventRepository.findAll();
        List<PregnancyCheck> allChecks = pregnancyCheckRepository.findAll();

        List<Calving> periodCalvings = allCalvings.stream()
            .filter(c -> inRange(c.getCalvedAt(), from, to))
            .toList();
        List<ServiceEvent> periodServices = allServices.stream()
            .filter(s -> inRange(s.getServiceDate(), from, to))
            .toList();

        Double daysOpenMedian = null;
        Double daysOpenP75 = null;
        Double daysOpenMax = null;
        List<Double> daysOpen = computeDaysOpen(animals, allCalvings, allChecks, to);
        if (!daysOpen.isEmpty()) {
            List<Double> sorted = new ArrayList<>(daysOpen);
            sorted.sort(Comparator.naturalOrder());
            daysOpenMedian = percentile(sorted, 0.5);
            daysOpenP75 = percentile(sorted, 0.75);
            daysOpenMax = sorted.get(sorted.size() - 1);
        }

        Double iepDays = computeIep(allCalvings);
        Double firstCalvingAge = computeFirstCalvingAge(animals, allCalvings);
        Double firstSvcRate = computeFirstServiceConceptionRate(allCalvings, allServices, allChecks);
        Double servicesPerConception = computeServicesPerConception(periodServices, allChecks);
        Double pregnancyRate = computePregnancyRate(periodServices, allChecks);

        return new ReproductionKpisDto(
            from, to,
            daysOpenMedian, daysOpenP75, daysOpenMax,
            iepDays, firstCalvingAge,
            firstSvcRate, servicesPerConception, pregnancyRate
        );
    }

    /**
     * Dias abiertos por vaca: desde la ultima calving hasta la primera concepcion confirmada
     * posterior (primer pregnancy_check POSITIVE), o hasta "to" si no ha concebido.
     */
    private List<Double> computeDaysOpen(List<Animal> animals, List<Calving> allCalvings,
                                         List<PregnancyCheck> allChecks, LocalDate asOf) {
        List<Double> out = new ArrayList<>();
        for (Animal a : animals) {
            Optional<Calving> lastCalving = allCalvings.stream()
                .filter(c -> c.getAnimalId().equals(a.getId()))
                .max(Comparator.comparing(Calving::getCalvedAt));
            if (lastCalving.isEmpty()) continue;
            LocalDate calvedAt = lastCalving.get().getCalvedAt();
            Optional<LocalDate> conception = allChecks.stream()
                .filter(pc -> pc.getAnimalId().equals(a.getId())
                    && pc.getResult() == PregnancyResult.POSITIVE
                    && pc.getCheckedAt().isAfter(calvedAt))
                .map(PregnancyCheck::getCheckedAt)
                .min(Comparator.naturalOrder());
            LocalDate endRef = conception.orElse(asOf);
            long days = ChronoUnit.DAYS.between(calvedAt, endRef);
            if (days >= 0) out.add((double) days);
        }
        return out;
    }

    /** IEP: promedio de (calving[n] - calving[n-1]) por animal con >=2 partos. */
    private Double computeIep(List<Calving> allCalvings) {
        List<Double> diffs = new ArrayList<>();
        var byAnimal = allCalvings.stream()
            .collect(java.util.stream.Collectors.groupingBy(Calving::getAnimalId));
        for (var entry : byAnimal.entrySet()) {
            List<Calving> sorted = new ArrayList<>(entry.getValue());
            sorted.sort(Comparator.comparing(Calving::getCalvedAt));
            for (int i = 1; i < sorted.size(); i++) {
                long days = ChronoUnit.DAYS.between(
                    sorted.get(i - 1).getCalvedAt(), sorted.get(i).getCalvedAt());
                if (days > 0) diffs.add((double) days);
            }
        }
        return diffs.isEmpty() ? null : average(diffs);
    }

    /** Edad al primer parto promedio: (primer calving - birth_date) cuando birth_date no es null. */
    private Double computeFirstCalvingAge(List<Animal> animals, List<Calving> allCalvings) {
        List<Double> ages = new ArrayList<>();
        for (Animal a : animals) {
            if (a.getBirthDate() == null) continue;
            Optional<LocalDate> first = allCalvings.stream()
                .filter(c -> c.getAnimalId().equals(a.getId()))
                .map(Calving::getCalvedAt)
                .min(Comparator.naturalOrder());
            if (first.isEmpty()) continue;
            long days = ChronoUnit.DAYS.between(a.getBirthDate(), first.get());
            if (days > 0) ages.add((double) days);
        }
        return ages.isEmpty() ? null : average(ages);
    }

    /**
     * Tasa concepcion al primer servicio post-parto: del primer servicio que sigue
     * a cada calving, cuantos tienen un pregnancy_check POSITIVE asociado.
     */
    private Double computeFirstServiceConceptionRate(List<Calving> allCalvings,
                                                     List<ServiceEvent> allServices,
                                                     List<PregnancyCheck> allChecks) {
        int total = 0;
        int positive = 0;
        for (Calving c : allCalvings) {
            Optional<ServiceEvent> firstSvc = allServices.stream()
                .filter(s -> s.getAnimalId().equals(c.getAnimalId())
                    && s.getServiceDate().isAfter(c.getCalvedAt()))
                .min(Comparator.comparing(ServiceEvent::getServiceDate));
            if (firstSvc.isEmpty()) continue;
            total++;
            ServiceEvent svc = firstSvc.get();
            boolean conceived = allChecks.stream().anyMatch(pc ->
                pc.getAnimalId().equals(c.getAnimalId())
                && pc.getResult() == PregnancyResult.POSITIVE
                && pc.getCheckedAt().isAfter(svc.getServiceDate())
                && (pc.getServiceId() == null || pc.getServiceId().equals(svc.getId())));
            if (conceived) positive++;
        }
        return total == 0 ? null : (double) positive / total;
    }

    /** Servicios totales del periodo / concepciones confirmadas del periodo. */
    private Double computeServicesPerConception(List<ServiceEvent> periodServices,
                                                List<PregnancyCheck> allChecks) {
        if (periodServices.isEmpty()) return null;
        long conceptions = allChecks.stream()
            .filter(pc -> pc.getResult() == PregnancyResult.POSITIVE)
            .map(PregnancyCheck::getAnimalId)
            .distinct()
            .count();
        if (conceptions == 0) return null;
        return (double) periodServices.size() / conceptions;
    }

    /** Animales servidos en el periodo con check POSITIVE / animales servidos en el periodo. */
    private Double computePregnancyRate(List<ServiceEvent> periodServices,
                                        List<PregnancyCheck> allChecks) {
        var served = periodServices.stream()
            .map(ServiceEvent::getAnimalId)
            .collect(java.util.stream.Collectors.toSet());
        if (served.isEmpty()) return null;
        long pregnant = served.stream().filter(animalId ->
            allChecks.stream().anyMatch(pc ->
                pc.getAnimalId().equals(animalId)
                && pc.getResult() == PregnancyResult.POSITIVE)
        ).count();
        return (double) pregnant / served.size();
    }

    private static boolean inRange(LocalDate d, LocalDate from, LocalDate to) {
        if (d == null) return false;
        return !d.isBefore(from) && !d.isAfter(to);
    }

    /** Percentil tipo linear interpolation; lista debe estar ordenada asc. */
    private static Double percentile(List<Double> sorted, double p) {
        if (sorted.isEmpty()) return null;
        if (sorted.size() == 1) return sorted.get(0);
        double rank = p * (sorted.size() - 1);
        int lo = (int) Math.floor(rank);
        int hi = (int) Math.ceil(rank);
        if (lo == hi) return sorted.get(lo);
        double frac = rank - lo;
        return sorted.get(lo) + frac * (sorted.get(hi) - sorted.get(lo));
    }

    private static double average(List<Double> values) {
        double sum = 0;
        for (Double v : values) sum += v;
        return sum / values.size();
    }
}
