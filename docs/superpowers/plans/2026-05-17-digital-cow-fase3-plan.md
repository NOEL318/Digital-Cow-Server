# Digital Cow Fase 3 — Implementation Plan (Reproducción)

> **For agentic workers:** REQUIRED SUB-SKILL: subagent-driven-development. Checkbox `- [ ]` tracking. NO git commits. NO emojis. NO ASCII art. Javadoc/TSDoc obligatorio en publics.

**Goal:** Módulo de Reproducción: catálogo de toros y pajillas de semen, eventos reproductivos (heat, service, pregnancy check, calving, abortion, weaning, dry-off), genealogía en animal, alertas, KPIs reproductivos.

**Architecture:** Mismo monolito Spring Boot. Nuevo paquete `com.digitalcow.reproduction`. Frontend: nuevas features bajo `frontend/src/features/reproduction/`. Migraciones V9 (extensión animal) y V10 (catálogos + eventos).

**Spec:** `docs/superpowers/specs/2026-05-17-digital-cow-fase3-design.md`.

---

## Patrón backend (referencia)

Mismo que Fase 2: por feature `entity + repo + service + controller + dto/ + mapper + event/`. Multi-tenant con `TenantAwareEntityListener` + `@Filter("accountFilter")`. Validación con `@Valid` en controllers. `@PreAuthorize` por rol en services. Eventos vía `ApplicationEventPublisher` para invalidación de caches.

## Patrón frontend (referencia)

Mismo que Fase 2: `features/<x>/api.ts` (TanStack Query), `schemas.ts` (zod), `types.ts`, `components/`, página en `pages/<x>/<X>Page.tsx`.

---

## Épica A — Migraciones

### Task 1: Migración V9 extensión animal (genealogía + birth_weight)

**Files:**
- Create: `backend/src/main/resources/db/migration/V9__animal_genealogy.sql`

- [ ] **Step 1:**

```sql
ALTER TABLE animal
  ADD COLUMN sire_id BIGINT NULL AFTER cover_photo_id,
  ADD COLUMN external_sire_name VARCHAR(160) NULL AFTER sire_id,
  ADD COLUMN dam_id BIGINT NULL AFTER external_sire_name,
  ADD COLUMN birth_weight_kg DECIMAL(5,2) NULL AFTER dam_id,
  ADD CONSTRAINT fk_animal_sire FOREIGN KEY (sire_id) REFERENCES animal(id),
  ADD CONSTRAINT fk_animal_dam FOREIGN KEY (dam_id) REFERENCES animal(id),
  ADD INDEX ix_animal_sire (account_id, sire_id),
  ADD INDEX ix_animal_dam (account_id, dam_id);
```

- [ ] **Step 2:** Modificar `backend/src/main/java/com/digitalcow/animal/Animal.java` agregando los 4 campos:

```java
@Column(name = "sire_id")
private Long sireId;

@Column(name = "external_sire_name", length = 160)
private String externalSireName;

@Column(name = "dam_id")
private Long damId;

@Column(name = "birth_weight_kg", precision = 5, scale = 2)
private java.math.BigDecimal birthWeightKg;
```

Y agregar los mismos a `AnimalResponseDto`, `AnimalCreateDto`, `AnimalUpdateDto`, y el `AnimalMapper` MapStruct (los mappers heredados por convención mapean por nombre, pero validar y agregar si faltan).

- [ ] **Step 3: Pausa de revisión.**

---

### Task 2: Migración V10 catálogos reproductivos + eventos

**Files:**
- Create: `backend/src/main/resources/db/migration/V10__reproduction.sql`

- [ ] **Step 1:**

