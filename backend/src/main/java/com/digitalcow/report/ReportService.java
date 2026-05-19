package com.digitalcow.report;

import com.digitalcow.animal.Animal;
import com.digitalcow.animal.AnimalRepository;
import com.digitalcow.animal.AnimalStatus;
import com.digitalcow.breed.Breed;
import com.digitalcow.breed.BreedRepository;
import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.finance.animalsale.AnimalSale;
import com.digitalcow.finance.animalsale.AnimalSaleRepository;
import com.digitalcow.finance.income.IncomeRepository;
import com.digitalcow.finance.milksale.MilkSale;
import com.digitalcow.finance.milksale.MilkSaleRepository;
import com.digitalcow.health.diagnosis.Diagnosis;
import com.digitalcow.health.diagnosis.DiagnosisRepository;
import com.digitalcow.health.treatment.Treatment;
import com.digitalcow.health.treatment.TreatmentRepository;
import com.digitalcow.health.vaccination.Vaccination;
import com.digitalcow.health.vaccination.VaccinationRepository;
import com.digitalcow.production.milking.Milking;
import com.digitalcow.production.milking.MilkingRepository;
import com.digitalcow.production.weighing.Weighing;
import com.digitalcow.production.weighing.WeighingRepository;
import com.digitalcow.ranch.Lot;
import com.digitalcow.ranch.LotRepository;
import com.digitalcow.ranch.Ranch;
import com.digitalcow.ranch.RanchRepository;
import com.digitalcow.reproduction.calving.Calving;
import com.digitalcow.reproduction.calving.CalvingRepository;
import com.digitalcow.report.dto.AnimalReportDto;
import com.digitalcow.report.dto.HealthSummaryDto;
import com.digitalcow.report.dto.InventoryReportDto;
import com.digitalcow.report.dto.SalesHistoryDto;
import com.digitalcow.tenancy.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de reportes. Solo SELECT: agrega datos de las fases anteriores y de finanzas
 * para vistas imprimibles y descargas CSV cliente-side.
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

    @PersistenceContext
    private EntityManager em;

    private final AnimalRepository animalRepository;
    private final RanchRepository ranchRepository;
    private final LotRepository lotRepository;
    private final BreedRepository breedRepository;
    private final VaccinationRepository vaccinationRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final TreatmentRepository treatmentRepository;
    private final WeighingRepository weighingRepository;
    private final MilkingRepository milkingRepository;
    private final CalvingRepository calvingRepository;
    private final AnimalSaleRepository animalSaleRepository;
    private final MilkSaleRepository milkSaleRepository;
    private final IncomeRepository incomeRepository;

    public ReportService(AnimalRepository animalRepository,
                         RanchRepository ranchRepository,
                         LotRepository lotRepository,
                         BreedRepository breedRepository,
                         VaccinationRepository vaccinationRepository,
                         DiagnosisRepository diagnosisRepository,
                         TreatmentRepository treatmentRepository,
                         WeighingRepository weighingRepository,
                         MilkingRepository milkingRepository,
                         CalvingRepository calvingRepository,
                         AnimalSaleRepository animalSaleRepository,
                         MilkSaleRepository milkSaleRepository,
                         IncomeRepository incomeRepository) {
        this.animalRepository = animalRepository;
        this.ranchRepository = ranchRepository;
        this.lotRepository = lotRepository;
        this.breedRepository = breedRepository;
        this.vaccinationRepository = vaccinationRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.treatmentRepository = treatmentRepository;
        this.weighingRepository = weighingRepository;
        this.milkingRepository = milkingRepository;
        this.calvingRepository = calvingRepository;
        this.animalSaleRepository = animalSaleRepository;
        this.milkSaleRepository = milkSaleRepository;
        this.incomeRepository = incomeRepository;
    }

    /** Reporte completo de un animal con timeline historico. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public AnimalReportDto animalReport(Long animalId) {
        Animal animal = animalRepository.findById(animalId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Animal not found"));

        Ranch ranch = ranchRepository.findById(animal.getRanchId()).orElse(null);
        Lot lot = animal.getLotId() != null ? lotRepository.findById(animal.getLotId()).orElse(null) : null;
        Breed breed = breedRepository.findById(animal.getBreedId()).orElse(null);

        AnimalReportDto.AnimalSummary summary = new AnimalReportDto.AnimalSummary(
            animal.getId(), animal.getInternalTag(), animal.getOfficialTag(), animal.getName(),
            animal.getSex(), animal.getPurpose(), animal.getStatus(), animal.getBirthDate(),
            animal.getRanchId(), ranch != null ? ranch.getName() : null,
            animal.getLotId(), lot != null ? lot.getName() : null,
            animal.getBreedId(),
            breed != null ? breed.getNameEs() : null,
            breed != null ? breed.getNameEn() : null
        );

        List<AnimalReportDto.VaccinationEntry> vaccinations = vaccinationRepository
            .findByAnimalIdOrderByAppliedAtDesc(animalId).stream()
            .map(this::toVacEntry).toList();
        List<AnimalReportDto.DiagnosisEntry> diagnoses = diagnosisRepository
            .findByAnimalIdOrderByDiagnosedAtDesc(animalId).stream()
            .map(this::toDiagEntry).toList();
        List<AnimalReportDto.TreatmentEntry> treatments = treatmentRepository
            .findByAnimalIdOrderByStartedAtDesc(animalId).stream()
            .map(this::toTreatEntry).toList();
        List<AnimalReportDto.WeighingEntry> weighings = weighingRepository
            .findByAnimalIdOrderByWeighedAtAsc(animalId).stream()
            .map(this::toWeighEntry).toList();
        // Ultimos 30 ordeños como aproximacion de "recientes"
        List<AnimalReportDto.MilkingEntry> recentMilkings = milkingRepository
            .findByAnimalIdOrderByMilkingDateDesc(animalId).stream()
            .limit(30)
            .map(this::toMilkEntry).toList();
        List<AnimalReportDto.CalvingEntry> calvings = calvingRepository
            .findByAnimalIdOrderByCalvedAtDesc(animalId).stream()
            .map(this::toCalvEntry).toList();

        AnimalSale saleEntity = animalSaleRepository.findByAnimalId(animalId).orElse(null);
        AnimalReportDto.AnimalSaleEntry sale = saleEntity == null ? null
            : new AnimalReportDto.AnimalSaleEntry(saleEntity.getId(), saleEntity.getSoldAt(),
                saleEntity.getTotalPrice(), saleEntity.getBuyer());

        BigDecimal totalCost = sumColumnByAnimal("treatment", "cost", animalId)
            .add(sumColumnByAnimal("vaccination", "cost", animalId))
            .add(sumColumnByAnimal("service_event", "cost", animalId))
            .add(sumColumnByAnimalAlt("expense", "amount", animalId));
        BigDecimal totalIncome = scalar(
            "SELECT COALESCE(SUM(amount), 0) FROM income WHERE account_id = :a AND animal_id = :id",
            TenantContext.requireAccountId(), animalId);

        return new AnimalReportDto(summary, vaccinations, diagnoses, treatments,
            weighings, recentMilkings, calvings, sale, totalCost, totalIncome);
    }

    /** Inventario de animales activos. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public InventoryReportDto inventoryReport() {
        List<Animal> animals = animalRepository.findAll().stream()
            .filter(a -> a.getStatus() == AnimalStatus.ACTIVE)
            .toList();

        Map<Long, Ranch> ranchById = new HashMap<>();
        for (Ranch r : ranchRepository.findAll()) ranchById.put(r.getId(), r);
        Map<Long, Lot> lotById = new HashMap<>();
        for (Lot l : lotRepository.findAll()) lotById.put(l.getId(), l);
        Map<Long, Breed> breedById = new HashMap<>();
        for (Breed b : breedRepository.findAll()) breedById.put(b.getId(), b);

        LocalDate today = LocalDate.now();
        List<InventoryReportDto.Row> rows = new ArrayList<>();
        for (Animal a : animals) {
            Long ageDays = a.getBirthDate() != null
                ? ChronoUnit.DAYS.between(a.getBirthDate(), today) : null;
            Ranch r = ranchById.get(a.getRanchId());
            Lot l = a.getLotId() != null ? lotById.get(a.getLotId()) : null;
            Breed b = breedById.get(a.getBreedId());
            BigDecimal lastWeight = lastWeightOf(a.getId());
            rows.add(new InventoryReportDto.Row(
                a.getId(), a.getInternalTag(), a.getOfficialTag(),
                a.getBreedId(), b != null ? b.getNameEs() : null, b != null ? b.getNameEn() : null,
                a.getSex(), a.getPurpose(), ageDays,
                a.getLotId(), l != null ? l.getName() : null,
                a.getRanchId(), r != null ? r.getName() : null,
                lastWeight
            ));
        }
        return new InventoryReportDto(rows.size(), rows);
    }

    /** Historico de ventas (animales y leche) en un periodo. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','VIEWER')")
    public SalesHistoryDto salesHistory(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw BusinessException.badRequest(ErrorCode.VALIDATION_ERROR, "from and to are required");
        }
        List<AnimalSale> animalSales = animalSaleRepository.findBySoldAtBetweenOrderBySoldAtDesc(from, to);
        List<MilkSale> milkSales = milkSaleRepository.findBySaleDateBetweenOrderBySaleDateDesc(from, to);

        BigDecimal totalAnimal = BigDecimal.ZERO;
        BigDecimal totalMilk = BigDecimal.ZERO;
        List<SalesHistoryDto.Row> rows = new ArrayList<>();
        for (AnimalSale s : animalSales) {
            totalAnimal = totalAnimal.add(s.getTotalPrice());
            rows.add(new SalesHistoryDto.Row(
                "ANIMAL_SALE", s.getId(), s.getSoldAt(), s.getAnimalId(),
                "Animal sale", s.getTotalPrice(), s.getCurrency(), s.getBuyer()
            ));
        }
        for (MilkSale m : milkSales) {
            totalMilk = totalMilk.add(m.getTotalPrice());
            rows.add(new SalesHistoryDto.Row(
                "MILK_SALE", m.getId(), m.getSaleDate(), null,
                "Milk sale " + m.getTotalLiters() + "L", m.getTotalPrice(), m.getCurrency(), m.getBuyer()
            ));
        }
        rows.sort((a, b) -> b.date().compareTo(a.date()));
        return new SalesHistoryDto(from, to, totalAnimal, totalMilk, totalAnimal.add(totalMilk), rows);
    }

    /** Resumen sanitario por mes en el periodo. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','VIEWER')")
    @SuppressWarnings("unchecked")
    public HealthSummaryDto healthSummary(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw BusinessException.badRequest(ErrorCode.VALIDATION_ERROR, "from and to are required");
        }
        Long a = TenantContext.requireAccountId();
        Map<String, long[]> counters = new HashMap<>(); // [vac, low, med, high, treat]
        Map<String, BigDecimal> costs = new HashMap<>();

        List<Object[]> vac = em.createNativeQuery(
            "SELECT DATE_FORMAT(applied_at, '%Y-%m'), COUNT(*), COALESCE(SUM(cost),0) "
                + "FROM vaccination WHERE account_id = :a AND applied_at BETWEEN :f AND :t "
                + "GROUP BY DATE_FORMAT(applied_at, '%Y-%m')")
            .setParameter("a", a).setParameter("f", from).setParameter("t", to).getResultList();
        for (Object[] r : vac) {
            String ym = (String) r[0];
            counters.computeIfAbsent(ym, k -> new long[5])[0] += ((Number) r[1]).longValue();
            costs.merge(ym, toBigDecimal(r[2]), BigDecimal::add);
        }
        List<Object[]> diag = em.createNativeQuery(
            "SELECT DATE_FORMAT(diagnosed_at, '%Y-%m'), severity, COUNT(*) "
                + "FROM diagnosis WHERE account_id = :a AND diagnosed_at BETWEEN :f AND :t "
                + "GROUP BY DATE_FORMAT(diagnosed_at, '%Y-%m'), severity")
            .setParameter("a", a).setParameter("f", from).setParameter("t", to).getResultList();
        for (Object[] r : diag) {
            String ym = (String) r[0];
            String sev = (String) r[1];
            long cnt = ((Number) r[2]).longValue();
            long[] arr = counters.computeIfAbsent(ym, k -> new long[5]);
            if ("LOW".equalsIgnoreCase(sev)) arr[1] += cnt;
            else if ("HIGH".equalsIgnoreCase(sev)) arr[3] += cnt;
            else arr[2] += cnt;
        }
        List<Object[]> treat = em.createNativeQuery(
            "SELECT DATE_FORMAT(started_at, '%Y-%m'), COUNT(*), COALESCE(SUM(cost),0) "
                + "FROM treatment WHERE account_id = :a AND started_at BETWEEN :f AND :t "
                + "GROUP BY DATE_FORMAT(started_at, '%Y-%m')")
            .setParameter("a", a).setParameter("f", from).setParameter("t", to).getResultList();
        for (Object[] r : treat) {
            String ym = (String) r[0];
            counters.computeIfAbsent(ym, k -> new long[5])[4] += ((Number) r[1]).longValue();
            costs.merge(ym, toBigDecimal(r[2]), BigDecimal::add);
        }

        List<String> keys = new ArrayList<>(counters.keySet());
        keys.sort(String::compareTo);
        List<HealthSummaryDto.MonthRow> months = new ArrayList<>();
        for (String ym : keys) {
            long[] c = counters.get(ym);
            BigDecimal cost = costs.getOrDefault(ym, BigDecimal.ZERO);
            months.add(new HealthSummaryDto.MonthRow(ym, c[0], c[1], c[2], c[3], c[4], cost));
        }
        return new HealthSummaryDto(from, to, months);
    }

    private AnimalReportDto.VaccinationEntry toVacEntry(Vaccination v) {
        return new AnimalReportDto.VaccinationEntry(
            v.getId(), v.getAppliedAt(), v.getVaccineId(), v.getCost(), v.getNextDoseDue());
    }

    private AnimalReportDto.DiagnosisEntry toDiagEntry(Diagnosis d) {
        return new AnimalReportDto.DiagnosisEntry(
            d.getId(), d.getDiagnosedAt(), d.getDiseaseId(),
            d.getSeverity() != null ? d.getSeverity().name() : null,
            d.getStatus() != null ? d.getStatus().name() : null);
    }

    private AnimalReportDto.TreatmentEntry toTreatEntry(Treatment t) {
        return new AnimalReportDto.TreatmentEntry(
            t.getId(), t.getStartedAt(), t.getEndedAt(), t.getMedicationId(), t.getCost());
    }

    private AnimalReportDto.WeighingEntry toWeighEntry(Weighing w) {
        return new AnimalReportDto.WeighingEntry(w.getId(), w.getWeighedAt(), w.getWeightKg());
    }

    private AnimalReportDto.MilkingEntry toMilkEntry(Milking m) {
        return new AnimalReportDto.MilkingEntry(
            m.getId(), m.getMilkingDate(),
            m.getSession() != null ? m.getSession().name() : null,
            m.getLiters());
    }

    private AnimalReportDto.CalvingEntry toCalvEntry(Calving c) {
        return new AnimalReportDto.CalvingEntry(
            c.getId(), c.getCalvedAt(),
            c.getOutcome() != null ? c.getOutcome().name() : null,
            c.getCalfAnimalId());
    }

    /** Ultimo peso registrado del animal. */
    private BigDecimal lastWeightOf(Long animalId) {
        List<Weighing> list = weighingRepository.findByAnimalIdOrderByWeighedAtDesc(animalId);
        return list.isEmpty() ? null : list.get(0).getWeightKg();
    }

    private BigDecimal sumColumnByAnimal(String table, String column, Long animalId) {
        return scalar("SELECT COALESCE(SUM(" + column + "), 0) FROM " + table
            + " WHERE account_id = :a AND animal_id = :id", TenantContext.requireAccountId(), animalId);
    }

    /** Variante para expense (amount en lugar de cost) — mismo SQL, distinto nombre por claridad. */
    private BigDecimal sumColumnByAnimalAlt(String table, String column, Long animalId) {
        return sumColumnByAnimal(table, column, animalId);
    }

    private BigDecimal scalar(String sql, Long accountId, Long id) {
        Object o = em.createNativeQuery(sql)
            .setParameter("a", accountId)
            .setParameter("id", id)
            .getSingleResult();
        return toBigDecimal(o);
    }

    private static BigDecimal toBigDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        return o instanceof BigDecimal bd ? bd : new BigDecimal(o.toString());
    }
}
