package com.digitalcow.animal.share;

import com.digitalcow.animal.Animal;
import com.digitalcow.animal.AnimalRepository;
import com.digitalcow.breed.Breed;
import com.digitalcow.breed.BreedRepository;
import com.digitalcow.catalog.vaccine.Vaccine;
import com.digitalcow.catalog.vaccine.VaccineRepository;
import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.health.vaccination.Vaccination;
import com.digitalcow.health.vaccination.VaccinationRepository;
import com.digitalcow.photo.AnimalPhotoRepository;
import com.digitalcow.production.milking.MilkingRepository;
import com.digitalcow.production.weighing.WeighingRepository;
import com.digitalcow.reproduction.calving.Calving;
import com.digitalcow.reproduction.calving.CalvingRepository;
import com.digitalcow.reproduction.dryoff.DryOff;
import com.digitalcow.reproduction.dryoff.DryOffRepository;
import com.digitalcow.tenancy.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Servicio que resuelve un share_token a una vista publica del animal.
 *
 * Diseno: el endpoint que invoca este servicio es publico (sin JWT,
 * sin TenantContext), por lo que el filtro Hibernate accountFilter
 * no esta activo y las queries cargan filas de cualquier tenant. La
 * autorizacion se basa unicamente en conocer el token opaco. El
 * servicio NUNCA acepta un id de animal directamente, solo el token,
 * y devuelve un DTO que omite ranchId, lotId, notas y todo dato
 * financiero. La superficie de informacion expuesta se limita a
 * estadisticas de manejo (peso, vacunas, ordeño) y datos de
 * identificacion.
 */
@Service
public class PublicAnimalShareService {

    private static final int RECENT_LIMIT = 30;
    private static final int DRY_OFF_GRACE_DAYS = 0;

    private final AnimalRepository animals;
    private final BreedRepository breeds;
    private final AnimalPhotoRepository photos;
    private final WeighingRepository weighings;
    private final VaccinationRepository vaccinations;
    private final VaccineRepository vaccines;
    private final MilkingRepository milkings;
    private final CalvingRepository calvings;
    private final DryOffRepository dryOffs;

    @PersistenceContext
    private EntityManager em;

    public PublicAnimalShareService(AnimalRepository animals,
                                    BreedRepository breeds,
                                    AnimalPhotoRepository photos,
                                    WeighingRepository weighings,
                                    VaccinationRepository vaccinations,
                                    VaccineRepository vaccines,
                                    MilkingRepository milkings,
                                    CalvingRepository calvings,
                                    DryOffRepository dryOffs) {
        this.animals = animals;
        this.breeds = breeds;
        this.photos = photos;
        this.weighings = weighings;
        this.vaccinations = vaccinations;
        this.vaccines = vaccines;
        this.milkings = milkings;
        this.calvings = calvings;
        this.dryOffs = dryOffs;
    }

    /**
     * Resuelve un token publico a la vista de solo lectura del animal.
     *
     * @param token identificador opaco del share
     * @return vista publica con identificacion e historial resumido
     */
    @Transactional(readOnly = true)
    public PublicAnimalShareResponse resolve(String token) {
        // Si el solicitante esta autenticado en otro tenant, el filtro
        // accountFilter ocultaria el animal compartido. Lo desactivamos
        // explicitamente para que el token, no el tenant, decida acceso.
        Long savedTenant = TenantContext.get();
        Session session = em.unwrap(Session.class);
        session.disableFilter("accountFilter");
        try {
            TenantContext.clear();
            return resolveInternal(token);
        } finally {
            if (savedTenant != null) {
                TenantContext.set(savedTenant);
                session.enableFilter("accountFilter").setParameter("accountId", savedTenant);
            }
        }
    }