```sql
CREATE TABLE bull (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  internal_code VARCHAR(60) NOT NULL,
  name VARCHAR(160) NOT NULL,
  breed_id BIGINT NULL,
  source ENUM('OWN','EXTERNAL') NOT NULL,
  animal_id BIGINT NULL,
  registry_number VARCHAR(80) NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_bull_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_bull_breed FOREIGN KEY (breed_id) REFERENCES breed(id),
  CONSTRAINT fk_bull_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT uq_bull_account_code UNIQUE (account_id, internal_code),
  INDEX ix_bull_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE semen_straw (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  bull_id BIGINT NOT NULL,
  provider VARCHAR(160) NULL,
  batch_number VARCHAR(80) NULL,
  total_quantity INT NOT NULL DEFAULT 0,
  available_quantity INT NOT NULL DEFAULT 0,
  received_at DATE NULL,
  expires_at DATE NULL,
  cost_per_straw DECIMAL(10,2) NULL,
  storage_location VARCHAR(120) NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_straw_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_straw_bull FOREIGN KEY (bull_id) REFERENCES bull(id),
  INDEX ix_straw_account_bull_exp (account_id, bull_id, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE heat (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  detected_at TIMESTAMP NOT NULL,
  detection_method ENUM('VISUAL','PEDOMETER','HEAT_PATCH','CAMERA','OTHER') NULL,
  intensity ENUM('WEAK','MODERATE','STRONG') NULL,
  notes TEXT NULL,
  detected_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_heat_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_heat_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_heat_user FOREIGN KEY (detected_by_user_id) REFERENCES app_user(id),
  INDEX ix_heat_acct_animal_date (account_id, animal_id, detected_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE service_event (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  service_type ENUM('AI','NATURAL','EMBRYO_TRANSFER') NOT NULL,
  service_date DATE NOT NULL,
  bull_id BIGINT NULL,
  semen_straw_id BIGINT NULL,
  technician_name VARCHAR(160) NULL,
  heat_id BIGINT NULL,
  cost DECIMAL(10,2) NULL,
  notes TEXT NULL,
  created_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_serv_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_serv_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_serv_bull FOREIGN KEY (bull_id) REFERENCES bull(id),
  CONSTRAINT fk_serv_straw FOREIGN KEY (semen_straw_id) REFERENCES semen_straw(id),
  CONSTRAINT fk_serv_heat FOREIGN KEY (heat_id) REFERENCES heat(id),
  CONSTRAINT fk_serv_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  INDEX ix_serv_acct_animal_date (account_id, animal_id, service_date),
  INDEX ix_serv_acct_date (account_id, service_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pregnancy_check (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  service_id BIGINT NULL,
  checked_at DATE NOT NULL,
  method ENUM('PALPATION','ULTRASOUND','BLOOD_TEST','MILK_TEST') NULL,
  result ENUM('POSITIVE','NEGATIVE','DOUBTFUL') NOT NULL,
  estimated_gestation_days SMALLINT NULL,
  estimated_calving_date DATE NULL,
  vet_visit_id BIGINT NULL,
  checked_by_user_id BIGINT NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_pc_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_pc_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_pc_service FOREIGN KEY (service_id) REFERENCES service_event(id),
  CONSTRAINT fk_pc_visit FOREIGN KEY (vet_visit_id) REFERENCES vet_visit(id),
  CONSTRAINT fk_pc_user FOREIGN KEY (checked_by_user_id) REFERENCES app_user(id),
  INDEX ix_pc_acct_animal_date (account_id, animal_id, checked_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE calving (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  calved_at DATE NOT NULL,
  ease ENUM('FREE','EASY','ASSISTED','DIFFICULT','SURGERY') NOT NULL DEFAULT 'FREE',
  outcome ENUM('LIVE','STILLBORN','TWIN_LIVE','TWIN_MIXED','TWIN_STILLBORN') NOT NULL DEFAULT 'LIVE',
  calf_animal_id BIGINT NULL,
  calf_sex ENUM('FEMALE','MALE') NULL,
  calf_birth_weight_kg DECIMAL(5,2) NULL,
  pregnancy_check_id BIGINT NULL,
  notes TEXT NULL,
  created_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_calv_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_calv_dam FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_calv_calf FOREIGN KEY (calf_animal_id) REFERENCES animal(id),
  CONSTRAINT fk_calv_pc FOREIGN KEY (pregnancy_check_id) REFERENCES pregnancy_check(id),
  CONSTRAINT fk_calv_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  INDEX ix_calv_acct_animal_date (account_id, animal_id, calved_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE abortion (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  aborted_at DATE NOT NULL,
  estimated_gestation_days SMALLINT NULL,
  cause VARCHAR(300) NULL,
  pregnancy_check_id BIGINT NULL,
  notes TEXT NULL,
  created_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_ab_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_ab_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_ab_pc FOREIGN KEY (pregnancy_check_id) REFERENCES pregnancy_check(id),
  CONSTRAINT fk_ab_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  INDEX ix_ab_acct_animal_date (account_id, animal_id, aborted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE weaning (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  weaned_at DATE NOT NULL,
  weight_kg DECIMAL(6,2) NULL,
  notes TEXT NULL,
  created_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_wean_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_wean_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_wean_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  INDEX ix_wean_acct_animal_date (account_id, animal_id, weaned_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE dry_off (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  dried_off_at DATE NOT NULL,
  lactation_days SMALLINT NULL,
  notes TEXT NULL,
  created_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_do_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_do_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_do_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  INDEX ix_do_acct_animal_date (account_id, animal_id, dried_off_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

NOTA: la tabla `service` se nombra `service_event` en SQL porque `SERVICE` puede ser palabra reservada en algunas configs. Java entity puede llamarse `ServiceEvent` o `Service`; usaremos `ServiceEvent` para alinear y evitar conflictos con `org.springframework.stereotype.Service`.

- [ ] **Step 2: Pausa de revisión.**

---

## Épica B — Catálogos backend

### Task 3: Bull feature

**Files:**
- Create: `backend/src/main/java/com/digitalcow/reproduction/bull/Bull.java`
- Create: `backend/src/main/java/com/digitalcow/reproduction/bull/BullRepository.java`
- Create: `backend/src/main/java/com/digitalcow/reproduction/bull/BullService.java`
- Create: `backend/src/main/java/com/digitalcow/reproduction/bull/BullController.java`
- Create: `backend/src/main/java/com/digitalcow/reproduction/bull/dto/{BullCreateDto,BullUpdateDto,BullResponseDto}.java`
- Create: `backend/src/main/java/com/digitalcow/reproduction/bull/mapper/BullMapper.java`
- Create: `backend/src/main/java/com/digitalcow/reproduction/bull/BullSource.java` (enum OWN, EXTERNAL)

- [ ] **Step 1: Bull entity** con `@EntityListeners(TenantAwareEntityListener.class)` + `@FilterDef`/`@Filter("accountFilter")` igual que Fase 2. Campos del spec §2.1.

- [ ] **Step 2: Repository**

```java
@Repository
public interface BullRepository extends JpaRepository<Bull, Long>, JpaSpecificationExecutor<Bull> {
    boolean existsByInternalCode(String code);
}
```

- [ ] **Step 3: DTOs y Mapper** (records y MapStruct estándar).

- [ ] **Step 4: Service** con CRUD + `@PreAuthorize` (matriz: list todos, create/edit MANAGER+, delete ADMIN+; falla con CONFLICT si hay semen_straws asociadas).

- [ ] **Step 5: Controller** `/api/v1/reproduction/bulls` CRUD.

- [ ] **Step 6: Pausa de revisión.**

---

### Task 4: SemenStraw feature

**Files:**
- Create: paquete `backend/src/main/java/com/digitalcow/reproduction/semen/` con SemenStraw entity/repo/service/controller/dtos/mapper.

- [ ] **Step 1-5: Mismo patrón.** Endpoints `/api/v1/reproduction/semen-straws`. El service expone método `decrementAvailable(Long strawId)` usado por ServiceEvent al crear servicios AI:

```java
@Transactional
public void decrementAvailable(Long strawId) {
    SemenStraw straw = repository.findById(strawId)
        .orElseThrow(() -> BusinessException.notFound("Semen straw not found"));
    if (straw.getAvailableQuantity() <= 0) {
        throw BusinessException.conflict("SEMEN_STRAW_EMPTY", "Straw has no available doses");
    }
    straw.setAvailableQuantity(straw.getAvailableQuantity() - 1);
}
```

- [ ] **Step 6: Pausa de revisión.**

---

## Épica C — Eventos reproductivos backend

### Task 5: Heat feature

**Files:**
- Create: paquete `backend/src/main/java/com/digitalcow/reproduction/heat/`

- [ ] **Step 1-5: Sigue el patrón** (entity + repo + service + controller + DTOs + mapper + event `HeatChangedEvent`). Helper `/api/v1/animals/{id}/heats` GET.

---

### Task 6: ServiceEvent feature (con decremento de semen straw)

**Files:**
- Create: paquete `backend/src/main/java/com/digitalcow/reproduction/service/`
- ServiceEvent entity (tabla `service_event`, clase Java `ServiceEvent`).
- Repository, ServiceEventService, ServiceEventController, dto/, mapper/, event/.

- [ ] **Step 1: Entity ServiceEvent** anotada igual.

- [ ] **Step 2: Repository**

```java
@Repository
public interface ServiceEventRepository extends JpaRepository<ServiceEvent, Long>, JpaSpecificationExecutor<ServiceEvent> {
    List<ServiceEvent> findByAnimalIdOrderByServiceDateDesc(Long animalId);

    @Query("SELECT s FROM ServiceEvent s WHERE s.animalId = :animalId ORDER BY s.serviceDate DESC")
    List<ServiceEvent> findLastByAnimal(@Param("animalId") Long animalId, org.springframework.data.domain.Pageable pageable);
}
```

- [ ] **Step 3: ServiceEventService.create** con decremento de straw cuando AI:

```java
@PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
public ServiceEventResponseDto create(ServiceEventCreateDto dto) {
    if (dto.serviceType() == ServiceType.AI) {
        if (dto.semenStrawId() == null) {
            throw BusinessException.badRequest("AI service requires semen_straw_id");
        }
        semenStrawService.decrementAvailable(dto.semenStrawId());
    }
    if ((dto.serviceType() == ServiceType.AI || dto.serviceType() == ServiceType.NATURAL) && dto.bullId() == null) {
        throw BusinessException.badRequest("Bull required for AI/NATURAL");
    }
    ServiceEvent entity = mapper.fromCreate(dto);
    ServiceEvent saved = repository.save(entity);
    events.publishEvent(new ServiceEventChangedEvent(TenantContext.requireAccountId()));
    return mapper.toDto(saved);
}
```

- [ ] **Step 4: Controller** `/api/v1/reproduction/services`. Helper `/api/v1/animals/{id}/services` GET.

- [ ] **Step 5: Pausa de revisión.**

---

### Task 7: PregnancyCheck feature

**Files:**
- Create: paquete `backend/src/main/java/com/digitalcow/reproduction/pregnancy/`

- [ ] **Step 1: Sigue el patrón.** En el service, al crear con `result=POSITIVE` y `estimated_gestation_days != null`:

```java
if (dto.result() == PregnancyResult.POSITIVE && dto.estimatedGestationDays() != null) {
    int daysToCalving = 283 - dto.estimatedGestationDays();
    entity.setEstimatedCalvingDate(dto.checkedAt().plusDays(daysToCalving));
}
```

Si `result=NEGATIVE` o `DOUBTFUL`, no calcula.

- [ ] **Step 2: Helper `/api/v1/animals/{id}/pregnancy-checks` GET.**

- [ ] **Step 3: Pausa de revisión.**

---

### Task 8: Calving feature (con creación opcional de Animal hijo)

**Files:**
- Create: paquete `backend/src/main/java/com/digitalcow/reproduction/calving/`

- [ ] **Step 1: CalvingCreateDto incluye campo opcional para crear becerro:**

```java
public record CalvingCreateDto(
    @NotNull Long animalId,
    @NotNull LocalDate calvedAt,
    CalvingEase ease,
    CalvingOutcome outcome,
    Sex calfSex,
    BigDecimal calfBirthWeightKg,
    Long pregnancyCheckId,
    String notes,
    // Si createCalfAnimal=true, backend crea Animal hijo enlazado.
    boolean createCalfAnimal,
    String calfInternalTag,
    Long calfRanchId,
    Long calfLotId,
    Long calfBreedId,
    Purpose calfPurpose
) { }
```

- [ ] **Step 2: CalvingService.create**

```java
@PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
public CalvingResponseDto create(CalvingCreateDto dto) {
    Calving entity = mapper.fromCreate(dto);
    // sire_id del calf = bull del último servicio si existe
    if (dto.createCalfAnimal()) {
        if (dto.calfInternalTag() == null || dto.calfRanchId() == null || dto.calfBreedId() == null || dto.calfPurpose() == null || dto.calfSex() == null) {
            throw BusinessException.badRequest("Missing calf data");
        }
        Animal calf = new Animal();
        calf.setInternalTag(dto.calfInternalTag());
        calf.setRanchId(dto.calfRanchId());
        calf.setLotId(dto.calfLotId());
        calf.setBreedId(dto.calfBreedId());
        calf.setPurpose(dto.calfPurpose());
        calf.setSex(dto.calfSex());
        calf.setBirthDate(dto.calvedAt());
        calf.setBirthWeightKg(dto.calfBirthWeightKg());
        calf.setDamId(dto.animalId());
        // sire: ultimo servicio AI/NATURAL de la madre
        var lastService = serviceEventRepository.findByAnimalIdOrderByServiceDateDesc(dto.animalId())
            .stream().findFirst();
        lastService.ifPresent(s -> {
            if (s.getBullId() != null) {
                var bull = bullRepository.findById(s.getBullId()).orElse(null);
                if (bull != null && bull.getAnimalId() != null) {
                    calf.setSireId(bull.getAnimalId());
                } else if (bull != null) {
                    calf.setExternalSireName(bull.getName());
                }
            }
        });
        Animal savedCalf = animalRepository.save(calf);
        entity.setCalfAnimalId(savedCalf.getId());
    }
    Calving saved = repository.save(entity);
    events.publishEvent(new CalvingChangedEvent(TenantContext.requireAccountId()));
    return mapper.toDto(saved);
}
```

- [ ] **Step 3: Controller + helper /api/v1/animals/{id}/calvings.**

- [ ] **Step 4: Pausa de revisión.**

---

### Task 9: Abortion, Weaning, DryOff features

**Files:**
- Create: paquetes `backend/src/main/java/com/digitalcow/reproduction/{abortion,weaning,dryoff}/` con entity/repo/service/controller estándar.

- [ ] **Step 1-3: Sigue el patrón** sin lógica especial. CRUD multi-tenant.

- [ ] **Step 4: Endpoints** `/api/v1/reproduction/abortions`, `/weanings`, `/dry-offs`.

- [ ] **Step 5: Pausa de revisión.**

---

## Épica D — Alertas y KPIs backend

### Task 10: ReproductionAlertsService + Controller

**Files:**
- Create: paquete `backend/src/main/java/com/digitalcow/reproduction/alerts/` con AlertItemDto, ReproductionAlertsDto, Service, Controller.

- [ ] **Step 1: ReproductionAlertsDto**

```java
public record ReproductionAlertsDto(
    List<AlertItemDto> upcomingCalvings21d,
    List<AlertItemDto> dryOffDue,
    List<AlertItemDto> servedWithoutCheck,
    List<AlertItemDto> openTooLong
) { }
```

- [ ] **Step 2: ReproductionAlertsService**

Queries:
- **upcomingCalvings21d**: `SELECT FROM pregnancy_check WHERE result='POSITIVE' AND estimated_calving_date BETWEEN today AND today+21`
- **dryOffDue (dairy only)**: vacas con animal.purpose=DAIRY y última calving entre today-305 y today-280 sin dry_off posterior. Query nativa.
- **servedWithoutCheck**: services sin pregnancy_check posterior y >40 días.
- **openTooLong**: vacas con última calving >120 días y sin pregnancy_check POSITIVE posterior.

Cacheable `reproduction-alerts` 5 min con `tenantKeyGenerator`. Listeners en service/calving/pregnancy_check events.

- [ ] **Step 3: Controller** `/api/v1/reproduction/alerts` GET.

- [ ] **Step 4: Registrar cache "reproduction-alerts" en CacheConfig.**

- [ ] **Step 5: Pausa de revisión.**

---

### Task 11: ReproductionKpisService

**Files:**
- Create: `backend/src/main/java/com/digitalcow/reproduction/kpis/ReproductionKpisService.java`
- Create: `backend/src/main/java/com/digitalcow/reproduction/kpis/ReproductionKpisController.java`
- Create: `backend/src/main/java/com/digitalcow/reproduction/kpis/dto/ReproductionKpisDto.java`

- [ ] **Step 1: DTO**

```java
public record ReproductionKpisDto(
    LocalDate from,
    LocalDate to,
    Double daysOpenMedian,
    Double daysOpenP75,
    Double daysOpenMax,
    Double iepDays,
    Double firstCalvingAgeDays,
    Double firstServiceConceptionRate,
    Double servicesPerConception,
    Double pregnancyRate
) { }
```

- [ ] **Step 2: Service** con queries SQL nativas usando funciones agregadas. Para mediana, usar `WITH ordered AS (SELECT ..., ROW_NUMBER() OVER (ORDER BY days_open) AS rn, COUNT(*) OVER () AS cnt FROM ...)` o calcular en memoria con stream cuando el dataset sea pequeño.

Si las queries SQL nativas resultan complejas para una primera implementación, calcular en memoria: cargar todos los animales activos con sus services/calvings/pregnancy_checks y reducir en Java.

- [ ] **Step 3: Controller** `/api/v1/reproduction/kpis?from=YYYY-MM-DD&to=YYYY-MM-DD`.

- [ ] **Step 4: Pausa de revisión.**

---

### Task 12: DashboardReproduction endpoint

**Files:**
- Create: `backend/src/main/java/com/digitalcow/dashboard/DashboardReproductionService.java`
- Create: `backend/src/main/java/com/digitalcow/dashboard/DashboardReproductionController.java`
- Create: `backend/src/main/java/com/digitalcow/dashboard/dto/DashboardReproductionDto.java`

- [ ] **Step 1: DTO**

```java
public record DashboardReproductionDto(
    long pregnantConfirmed,
    long upcomingCalvings21d,
    long openCows,
    Double avgDaysOpen
) { }
```

- [ ] **Step 2: Service + cache "dashboard-reproduction" registrada en CacheConfig.**

- [ ] **Step 3: Controller** `/api/v1/dashboard/reproduction`.

- [ ] **Step 4: Pausa de revisión.**

---

## Épica E — Frontend

### Task 13: i18n nuevos namespaces + sidebar

**Files:**
- Create: `frontend/public/locales/{es,en}/reproduction.json`
- Create: `frontend/public/locales/{es,en}/reproductionAlerts.json`
- Modify: `frontend/src/lib/i18n.ts`
- Modify: sidebar (donde sea que esté en Fase 1/2; revisar `frontend/src/components/sidebar.tsx`)

- [ ] **Step 1: JSONs.** Estructura mínima para `reproduction.json` ES:

```json
{
  "nav": {
    "section": "Reproduccion",
    "overview": "Resumen",
    "bulls": "Toros",
    "semen": "Inventario de semen",
    "heats": "Celos",
    "services": "Servicios",
    "pregnancyChecks": "Diagnosticos de gestacion",
    "calvings": "Partos",
    "abortions": "Abortos",
    "weanings": "Destetes",
    "dryOffs": "Secados",
    "kpis": "KPIs reproductivos"
  },
  "bull": { "title": "Toros", "new": "Nuevo toro", "internalCode": "Codigo interno", "name": "Nombre", "breed": "Raza", "source": "Origen", "registryNumber": "Numero de registro" },
  "semen": { "title": "Inventario de semen", "new": "Nueva pajilla", "provider": "Proveedor", "batchNumber": "Lote", "totalQuantity": "Cantidad total", "availableQuantity": "Disponibles", "receivedAt": "Recibido", "expiresAt": "Vence", "costPerStraw": "Costo por pajilla", "storageLocation": "Ubicacion" },
  "heat": { "title": "Celos", "new": "Nuevo celo", "detectedAt": "Detectado", "detectionMethod": "Metodo", "intensity": "Intensidad" },
  "service": { "title": "Servicios", "new": "Nuevo servicio", "serviceType": "Tipo", "serviceDate": "Fecha", "bull": "Toro", "semenStraw": "Pajilla", "technician": "Tecnico", "estimatedCalvingDate": "Parto estimado" },
  "pregnancy": { "title": "Diagnosticos de gestacion", "new": "Nuevo diagnostico", "method": "Metodo", "result": "Resultado", "estimatedGestationDays": "Dias de gestacion estimados", "estimatedCalvingDate": "Fecha de parto estimada" },
  "calving": { "title": "Partos", "new": "Nuevo parto", "calvedAt": "Fecha", "ease": "Facilidad", "outcome": "Resultado", "calfSex": "Sexo del becerro", "calfBirthWeight": "Peso al nacer", "createCalfAnimal": "Registrar becerro como nuevo animal", "calfInternalTag": "Arete interno del becerro", "calfRanch": "Rancho del becerro", "calfLot": "Lote del becerro", "calfBreed": "Raza del becerro", "calfPurpose": "Proposito del becerro" },
  "abortion": { "title": "Abortos", "new": "Nuevo aborto", "abortedAt": "Fecha", "cause": "Causa" },
  "weaning": { "title": "Destetes", "new": "Nuevo destete", "weanedAt": "Fecha", "weightKg": "Peso (kg)" },
  "dryOff": { "title": "Secados", "new": "Nuevo secado", "driedOffAt": "Fecha", "lactationDays": "Dias en lactancia" },
  "kpis": { "title": "KPIs reproductivos", "daysOpen": "Dias abiertos", "median": "Mediana", "p75": "P75", "max": "Maximo", "iep": "IEP", "firstCalvingAge": "Edad al primer parto", "firstServiceConception": "Concepcion al 1er servicio", "servicesPerConception": "Servicios por concepcion", "pregnancyRate": "Tasa de gestacion" },
  "tab": { "reproduction": "Reproduccion" }
}
```

Y `reproductionAlerts.json` ES:

```json
{
  "title": "Alertas de reproduccion",
  "upcomingCalvings": "Proximos partos (21 dias)",
  "dryOffDue": "Vacas a secar",
  "servedWithoutCheck": "Servidas sin diagnostico",
  "openTooLong": "Vacas vacias mas de 120 dias",
  "noAlerts": "Sin alertas"
}
```

Crear contrapartes EN traducidas.

- [ ] **Step 2: Modificar `frontend/src/lib/i18n.ts`** agregando los nuevos namespaces a la lista `ns`.

- [ ] **Step 3: Modificar sidebar** agregando sección "Reproduccion" con NavLinks a las 9 páginas, usando icono `Heart` de lucide.

- [ ] **Step 4: Pausa de revisión.**

---

### Task 14: Bulls, Semen, Heat features frontend

**Files:**
- Create: `frontend/src/features/reproduction/{bulls,semen,heats}/{api,schemas,types}.ts`
- Create: `frontend/src/features/reproduction/{bulls,semen,heats}/components/<X>Form.tsx`
- Create: `frontend/src/pages/reproduction/{BullsPage,SemenPage,HeatsPage}.tsx`
- Modify: router.tsx con rutas `/reproduction/bulls`, `/reproduction/semen`, `/reproduction/heats`.

- [ ] **Step 1-4: Sigue el patrón de Fase 2** (VetVisit como referencia). Cada feature CRUD con form en dialog.

- [ ] **Step 5: BullForm** ofrece radio buttons OWN/EXTERNAL. Cuando OWN, muestra selector de animal (filtrado por sex='MALE' usando hook `useAnimals({sex:'MALE'})`).

- [ ] **Step 6: SemenStrawForm** con selector de bull (usando `useBulls()`), provider, batch, totalQuantity, availableQuantity (default = total), expiresAt, etc.

- [ ] **Step 7: HeatForm** con selector de animal (filtrado por sex='FEMALE'), detectedAt (datetime-local), detectionMethod (select), intensity.

- [ ] **Step 8: Pausa de revisión.**

---

### Task 15: ServiceEvent, PregnancyCheck features frontend

**Files:**
- Create: `frontend/src/features/reproduction/{services,pregnancyChecks}/...`
- Create: `frontend/src/pages/reproduction/{ServicesPage,PregnancyChecksPage}.tsx`
- Modify: router.tsx.

- [ ] **Step 1: ServiceForm**

```tsx
const serviceType = watch('serviceType');
const serviceDate = watch('serviceDate');
const estimatedCalving = serviceDate
  ? new Date(new Date(serviceDate).getTime() + 283 * 86400000).toISOString().slice(0,10)
  : null;