    private PublicAnimalShareResponse resolveInternal(String token) {
        Animal a = animals.findByShareToken(token)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Share not found"));

        String breedName = breeds.findById(a.getBreedId()).map(Breed::getNameEs).orElse(null);

        String coverUrl = null;
        if (a.getCoverPhotoId() != null) {
            for (var p : photos.findUrlByIdIn(List.of(a.getCoverPhotoId()))) {
                coverUrl = p.getUrl();
            }
        }

        List<PublicAnimalShareResponse.WeighingPoint> wpts = weighings
            .findByAnimalIdOrderByWeighedAtDesc(a.getId()).stream()
            .limit(RECENT_LIMIT)
            .map(w -> new PublicAnimalShareResponse.WeighingPoint(w.getWeighedAt(), w.getWeightKg()))
            .toList();

        List<Vaccination> recentVax = vaccinations.findByAnimalIdOrderByAppliedAtDesc(a.getId())
            .stream().limit(RECENT_LIMIT).toList();
        List<PublicAnimalShareResponse.VaccinationPoint> vpts = recentVax.stream()
            .map(v -> {
                String name = vaccines.findById(v.getVaccineId()).map(Vaccine::getNameEs).orElse(null);
                return new PublicAnimalShareResponse.VaccinationPoint(v.getAppliedAt(), name);
            })
            .toList();

        List<PublicAnimalShareResponse.MilkingPoint> mpts = milkings
            .findByAnimalIdOrderByMilkingDateDesc(a.getId()).stream()
            .limit(RECENT_LIMIT)
            .map(m -> new PublicAnimalShareResponse.MilkingPoint(m.getMilkingDate(), m.getLiters()))
            .toList();

        LocalDate lastCalving = calvings.findByAnimalIdOrderByCalvedAtDesc(a.getId()).stream()
            .map(Calving::getCalvedAt)
            .findFirst()
            .orElse(null);
        Optional<DryOff> lastDryOff = dryOffs.findByAnimalIdOrderByDriedOffAtDesc(a.getId()).stream()
            .findFirst();
        Integer dim = computeDaysInMilk(lastCalving, lastDryOff.map(DryOff::getDriedOffAt).orElse(null));
        Integer ageMonths = a.getBirthDate() == null
            ? null
            : Period.between(a.getBirthDate(), LocalDate.now()).getYears() * 12
                + Period.between(a.getBirthDate(), LocalDate.now()).getMonths();

        return new PublicAnimalShareResponse(
            a.getInternalTag(),
            a.getName(),
            a.getSex(),
            a.getStatus(),
            a.getPurpose(),
            a.getBirthDate(),
            a.isBirthDateEstimated(),
            ageMonths,
            breedName,
            coverUrl,
            dim,
            lastCalving,
            sortAscByWeighedAt(wpts),
            sortAscByAppliedAt(vpts),
            sortAscByMilkingDate(mpts)
        );
    }

    private static Integer computeDaysInMilk(LocalDate lastCalving, LocalDate lastDryOff) {
        if (lastCalving == null) return null;
        LocalDate end = lastDryOff != null && lastDryOff.isAfter(lastCalving)
            ? lastDryOff.minusDays(DRY_OFF_GRACE_DAYS)
            : LocalDate.now();
        return Math.toIntExact(java.time.temporal.ChronoUnit.DAYS.between(lastCalving, end));
    }

    private static List<PublicAnimalShareResponse.WeighingPoint> sortAscByWeighedAt(List<PublicAnimalShareResponse.WeighingPoint> in) {
        return in.stream()
            .sorted(Comparator.comparing(PublicAnimalShareResponse.WeighingPoint::weighedAt))
            .toList();
    }

    private static List<PublicAnimalShareResponse.VaccinationPoint> sortAscByAppliedAt(List<PublicAnimalShareResponse.VaccinationPoint> in) {
        return in.stream()
            .sorted(Comparator.comparing(PublicAnimalShareResponse.VaccinationPoint::appliedAt))
            .toList();
    }

    private static List<PublicAnimalShareResponse.MilkingPoint> sortAscByMilkingDate(List<PublicAnimalShareResponse.MilkingPoint> in) {
        return in.stream()
            .sorted(Comparator.comparing(PublicAnimalShareResponse.MilkingPoint::milkingDate))
            .toList();
    }
}