return (
  <form ...>
    <Select {...register('serviceType')}>
      <option value="AI">{t('reproduction:service.AI')}</option>
      <option value="NATURAL">{t('reproduction:service.NATURAL')}</option>
      <option value="EMBRYO_TRANSFER">{t('reproduction:service.EMBRYO_TRANSFER')}</option>
    </Select>
    {(serviceType === 'AI' || serviceType === 'NATURAL') && (
      <Select {...register('bullId', { valueAsNumber: true })}>
        {bulls.data?.map(b => <option key={b.id} value={b.id}>{b.name} ({b.internalCode})</option>)}
      </Select>
    )}
    {serviceType === 'AI' && (
      <Select {...register('semenStrawId', { valueAsNumber: true })}>
        {semenStraws.data?.filter(s => s.availableQuantity > 0).map(s => (
          <option key={s.id} value={s.id}>{s.batchNumber ?? '-'} ({s.availableQuantity} disponibles)</option>
        ))}
      </Select>
    )}
    <Input type="date" {...register('serviceDate')} />
    {estimatedCalving && <p className="text-sm">{t('reproduction:service.estimatedCalvingDate')}: {estimatedCalving}</p>}
    <Button type="submit">{t('common:actions.save')}</Button>
  </form>
);
```

- [ ] **Step 2: PregnancyCheckForm** con selector de animal, service (filtrado por animal), método (select), resultado (radio), gestation days. Cuando POSITIVE + gestation days, muestra estimated_calving = checked_at + (283 - gestation).

- [ ] **Step 3: Pausa de revisión.**

---

### Task 16: Calving, Abortion, Weaning, DryOff features

**Files:**
- Create: 4 features simétricos.
- Create: 4 páginas.
- Modify: router.tsx.

- [ ] **Step 1: CalvingForm con sub-form opcional** "Registrar becerro":

```tsx
const createCalf = watch('createCalfAnimal');
// ...
<Checkbox {...register('createCalfAnimal')} /> {t('reproduction:calving.createCalfAnimal')}
{createCalf && (
  <>
    <Input {...register('calfInternalTag')} placeholder={t('reproduction:calving.calfInternalTag')} />
    <Select {...register('calfRanchId', { valueAsNumber: true })}>...</Select>
    <Select {...register('calfBreedId', { valueAsNumber: true })}>...</Select>
    <Select {...register('calfPurpose')}>...</Select>
  </>
)}
```

- [ ] **Step 2: Pausa de revisión.**

---

### Task 17: ReproductionOverviewPage + KPIs page + dashboard widgets + tab animal

**Files:**
- Create: `frontend/src/features/reproduction/alerts/{api,components/AlertsList}.ts(x)`
- Create: `frontend/src/features/reproduction/kpis/api.ts`
- Create: `frontend/src/features/reproduction/dashboard/api.ts`
- Create: `frontend/src/pages/reproduction/{ReproductionOverviewPage,ReproductionKpisPage}.tsx`
- Create: `frontend/src/features/animals/components/AnimalReproductionTab.tsx`
- Modify: `frontend/src/pages/animals/AnimalDetailPage.tsx` (4ta tab)
- Modify: `frontend/src/pages/dashboard/DashboardPage.tsx` (sección Reproducción)
- Modify: router.tsx.

- [ ] **Step 1: ReproductionOverviewPage** con `AlertsList` reusable (similar a Fase 2): 4 buckets.

- [ ] **Step 2: ReproductionKpisPage** con date range picker (from/to defaults a últimos 12 meses), llama a `/reproduction/kpis?from=...&to=...`, muestra cards y un BarChart de distribución de días abiertos (placeholder: barras por bucket 0-60, 61-90, 91-120, 121+).

- [ ] **Step 3: DashboardPage** agrega sección "Reproduccion" con 4 cards del DTO `DashboardReproductionDto`.

- [ ] **Step 4: AnimalReproductionTab** con timeline ordenado por fecha desc combinando: heats, services, pregnancyChecks, calvings, abortions, weanings, dryOffs del animal. Cada item card con tipo + fecha + datos relevantes.

- [ ] **Step 5: AnimalDetailPage** agrega 4ta Tab `<TabsTrigger value="reproduction">`.

- [ ] **Step 6: Pausa de revisión.**

---

### Task 18: Actualizar DEFINITION_OF_DONE.md

**Files:**
- Modify: `docs/DEFINITION_OF_DONE.md`

- [ ] **Step 1: Agregar al final:**

```markdown

## Fase 3 — Reproduccion

- [ ] Migraciones V9 y V10 aplican limpio.
- [ ] Crear bull (OWN + EXTERNAL), semen_straw, heat, service AI con decremento de pajilla.
- [ ] Pregnancy check POSITIVE calcula estimated_calving_date.
- [ ] Calving con createCalfAnimal=true crea Animal hijo con sire/dam enlazados.
- [ ] KPIs reproductivos retornan valores coherentes (medianas, IEP, tasa de concepcion).
- [ ] Alertas muestran proximos partos, dry-off due, sin check, vacas vacias.
- [ ] Tab Reproduccion en /animals/:id muestra timeline cronologico.
- [ ] Dashboard /dashboard muestra cards de reproduccion.
- [ ] i18n ES/EN completo para reproduction y reproductionAlerts.
- [ ] Aislamiento multi-tenant validado.
- [ ] Roles aplicados.
```

---

## Notas finales para el ejecutor

- Sin git commits.
- Sin emojis. Sin arte ASCII en comentarios. Java sin acentos en comentarios; strings UI ES con acentos están OK.
- Sin ejecutar Maven/npm/tests.
- Si una clase referenciada en este plan no existe con el nombre exacto en Fase 1/2, busca el equivalente y adapta (ejemplo: si BusinessException tiene factory `BusinessException.notFound(...)` úsalo en lugar de `new BusinessException(ErrorCode.NOT_FOUND, ...)`).
- `tenantKeyGenerator` bean ya existe (Fase 2). Reutilizar.
- Si el helper "useAnimals filtrado por sex" no existe, agrégalo a `features/animals/api.ts`.
