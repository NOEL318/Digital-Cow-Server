# Digital Cow Fase 2 — Implementation Plan (Salud y Veterinaria)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implementar el módulo de Salud y Veterinaria sobre la plataforma de Fase 1: catálogos (vacunas, enfermedades, medicamentos, plagas), eventos por animal (vacunaciones, diagnósticos, tratamientos, controles de plagas), visitas veterinarias, planes sanitarios y alertas computadas. Frontend con páginas dedicadas, tab "Salud" en detalle de animal, widgets de dashboard.

**Architecture:** Mismo monolito modular Spring Boot 3.3. Nuevos paquetes Java: `catalog/` (vaccine, disease, medication, pest), `health/` (vaccination, diagnosis, treatment, pest_control, vet_visit, plan, alerts). Mismo frontend React: nuevas features `catalog/`, `health/`. Migraciones Flyway V6 a V11.

**Tech Stack:** Mismo que Fase 1 (sin nuevas deps).

**Spec de referencia:** `docs/superpowers/specs/2026-05-17-digital-cow-fase2-design.md`. NO REFINAR DECISIONES — ya están tomadas.

---

## Patrón del backend (referencia común para todas las tasks)

Cada feature (vaccine, disease, etc.) sigue este patrón. Donde una task dice "sigue el patrón X" se refiere a esto:

**Para CATÁLOGOS** (read-only):
```
backend/src/main/java/com/digitalcow/catalog/<feature>/
  <Feature>.java                  entity con @Entity, campos del spec §2.1
  <Feature>Repository.java        extends JpaRepository<Feature, Long>
  <Feature>Controller.java        @RestController @RequestMapping("/api/v1/catalog/<features>")
                                  un solo método: @GetMapping List<FeatureDto> list()
  dto/<Feature>Dto.java           registro Java
  mapper/<Feature>Mapper.java     @Mapper(componentModel = "spring")
```
- Catálogos NO llevan account_id (son globales).
- Cacheables: anotar el método list() con @Cacheable("catalog-<feature>").
- Sin servicio: el controller llama directo al repo (low complexity).

**Para EVENTOS** (multi-tenant CRUD):
```
backend/src/main/java/com/digitalcow/health/<feature>/
  <Feature>.java                  entity con @Entity, @EntityListeners(TenantAwareEntityListener.class), @Filter("accountFilter"), campos del spec §2.2
  <Feature>Repository.java        extends JpaRepository, JpaSpecificationExecutor
  <Feature>Specifications.java    builders de Specification<Feature> para filtros
  <Feature>Service.java           @Service, @Transactional; CRUD con @PreAuthorize por rol
  <Feature>Controller.java        @RestController
  dto/                            <Feature>CreateDto, <Feature>UpdateDto, <Feature>ResponseDto, <Feature>ListItemDto
  mapper/<Feature>Mapper.java     MapStruct
  event/<Feature>ChangedEvent.java  para invalidación de cache de alertas/dashboard
```

**Para Tests integración:** clase `<Feature>IT.java` extends `AbstractIT` (ya existe en backend/src/test desde Fase 1). Usa Testcontainers MySQL. NO los ejecutes durante implementación (sin Maven instalado); solo créalos.

---

## Patrón del frontend (referencia común)

Cada feature sigue:
```
frontend/src/features/<feature>/
  api/                hooks TanStack Query: use<Feature>s, useCreate<Feature>, useUpdate<Feature>, useDelete<Feature>
  schemas/<feature>.ts    zod schemas para forms
  components/<Feature>Form.tsx
  components/<Feature>Table.tsx
  types.ts            tipos TS
frontend/src/pages/<feature>/<Feature>ListPage.tsx
```

Páginas se registran en `frontend/src/app/router.tsx` (de Fase 1; modificar para agregar rutas).

Sidebar entries se agregan en `frontend/src/app/AppLayout.tsx` bajo una sección "Salud" colapsable, con icono lucide y RoleGate cuando aplique.

i18n: cada feature trae sus strings en `frontend/public/locales/{es,en}/<namespace>.json`. Nuevos namespaces: `health`, `catalog`, `alerts`. Inicializar el namespace en `frontend/src/lib/i18n.ts` (la lista de namespaces de Fase 1 se extiende).

---

## Épica A — Catálogos backend (datos seed)

### Task 1: Migración V6 catálogos (vaccine, disease, medication, pest)

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/resources/db/migration/V6__health_catalog.sql`

- [ ] **Step 1: Crear V6 con DDL de las 4 tablas**

```sql
CREATE TABLE vaccine (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(60) NOT NULL UNIQUE,
  name_es VARCHAR(160) NOT NULL,
  name_en VARCHAR(160) NOT NULL,
  target_diseases VARCHAR(400) NULL,
  default_dose_ml DECIMAL(5,2) NULL,
  route ENUM('IM','SC','ORAL','INTRANASAL','TOPICAL') NULL,
  recommended_age_months SMALLINT NULL,
  recommended_frequency_months SMALLINT NULL,
  species VARCHAR(20) NOT NULL DEFAULT 'BOVINE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE disease (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(60) NOT NULL UNIQUE,
  name_es VARCHAR(160) NOT NULL,
  name_en VARCHAR(160) NOT NULL,
  category ENUM('BACTERIAL','VIRAL','PARASITIC','METABOLIC','NUTRITIONAL','MECHANICAL','OTHER') NOT NULL,
  zoonotic BOOLEAN NOT NULL DEFAULT FALSE,
  severity ENUM('LOW','MEDIUM','HIGH') NOT NULL DEFAULT 'MEDIUM',
  default_symptoms VARCHAR(500) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE medication (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(60) NOT NULL UNIQUE,
  name_es VARCHAR(160) NOT NULL,
  name_en VARCHAR(160) NOT NULL,
  active_ingredient VARCHAR(200) NULL,
  default_dose VARCHAR(120) NULL,
  default_route ENUM('IM','SC','IV','ORAL','TOPICAL','INTRAMAMMARY') NULL,
  withdrawal_milk_days SMALLINT NOT NULL DEFAULT 0,
  withdrawal_meat_days SMALLINT NOT NULL DEFAULT 0,
  notes VARCHAR(400) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pest (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(60) NOT NULL UNIQUE,
  name_es VARCHAR(160) NOT NULL,
  name_en VARCHAR(160) NOT NULL,
  scientific_name VARCHAR(160) NULL,
  type ENUM('TICK','FLY','WORM','LICE','MITE','OTHER') NOT NULL,
  region ENUM('TROPICAL','TEMPERATE','ANY') NOT NULL DEFAULT 'ANY',
  notes VARCHAR(400) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

Después seed data idempotente. Usar `INSERT INTO ... ON DUPLICATE KEY UPDATE` por seguridad:

```sql
INSERT INTO vaccine (code, name_es, name_en, route, recommended_frequency_months) VALUES
  ('BRUCELLA_RB51', 'Brucella RB51', 'Brucella RB51', 'SC', NULL),
  ('IBR_BVD_PI3_BRSV', 'IBR/BVD/PI3/BRSV (Bovi-Shield)', 'IBR/BVD/PI3/BRSV (Bovi-Shield)', 'IM', 12),
  ('LEPTOSPIRA_PENTAVALENTE', 'Leptospira Pentavalente', 'Leptospira Pentavalent', 'IM', 6),
  ('CARBON_SINTOMATICO', 'Carbon sintomatico (Clostridiosis)', 'Blackleg (Clostridiosis)', 'SC', 12),
  ('RABIA_BOVINA', 'Rabia bovina', 'Bovine rabies', 'IM', 12),
  ('PASTEURELLA', 'Pasteurella multocida', 'Pasteurella multocida', 'SC', 12),
  ('FIEBRE_AFTOSA', 'Fiebre aftosa', 'Foot-and-mouth disease', 'IM', 6),
  ('ANTHRAX', 'Antrax', 'Anthrax', 'SC', 12),
  ('MASTITIS_J5', 'Mastitis Coliforme J5', 'Coliform Mastitis J5', 'IM', 6)
ON DUPLICATE KEY UPDATE name_es=VALUES(name_es), name_en=VALUES(name_en);

INSERT INTO disease (code, name_es, name_en, category, zoonotic, severity) VALUES
  ('MASTITIS', 'Mastitis', 'Mastitis', 'BACTERIAL', FALSE, 'MEDIUM'),
  ('BRD', 'Complejo respiratorio bovino', 'Bovine Respiratory Disease', 'BACTERIAL', FALSE, 'HIGH'),
  ('DIARREA_NEONATAL', 'Diarrea neonatal', 'Neonatal diarrhea', 'BACTERIAL', FALSE, 'HIGH'),
  ('COJERA', 'Cojera', 'Lameness', 'MECHANICAL', FALSE, 'MEDIUM'),
  ('BRUCELOSIS', 'Brucelosis', 'Brucellosis', 'BACTERIAL', TRUE, 'HIGH'),
  ('TUBERCULOSIS', 'Tuberculosis bovina', 'Bovine tuberculosis', 'BACTERIAL', TRUE, 'HIGH'),
  ('LEPTOSPIROSIS', 'Leptospirosis', 'Leptospirosis', 'BACTERIAL', TRUE, 'HIGH'),
  ('FIEBRE_AFTOSA_DIS', 'Fiebre aftosa', 'Foot-and-mouth disease', 'VIRAL', FALSE, 'HIGH'),
  ('IBR', 'Rinotraqueitis infecciosa bovina', 'Infectious bovine rhinotracheitis', 'VIRAL', FALSE, 'MEDIUM'),
  ('BVD', 'Diarrea viral bovina', 'Bovine viral diarrhea', 'VIRAL', FALSE, 'HIGH'),
  ('ANAPLASMOSIS', 'Anaplasmosis', 'Anaplasmosis', 'PARASITIC', FALSE, 'HIGH'),
  ('BABESIOSIS', 'Piroplasmosis (Babesiosis)', 'Babesiosis', 'PARASITIC', FALSE, 'HIGH'),
  ('ACETOSIS', 'Acetonemia', 'Ketosis', 'METABOLIC', FALSE, 'MEDIUM'),
  ('HIPOCALCEMIA', 'Hipocalcemia (Fiebre de leche)', 'Milk fever', 'METABOLIC', FALSE, 'HIGH'),
  ('METRITIS', 'Metritis', 'Metritis', 'BACTERIAL', FALSE, 'MEDIUM'),
  ('RETENCION_PLACENTA', 'Retencion placentaria', 'Retained placenta', 'OTHER', FALSE, 'MEDIUM')
ON DUPLICATE KEY UPDATE name_es=VALUES(name_es), name_en=VALUES(name_en), severity=VALUES(severity);

INSERT INTO medication (code, name_es, name_en, active_ingredient, withdrawal_milk_days, withdrawal_meat_days) VALUES
  ('OXITETRACICLINA_LA', 'Oxitetraciclina LA', 'Long-acting Oxytetracycline', 'Oxitetraciclina', 21, 28),
  ('PENICILINA', 'Penicilina G procainica', 'Penicillin G procaine', 'Penicillin G', 3, 14),
  ('ENROFLOXACINA', 'Enrofloxacina', 'Enrofloxacin', 'Enrofloxacin', 4, 14),
  ('IVERMECTINA', 'Ivermectina 1%', 'Ivermectin 1%', 'Ivermectin', 28, 35),
  ('FLUNIXIN', 'Flunixin meglumine', 'Flunixin meglumine', 'Flunixin meglumine', 2, 4),
  ('CEFTIOFUR', 'Ceftiofur', 'Ceftiofur', 'Ceftiofur', 0, 4),
  ('AMOXICILINA', 'Amoxicilina', 'Amoxicillin', 'Amoxicillin', 4, 25),
  ('DEXAMETASONA', 'Dexametasona', 'Dexamethasone', 'Dexamethasone', 3, 14),
  ('TIAMULINA', 'Tiamulina', 'Tiamulin', 'Tiamulin', 1, 5),
  ('CLORSULON', 'Clorsulon (fasciolicida)', 'Clorsulon (flukicide)', 'Clorsulon', 30, 8)
ON DUPLICATE KEY UPDATE name_es=VALUES(name_es), name_en=VALUES(name_en), withdrawal_milk_days=VALUES(withdrawal_milk_days), withdrawal_meat_days=VALUES(withdrawal_meat_days);

INSERT INTO pest (code, name_es, name_en, scientific_name, type, region) VALUES
  ('GARRAPATA_COMUN', 'Garrapata comun', 'Common cattle tick', 'Rhipicephalus microplus', 'TICK', 'TROPICAL'),
  ('MOSCA_CUERNO', 'Mosca del cuerno', 'Horn fly', 'Haematobia irritans', 'FLY', 'ANY'),
  ('MOSCA_BRAVA', 'Mosca brava', 'Stable fly', 'Stomoxys calcitrans', 'FLY', 'ANY'),
  ('GUSANO_BARRENADOR', 'Gusano barrenador', 'Screwworm', 'Cochliomyia hominivorax', 'WORM', 'TROPICAL'),
  ('PIOJO_BOVINO', 'Piojo bovino', 'Cattle louse', 'Haematopinus eurysternus', 'LICE', 'TEMPERATE'),
  ('ACARO_SARCOPTICO', 'Acaro sarcoptico', 'Sarcoptic mite', 'Sarcoptes scabiei', 'MITE', 'ANY'),
  ('GASTERINTESTINALES', 'Parasitos gastrointestinales', 'Gastrointestinal parasites', 'varios', 'WORM', 'ANY'),
  ('FASCIOLA', 'Fasciola hepatica', 'Liver fluke', 'Fasciola hepatica', 'WORM', 'TEMPERATE')
ON DUPLICATE KEY UPDATE name_es=VALUES(name_es), name_en=VALUES(name_en);
```

- [ ] **Step 2: Pausa de revisión** — verificar que `V6__health_catalog.sql` queda en `backend/src/main/resources/db/migration/` y no colisiona con V5.

---

### Task 2: Entities, repos y controllers de catálogo (Vaccine, Disease, Medication, Pest)

**Files:** (un paquete por entidad)
- Create: `backend/src/main/java/com/digitalcow/catalog/vaccine/Vaccine.java`
- Create: `backend/src/main/java/com/digitalcow/catalog/vaccine/VaccineRepository.java`
- Create: `backend/src/main/java/com/digitalcow/catalog/vaccine/VaccineController.java`
- Create: `backend/src/main/java/com/digitalcow/catalog/vaccine/dto/VaccineDto.java`
- Create: `backend/src/main/java/com/digitalcow/catalog/vaccine/mapper/VaccineMapper.java`
- Mismo set para `disease/`, `medication/`, `pest/`.

- [ ] **Step 1: Crear Vaccine entity (template para los demás)**

```java
package com.digitalcow.catalog.vaccine;

import com.digitalcow.common.AbstractAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Catalogo global de vacunas bovinas. Sin account_id (datos seed compartidos).
 */
@Entity
@Table(name = "vaccine")
@Getter
@Setter
public class Vaccine extends AbstractAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String code;

    @Column(name = "name_es", nullable = false, length = 160)
    private String nameEs;

    @Column(name = "name_en", nullable = false, length = 160)
    private String nameEn;

    @Column(name = "target_diseases", length = 400)
    private String targetDiseases;

    @Column(name = "default_dose_ml", precision = 5, scale = 2)
    private java.math.BigDecimal defaultDoseMl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private VaccineRoute route;

    @Column(name = "recommended_age_months")
    private Short recommendedAgeMonths;

    @Column(name = "recommended_frequency_months")
    private Short recommendedFrequencyMonths;

    @Column(nullable = false, length = 20)
    private String species = "BOVINE";
}
```

Y enum `VaccineRoute`:

```java
package com.digitalcow.catalog.vaccine;

/** Via de aplicacion de la vacuna. */
public enum VaccineRoute { IM, SC, ORAL, INTRANASAL, TOPICAL }
```

- [ ] **Step 2: VaccineRepository**

```java
package com.digitalcow.catalog.vaccine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repo read-only de catalogo global. */
@Repository
public interface VaccineRepository extends JpaRepository<Vaccine, Long> { }
```

- [ ] **Step 3: VaccineDto record**

```java
package com.digitalcow.catalog.vaccine.dto;

import com.digitalcow.catalog.vaccine.VaccineRoute;
import java.math.BigDecimal;

/** DTO publico de Vaccine. */
public record VaccineDto(
    Long id,
    String code,
    String nameEs,
    String nameEn,
    String targetDiseases,
    BigDecimal defaultDoseMl,
    VaccineRoute route,
    Short recommendedAgeMonths,
    Short recommendedFrequencyMonths
) { }
```

- [ ] **Step 4: VaccineMapper (MapStruct)**

```java
package com.digitalcow.catalog.vaccine.mapper;

import com.digitalcow.catalog.vaccine.Vaccine;
import com.digitalcow.catalog.vaccine.dto.VaccineDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VaccineMapper {
    VaccineDto toDto(Vaccine entity);
}
```

- [ ] **Step 5: VaccineController**

```java
package com.digitalcow.catalog.vaccine;

import com.digitalcow.catalog.vaccine.dto.VaccineDto;
import com.digitalcow.catalog.vaccine.mapper.VaccineMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Lista global de vacunas. Cacheable. */
@RestController
@RequestMapping("/api/v1/catalog/vaccines")
public class VaccineController {

    private final VaccineRepository repository;
    private final VaccineMapper mapper;

    public VaccineController(VaccineRepository repository, VaccineMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /** Devuelve todas las vacunas ordenadas por code. */
    @GetMapping
    @Cacheable("catalog-vaccines")
    public List<VaccineDto> list() {
        return repository.findAll(org.springframework.data.domain.Sort.by("code"))
            .stream().map(mapper::toDto).toList();
    }
}
```

- [ ] **Step 6: Repetir el patrón para Disease, Medication, Pest**

Usar exactamente el mismo template:
- `Disease` con campos del spec §2.1; enum `DiseaseCategory` y `DiseaseSeverity`
- `Medication` con campos del spec §2.1; enum `MedicationRoute`
- `Pest` con campos del spec §2.1; enums `PestType`, `PestRegion`

DTOs como `record`. Mappers MapStruct. Controllers en `/api/v1/catalog/diseases`, `/medications`, `/pests`, con `@Cacheable("catalog-diseases")`, etc.

- [ ] **Step 7: Registrar caches en CacheConfig**

Modificar `backend/src/main/java/com/digitalcow/config/CacheConfig.java` para agregar nombres:

```java
// Dentro del bean CaffeineCacheManager:
manager.setCacheNames(java.util.List.of(
    "dashboard-summary",
    "catalog-vaccines",
    "catalog-diseases",
    "catalog-medications",
    "catalog-pests",
    "health-alerts"
));
```

(Si Fase 1 ya tenía `dashboard-summary` mantenerlo; sumar los nuevos.)

- [ ] **Step 8: Pausa de revisión** — verificar las 4 entities, repos, controllers en `catalog/*`.

---

## Épica B — Eventos sanitarios backend

### Task 3: Migración V7 eventos sanitarios (vet_visit, vaccination, diagnosis, treatment, pest_control)

**Files:**
- Create: `backend/src/main/resources/db/migration/V7__health_events.sql`

- [ ] **Step 1: Crear V7 con DDL completo del spec §2.2**

```sql
CREATE TABLE vet_visit (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  ranch_id BIGINT NOT NULL,
  visited_at DATE NOT NULL,
  vet_name VARCHAR(160) NOT NULL,
  vet_contact VARCHAR(160) NULL,
  reason VARCHAR(300) NOT NULL,
  total_cost DECIMAL(12,2) NULL,
  notes TEXT NULL,
  created_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_vet_visit_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_vet_visit_ranch FOREIGN KEY (ranch_id) REFERENCES ranch(id),
  CONSTRAINT fk_vet_visit_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  INDEX ix_vet_visit_acct_ranch_date (account_id, ranch_id, visited_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE vaccination (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NULL,
  lot_id BIGINT NULL,
  vaccine_id BIGINT NOT NULL,
  batch_number VARCHAR(80) NULL,
  applied_at DATE NOT NULL,
  dose_ml DECIMAL(5,2) NULL,
  route ENUM('IM','SC','ORAL','INTRANASAL','TOPICAL') NULL,
  next_dose_due DATE NULL,
  cost DECIMAL(10,2) NULL,
  applied_by_user_id BIGINT NULL,
  vet_visit_id BIGINT NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_vaccination_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_vaccination_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_vaccination_lot FOREIGN KEY (lot_id) REFERENCES lot(id),
  CONSTRAINT fk_vaccination_vaccine FOREIGN KEY (vaccine_id) REFERENCES vaccine(id),
  CONSTRAINT fk_vaccination_user FOREIGN KEY (applied_by_user_id) REFERENCES app_user(id),
  CONSTRAINT fk_vaccination_visit FOREIGN KEY (vet_visit_id) REFERENCES vet_visit(id),
  INDEX ix_vacc_acct_animal_date (account_id, animal_id, applied_at),
  INDEX ix_vacc_acct_lot_date (account_id, lot_id, applied_at),
  INDEX ix_vacc_next_dose (account_id, next_dose_due)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE diagnosis (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  disease_id BIGINT NOT NULL,
  diagnosed_at DATE NOT NULL,
  severity ENUM('LOW','MEDIUM','HIGH') NOT NULL DEFAULT 'MEDIUM',
  symptoms VARCHAR(500) NULL,
  status ENUM('ACTIVE','RECOVERED','CHRONIC','DECEASED') NOT NULL DEFAULT 'ACTIVE',
  resolved_at DATE NULL,
  diagnosed_by_user_id BIGINT NULL,
  vet_visit_id BIGINT NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_diag_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_diag_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_diag_disease FOREIGN KEY (disease_id) REFERENCES disease(id),
  CONSTRAINT fk_diag_user FOREIGN KEY (diagnosed_by_user_id) REFERENCES app_user(id),
  CONSTRAINT fk_diag_visit FOREIGN KEY (vet_visit_id) REFERENCES vet_visit(id),
  INDEX ix_diag_acct_animal_date (account_id, animal_id, diagnosed_at),
  INDEX ix_diag_acct_status_date (account_id, status, diagnosed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE treatment (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  diagnosis_id BIGINT NULL,
  medication_id BIGINT NOT NULL,
  started_at DATE NOT NULL,
  ended_at DATE NULL,
  dose VARCHAR(120) NULL,
  doses_count SMALLINT NULL,
  route ENUM('IM','SC','IV','ORAL','TOPICAL','INTRAMAMMARY') NULL,
  withdrawal_milk_until DATE NULL,
  withdrawal_meat_until DATE NULL,
  cost DECIMAL(10,2) NULL,
  prescribed_by VARCHAR(160) NULL,
  vet_visit_id BIGINT NULL,
  created_by_user_id BIGINT NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_treat_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_treat_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_treat_diagnosis FOREIGN KEY (diagnosis_id) REFERENCES diagnosis(id),
  CONSTRAINT fk_treat_medication FOREIGN KEY (medication_id) REFERENCES medication(id),
  CONSTRAINT fk_treat_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  CONSTRAINT fk_treat_visit FOREIGN KEY (vet_visit_id) REFERENCES vet_visit(id),
  INDEX ix_treat_acct_animal_date (account_id, animal_id, started_at),
  INDEX ix_treat_wd_milk (account_id, withdrawal_milk_until),
  INDEX ix_treat_wd_meat (account_id, withdrawal_meat_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pest_control (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  ranch_id BIGINT NULL,
  lot_id BIGINT NULL,
  pest_id BIGINT NOT NULL,
  product_used VARCHAR(200) NOT NULL,
  dose VARCHAR(120) NULL,
  applied_at DATE NOT NULL,
  next_application_at DATE NULL,
  cost DECIMAL(10,2) NULL,
  applied_by_user_id BIGINT NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_pc_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_pc_ranch FOREIGN KEY (ranch_id) REFERENCES ranch(id),
  CONSTRAINT fk_pc_lot FOREIGN KEY (lot_id) REFERENCES lot(id),
  CONSTRAINT fk_pc_pest FOREIGN KEY (pest_id) REFERENCES pest(id),
  CONSTRAINT fk_pc_user FOREIGN KEY (applied_by_user_id) REFERENCES app_user(id),
  INDEX ix_pc_acct_ranch_date (account_id, ranch_id, applied_at),
  INDEX ix_pc_acct_lot_date (account_id, lot_id, applied_at),
  INDEX ix_pc_next (account_id, next_application_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 2: Pausa de revisión** — verificar FKs y orden (vet_visit antes que vaccination/diagnosis/treatment para que las FKs resuelvan).

---

### Task 4: VetVisit feature (entity + repo + service + controller)

**Files:**
- Create: `backend/src/main/java/com/digitalcow/health/vetvisit/VetVisit.java`
- Create: `backend/src/main/java/com/digitalcow/health/vetvisit/VetVisitRepository.java`
- Create: `backend/src/main/java/com/digitalcow/health/vetvisit/VetVisitService.java`
- Create: `backend/src/main/java/com/digitalcow/health/vetvisit/VetVisitController.java`
- Create: `backend/src/main/java/com/digitalcow/health/vetvisit/dto/VetVisitCreateDto.java`
- Create: `backend/src/main/java/com/digitalcow/health/vetvisit/dto/VetVisitUpdateDto.java`
- Create: `backend/src/main/java/com/digitalcow/health/vetvisit/dto/VetVisitResponseDto.java`
- Create: `backend/src/main/java/com/digitalcow/health/vetvisit/mapper/VetVisitMapper.java`

- [ ] **Step 1: VetVisit entity**

```java
package com.digitalcow.health.vetvisit;

import com.digitalcow.common.AbstractAuditableEntity;
import com.digitalcow.tenancy.TenantAwareEntityListener;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Visita veterinaria. Agrupa eventos sanitarios del mismo dia/rancho.
 * Multi-tenant via filtro accountFilter.
 */
@Entity
@Table(name = "vet_visit")
@EntityListeners(TenantAwareEntityListener.class)
@FilterDef(name = "accountFilter", parameters = @ParamDef(name = "accountId", type = Long.class))
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter
@Setter
public class VetVisit extends AbstractAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "ranch_id", nullable = false)
    private Long ranchId;

    @Column(name = "visited_at", nullable = false)
    private LocalDate visitedAt;

    @Column(name = "vet_name", nullable = false, length = 160)
    private String vetName;

    @Column(name = "vet_contact", length = 160)
    private String vetContact;

    @Column(nullable = false, length = 300)
    private String reason;

    @Column(name = "total_cost", precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;
}
```

- [ ] **Step 2: Repository**

```java
package com.digitalcow.health.vetvisit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface VetVisitRepository extends JpaRepository<VetVisit, Long>, JpaSpecificationExecutor<VetVisit> {
    long countByRanchId(Long ranchId);
}
```

- [ ] **Step 3: DTOs**

```java
package com.digitalcow.health.vetvisit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Create payload de VetVisit. */
public record VetVisitCreateDto(
    @NotNull Long ranchId,
    @NotNull LocalDate visitedAt,
    @NotBlank String vetName,
    String vetContact,
    @NotBlank String reason,
    BigDecimal totalCost,
    String notes
) { }
```

```java
package com.digitalcow.health.vetvisit.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Update parcial. Todos los campos opcionales. */
public record VetVisitUpdateDto(
    LocalDate visitedAt,
    String vetName,
    String vetContact,
    String reason,
    BigDecimal totalCost,
    String notes
) { }
```

```java
package com.digitalcow.health.vetvisit.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Response DTO. */
public record VetVisitResponseDto(
    Long id,
    Long ranchId,
    LocalDate visitedAt,
    String vetName,
    String vetContact,
    String reason,
    BigDecimal totalCost,
    String notes
) { }
```

- [ ] **Step 4: Mapper**

```java
package com.digitalcow.health.vetvisit.mapper;

import com.digitalcow.health.vetvisit.VetVisit;
import com.digitalcow.health.vetvisit.dto.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface VetVisitMapper {
    VetVisitResponseDto toDto(VetVisit entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    VetVisit fromCreate(VetVisitCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void applyUpdate(VetVisitUpdateDto dto, @MappingTarget VetVisit entity);
}
```

- [ ] **Step 5: Service**

```java
package com.digitalcow.health.vetvisit;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.health.vetvisit.dto.*;
import com.digitalcow.health.vetvisit.mapper.VetVisitMapper;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Servicio CRUD de VetVisit con autorizacion por rol. */
@Service
@Transactional
public class VetVisitService {

    private final VetVisitRepository repository;
    private final VetVisitMapper mapper;

    public VetVisitService(VetVisitRepository repository, VetVisitMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /** Lista todas las visitas de la cuenta del usuario actual. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<VetVisitResponseDto> list() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    /** Crea una visita. Worker o superior. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public VetVisitResponseDto create(VetVisitCreateDto dto) {
        VetVisit entity = mapper.fromCreate(dto);
        // accountId lo setea TenantAwareEntityListener; ranch_id se asume valido (FK valida en DB)
        VetVisit saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    /** Actualiza una visita por id. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public VetVisitResponseDto update(Long id, VetVisitUpdateDto dto) {
        VetVisit entity = repository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Vet visit not found"));
        mapper.applyUpdate(dto, entity);
        return mapper.toDto(entity);
    }

    /** Borra una visita. Falla si tiene eventos asociados (vaccination/diagnosis/treatment con vet_visit_id). */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public void delete(Long id) {
        VetVisit entity = repository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Vet visit not found"));
        // El check de eventos asociados se hace en el servicio del evento (FK ON DELETE RESTRICT lo enforced).
        try {
            repository.delete(entity);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new BusinessException(ErrorCode.CONFLICT, "Vet visit has associated events");
        }
    }
}
```

- [ ] **Step 6: Controller**

```java
package com.digitalcow.health.vetvisit;

import com.digitalcow.health.vetvisit.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Endpoints REST de VetVisit. */
@RestController
@RequestMapping("/api/v1/health/vet-visits")
public class VetVisitController {

    private final VetVisitService service;

    public VetVisitController(VetVisitService service) {
        this.service = service;
    }

    @GetMapping
    public List<VetVisitResponseDto> list() {
        return service.list();
    }

    @PostMapping
    public ResponseEntity<VetVisitResponseDto> create(@Valid @RequestBody VetVisitCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    @PatchMapping("/{id}")
    public VetVisitResponseDto update(@PathVariable Long id, @Valid @RequestBody VetVisitUpdateDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

- [ ] **Step 7: Pausa de revisión** — verificar paquete `health/vetvisit/` completo.

---

### Task 5: Vaccination feature (con expansión por lote)

**Files:**
- Create: `backend/src/main/java/com/digitalcow/health/vaccination/Vaccination.java`
- Create: `backend/src/main/java/com/digitalcow/health/vaccination/VaccinationRepository.java`
- Create: `backend/src/main/java/com/digitalcow/health/vaccination/VaccinationSpecifications.java`
- Create: `backend/src/main/java/com/digitalcow/health/vaccination/VaccinationService.java`
- Create: `backend/src/main/java/com/digitalcow/health/vaccination/VaccinationController.java`
- Create: `backend/src/main/java/com/digitalcow/health/vaccination/dto/VaccinationCreateDto.java`
- Create: `backend/src/main/java/com/digitalcow/health/vaccination/dto/VaccinationBulkDto.java`
- Create: `backend/src/main/java/com/digitalcow/health/vaccination/dto/VaccinationUpdateDto.java`
- Create: `backend/src/main/java/com/digitalcow/health/vaccination/dto/VaccinationResponseDto.java`
- Create: `backend/src/main/java/com/digitalcow/health/vaccination/mapper/VaccinationMapper.java`
- Create: `backend/src/main/java/com/digitalcow/health/vaccination/event/VaccinationChangedEvent.java`

- [ ] **Step 1: Vaccination entity** (similar a VetVisit; ver §2.2 del spec). Campos: account_id, animal_id (NULL), lot_id (NULL), vaccine_id, batch_number, applied_at, dose_ml, route (enum), next_dose_due, cost, applied_by_user_id, vet_visit_id, notes. Anotada igual con @EntityListeners + @Filter accountFilter.

- [ ] **Step 2: Repository con custom queries**

```java
package com.digitalcow.health.vaccination;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VaccinationRepository extends JpaRepository<Vaccination, Long>, JpaSpecificationExecutor<Vaccination> {

    List<Vaccination> findByAnimalIdOrderByAppliedAtDesc(Long animalId);

    @Query("SELECT v FROM Vaccination v WHERE v.nextDoseDue BETWEEN :from AND :to ORDER BY v.nextDoseDue")
    List<Vaccination> findUpcomingDoses(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
```

- [ ] **Step 3: DTOs**

```java
package com.digitalcow.health.vaccination.dto;

import com.digitalcow.catalog.vaccine.VaccineRoute;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Vacunacion individual. Requiere animalId. */
public record VaccinationCreateDto(
    @NotNull Long animalId,
    @NotNull Long vaccineId,
    @NotNull LocalDate appliedAt,
    String batchNumber,
    BigDecimal doseMl,
    VaccineRoute route,
    BigDecimal cost,
    Long vetVisitId,
    String notes
) { }
```

```java
package com.digitalcow.health.vaccination.dto;

import com.digitalcow.catalog.vaccine.VaccineRoute;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Vacunacion masiva a un lote completo. */
public record VaccinationBulkDto(
    @NotNull Long lotId,
    @NotNull Long vaccineId,
    @NotNull LocalDate appliedAt,
    String batchNumber,
    BigDecimal doseMl,
    VaccineRoute route,
    BigDecimal cost,
    Long vetVisitId,
    String notes
) { }
```

```java
package com.digitalcow.health.vaccination.dto;

import com.digitalcow.catalog.vaccine.VaccineRoute;
import java.math.BigDecimal;
import java.time.LocalDate;

public record VaccinationUpdateDto(
    LocalDate appliedAt,
    String batchNumber,
    BigDecimal doseMl,
    VaccineRoute route,
    LocalDate nextDoseDue,
    BigDecimal cost,
    String notes
) { }
```

```java
package com.digitalcow.health.vaccination.dto;

import com.digitalcow.catalog.vaccine.VaccineRoute;
import java.math.BigDecimal;
import java.time.LocalDate;

public record VaccinationResponseDto(
    Long id,
    Long animalId,
    Long lotId,
    Long vaccineId,
    String vaccineNameEs,
    String vaccineNameEn,
    String batchNumber,
    LocalDate appliedAt,
    BigDecimal doseMl,
    VaccineRoute route,
    LocalDate nextDoseDue,
    BigDecimal cost,
    Long vetVisitId,
    String notes
) { }
```

- [ ] **Step 4: Mapper (con join expandido al catálogo)**

```java
package com.digitalcow.health.vaccination.mapper;

import com.digitalcow.catalog.vaccine.Vaccine;
import com.digitalcow.health.vaccination.Vaccination;
import com.digitalcow.health.vaccination.dto.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface VaccinationMapper {

    @Mapping(target = "vaccineNameEs", source = "vaccine.nameEs")
    @Mapping(target = "vaccineNameEn", source = "vaccine.nameEn")
    VaccinationResponseDto toDto(Vaccination entity, Vaccine vaccine);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "appliedByUserId", ignore = true)
    @Mapping(target = "nextDoseDue", ignore = true)
    @Mapping(target = "lotId", ignore = true)
    Vaccination fromCreate(VaccinationCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void applyUpdate(VaccinationUpdateDto dto, @MappingTarget Vaccination entity);
}
```

- [ ] **Step 5: Event para invalidación de caches**

```java
package com.digitalcow.health.vaccination.event;

/** Evento que dispara invalidacion de caches dashboard/alerts. */
public record VaccinationChangedEvent(Long accountId) { }
```

- [ ] **Step 6: Service con lógica de expansión por lote y cálculo de next_dose_due**

```java
package com.digitalcow.health.vaccination;

import com.digitalcow.animal.Animal;
import com.digitalcow.animal.AnimalRepository;
import com.digitalcow.animal.AnimalStatus;
import com.digitalcow.catalog.vaccine.Vaccine;
import com.digitalcow.catalog.vaccine.VaccineRepository;
import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.health.vaccination.dto.*;
import com.digitalcow.health.vaccination.event.VaccinationChangedEvent;
import com.digitalcow.health.vaccination.mapper.VaccinationMapper;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Servicio de Vaccination. Soporta vacunacion individual y por lote.
 * El bulk expande a una fila por animal activo del lote.
 */
@Service
@Transactional
public class VaccinationService {

    private final VaccinationRepository repository;
    private final VaccineRepository vaccineRepository;
    private final AnimalRepository animalRepository;
    private final VaccinationMapper mapper;
    private final ApplicationEventPublisher events;

    public VaccinationService(VaccinationRepository repository,
                              VaccineRepository vaccineRepository,
                              AnimalRepository animalRepository,
                              VaccinationMapper mapper,
                              ApplicationEventPublisher events) {
        this.repository = repository;
        this.vaccineRepository = vaccineRepository;
        this.animalRepository = animalRepository;
        this.mapper = mapper;
        this.events = events;
    }

    /** Lista vacunaciones de un animal en orden cronologico inverso. */
    @Transactional(readOnly = true)
    public List<VaccinationResponseDto> listByAnimal(Long animalId) {
        return repository.findByAnimalIdOrderByAppliedAtDesc(animalId).stream()
            .map(v -> toDto(v))
            .toList();
    }

    /** Crea vacunacion individual. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public VaccinationResponseDto create(VaccinationCreateDto dto) {
        Vaccine vaccine = vaccineRepository.findById(dto.vaccineId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Vaccine not found"));
        Vaccination entity = mapper.fromCreate(dto);
        entity.setNextDoseDue(computeNextDoseDue(dto.appliedAt(), vaccine));
        Vaccination saved = repository.save(entity);
        events.publishEvent(new VaccinationChangedEvent(TenantContext.requireAccountId()));
        return toDto(saved);
    }

    /** Crea vacunaciones masivas: una fila por animal activo del lote. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public List<VaccinationResponseDto> createBulk(VaccinationBulkDto dto) {
        Vaccine vaccine = vaccineRepository.findById(dto.vaccineId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Vaccine not found"));
        List<Animal> animals = animalRepository.findByLotIdAndStatus(dto.lotId(), AnimalStatus.ACTIVE);
        if (animals.isEmpty()) {
            throw new BusinessException(ErrorCode.CONFLICT, "Lot has no active animals");
        }
        LocalDate nextDose = computeNextDoseDue(dto.appliedAt(), vaccine);
        List<Vaccination> rows = animals.stream().map(animal -> {
            Vaccination v = new Vaccination();
            v.setAnimalId(animal.getId());
            v.setLotId(dto.lotId());
            v.setVaccineId(dto.vaccineId());
            v.setBatchNumber(dto.batchNumber());
            v.setAppliedAt(dto.appliedAt());
            v.setDoseMl(dto.doseMl());
            v.setRoute(dto.route());
            v.setNextDoseDue(nextDose);
            v.setCost(dto.cost());
            v.setVetVisitId(dto.vetVisitId());
            v.setNotes(dto.notes());
            return v;
        }).toList();
        List<Vaccination> saved = repository.saveAll(rows);
        events.publishEvent(new VaccinationChangedEvent(TenantContext.requireAccountId()));
        return saved.stream().map(this::toDto).toList();
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public VaccinationResponseDto update(Long id, VaccinationUpdateDto dto) {
        Vaccination entity = repository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Vaccination not found"));
        mapper.applyUpdate(dto, entity);
        events.publishEvent(new VaccinationChangedEvent(TenantContext.requireAccountId()));
        return toDto(entity);
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        Vaccination entity = repository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Vaccination not found"));
        repository.delete(entity);
        events.publishEvent(new VaccinationChangedEvent(TenantContext.requireAccountId()));
    }

    private LocalDate computeNextDoseDue(LocalDate appliedAt, Vaccine vaccine) {
        Short months = vaccine.getRecommendedFrequencyMonths();
        return months != null && months > 0 ? appliedAt.plusMonths(months) : null;
    }

    private VaccinationResponseDto toDto(Vaccination v) {
        Vaccine vaccine = vaccineRepository.findById(v.getVaccineId()).orElse(null);
        return mapper.toDto(v, vaccine);
    }
}
```

Necesita que `AnimalRepository` exponga `findByLotIdAndStatus(Long lotId, AnimalStatus status)`. Verificar si ya existe; si no, agregarlo a `backend/src/main/java/com/digitalcow/animal/AnimalRepository.java`.

- [ ] **Step 7: Controller**

```java
package com.digitalcow.health.vaccination;

import com.digitalcow.health.vaccination.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/health/vaccinations")
public class VaccinationController {

    private final VaccinationService service;

    public VaccinationController(VaccinationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<VaccinationResponseDto> create(@Valid @RequestBody VaccinationCreateDto dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<VaccinationResponseDto>> createBulk(@Valid @RequestBody VaccinationBulkDto dto) {
        return ResponseEntity.status(201).body(service.createBulk(dto));
    }

    @PatchMapping("/{id}")
    public VaccinationResponseDto update(@PathVariable Long id, @Valid @RequestBody VaccinationUpdateDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

Endpoint `/api/v1/animals/{id}/vaccinations`: agregar método al `AnimalController` existente o crear un nuevo `AnimalVaccinationsController` en este mismo paquete:

```java
package com.digitalcow.health.vaccination;

import com.digitalcow.health.vaccination.dto.VaccinationResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Helper para timeline de salud de un animal. */
@RestController
@RequestMapping("/api/v1/animals/{animalId}/vaccinations")
public class AnimalVaccinationsController {

    private final VaccinationService service;

    public AnimalVaccinationsController(VaccinationService service) {
        this.service = service;
    }

    @GetMapping
    public List<VaccinationResponseDto> list(@PathVariable Long animalId) {
        return service.listByAnimal(animalId);
    }
}
```

- [ ] **Step 8: Pausa de revisión** — verificar expansión por lote, cálculo de next_dose_due, evento publicado.

---

### Task 6: Diagnosis feature

**Files:**
- Create: `backend/src/main/java/com/digitalcow/health/diagnosis/Diagnosis.java`
- Create: `backend/src/main/java/com/digitalcow/health/diagnosis/DiagnosisRepository.java`
- Create: `backend/src/main/java/com/digitalcow/health/diagnosis/DiagnosisService.java`
- Create: `backend/src/main/java/com/digitalcow/health/diagnosis/DiagnosisController.java`
- Create: `backend/src/main/java/com/digitalcow/health/diagnosis/dto/{DiagnosisCreateDto, DiagnosisUpdateDto, DiagnosisResponseDto}.java`
- Create: `backend/src/main/java/com/digitalcow/health/diagnosis/mapper/DiagnosisMapper.java`
- Create: `backend/src/main/java/com/digitalcow/health/diagnosis/event/DiagnosisChangedEvent.java`
- Create: `backend/src/main/java/com/digitalcow/health/diagnosis/AnimalDiagnosesController.java` (helper para `/api/v1/animals/{id}/diagnoses`)

- [ ] **Step 1: Sigue el patrón de Vaccination (entity + repo + service + controller + DTOs)**, con estas diferencias específicas:

  - Sin lote (siempre individual): `animal_id NOT NULL`.
  - Enum local `DiagnosisStatus { ACTIVE, RECOVERED, CHRONIC, DECEASED }` y reutiliza `DiseaseSeverity` del catálogo.
  - El update permite cambiar `status`; cuando pasa a RECOVERED/CHRONIC/DECEASED se setea `resolved_at = LocalDate.now()` si viene null.
  - Borrar diagnosis falla con `CONFLICT` si tiene tratamientos asociados (`treatmentRepository.existsByDiagnosisId(id)`).
  - Permisos: igual matriz que Vaccination.
  - Publica `DiagnosisChangedEvent` en create/update/delete.

- [ ] **Step 2: DiagnosisResponseDto incluye `diseaseNameEs`, `diseaseNameEn`** vía join en el mapper.

- [ ] **Step 3: Helper controller `/api/v1/animals/{id}/diagnoses` GET.**

- [ ] **Step 4: Pausa de revisión.**

---

### Task 7: Treatment feature (con cálculo de withdrawals)

**Files:**
- Create: `backend/src/main/java/com/digitalcow/health/treatment/Treatment.java`
- Create: `backend/src/main/java/com/digitalcow/health/treatment/TreatmentRepository.java`
- Create: `backend/src/main/java/com/digitalcow/health/treatment/TreatmentService.java`
- Create: `backend/src/main/java/com/digitalcow/health/treatment/TreatmentController.java`
- Create: `backend/src/main/java/com/digitalcow/health/treatment/dto/...` (Create, Update, Response)
- Create: `backend/src/main/java/com/digitalcow/health/treatment/mapper/TreatmentMapper.java`
- Create: `backend/src/main/java/com/digitalcow/health/treatment/event/TreatmentChangedEvent.java`
- Create: `backend/src/main/java/com/digitalcow/health/treatment/AnimalTreatmentsController.java`

- [ ] **Step 1: Treatment entity** con campos del spec §2.2 (route enum `TreatmentRoute { IM, SC, IV, ORAL, TOPICAL, INTRAMAMMARY }`).

- [ ] **Step 2: TreatmentRepository**

```java
package com.digitalcow.health.treatment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TreatmentRepository extends JpaRepository<Treatment, Long>, JpaSpecificationExecutor<Treatment> {
    List<Treatment> findByAnimalIdOrderByStartedAtDesc(Long animalId);
    boolean existsByDiagnosisId(Long diagnosisId);

    @Query("SELECT t FROM Treatment t WHERE t.withdrawalMilkUntil >= :today ORDER BY t.withdrawalMilkUntil")
    List<Treatment> findActiveMilkWithdrawals(@Param("today") LocalDate today);

    @Query("SELECT t FROM Treatment t WHERE t.withdrawalMeatUntil >= :today ORDER BY t.withdrawalMeatUntil")
    List<Treatment> findActiveMeatWithdrawals(@Param("today") LocalDate today);
}
```

- [ ] **Step 3: TreatmentService con cálculo de withdrawal**

```java
package com.digitalcow.health.treatment;

import com.digitalcow.catalog.medication.Medication;
import com.digitalcow.catalog.medication.MedicationRepository;
import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.health.treatment.dto.*;
import com.digitalcow.health.treatment.event.TreatmentChangedEvent;
import com.digitalcow.health.treatment.mapper.TreatmentMapper;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de Treatment. Calcula withdrawal_milk_until y withdrawal_meat_until
 * basado en la medicacion catalogada.
 */
@Service
@Transactional
public class TreatmentService {

    private final TreatmentRepository repository;
    private final MedicationRepository medicationRepository;
    private final TreatmentMapper mapper;
    private final ApplicationEventPublisher events;

    public TreatmentService(TreatmentRepository repository,
                            MedicationRepository medicationRepository,
                            TreatmentMapper mapper,
                            ApplicationEventPublisher events) {
        this.repository = repository;
        this.medicationRepository = medicationRepository;
        this.mapper = mapper;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<TreatmentResponseDto> listByAnimal(Long animalId) {
        return repository.findByAnimalIdOrderByStartedAtDesc(animalId).stream()
            .map(this::toDto).toList();
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public TreatmentResponseDto create(TreatmentCreateDto dto) {
        Medication med = medicationRepository.findById(dto.medicationId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Medication not found"));
        Treatment t = mapper.fromCreate(dto);
        applyWithdrawals(t, med);
        Treatment saved = repository.save(t);
        events.publishEvent(new TreatmentChangedEvent(TenantContext.requireAccountId()));
        return toDto(saved);
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public TreatmentResponseDto update(Long id, TreatmentUpdateDto dto) {
        Treatment t = repository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Treatment not found"));
        mapper.applyUpdate(dto, t);
        Medication med = medicationRepository.findById(t.getMedicationId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Medication not found"));
        applyWithdrawals(t, med);
        events.publishEvent(new TreatmentChangedEvent(TenantContext.requireAccountId()));
        return toDto(t);
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        Treatment t = repository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Treatment not found"));
        repository.delete(t);
        events.publishEvent(new TreatmentChangedEvent(TenantContext.requireAccountId()));
    }

    /**
     * Setea withdrawal_milk_until y withdrawal_meat_until.
     * Base date = endedAt si existe, sino startedAt.
     */
    private void applyWithdrawals(Treatment t, Medication med) {
        LocalDate base = t.getEndedAt() != null ? t.getEndedAt() : t.getStartedAt();
        if (med.getWithdrawalMilkDays() > 0) {
            t.setWithdrawalMilkUntil(base.plusDays(med.getWithdrawalMilkDays()));
        } else {
            t.setWithdrawalMilkUntil(null);
        }
        if (med.getWithdrawalMeatDays() > 0) {
            t.setWithdrawalMeatUntil(base.plusDays(med.getWithdrawalMeatDays()));
        } else {
            t.setWithdrawalMeatUntil(null);
        }
    }

    private TreatmentResponseDto toDto(Treatment t) {
        Medication med = medicationRepository.findById(t.getMedicationId()).orElse(null);
        return mapper.toDto(t, med);
    }
}
```

- [ ] **Step 4: Controller**, endpoints estándar + `/api/v1/animals/{id}/treatments` helper.

- [ ] **Step 5: Pausa de revisión** — verificar que el cálculo de withdrawals es determinístico y se aplica también en update.

---

### Task 8: PestControl feature

**Files:**
- Create: paquete `backend/src/main/java/com/digitalcow/health/pestcontrol/` con entity, repo, service, controller, DTOs (create/update/response), mapper, event `PestControlChangedEvent`.

- [ ] **Step 1: Sigue el patrón de Vaccination (sin expansión por lote, sin next_dose calculation automática; el usuario ingresa `next_application_at` directamente).**

- [ ] **Step 2: Endpoints `/api/v1/health/pest-controls` CRUD.**

- [ ] **Step 3: Pausa de revisión.**

---

## Épica C — Plan sanitario backend

### Task 9: Migración V8 health_plan + assignments

**Files:**
- Create: `backend/src/main/resources/db/migration/V8__health_plan.sql`

- [ ] **Step 1: DDL del spec §2.3**

```sql
CREATE TABLE health_plan (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NULL,
  name VARCHAR(160) NOT NULL,
  description VARCHAR(500) NULL,
  applies_to_purpose ENUM('BEEF','DAIRY','DUAL','ANY') NOT NULL DEFAULT 'ANY',
  applies_to_sex ENUM('FEMALE','MALE','ANY') NOT NULL DEFAULT 'ANY',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_health_plan_account FOREIGN KEY (account_id) REFERENCES account(id),
  INDEX ix_health_plan_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE health_plan_step (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  health_plan_id BIGINT NOT NULL,
  step_order SMALLINT NOT NULL,
  name VARCHAR(160) NOT NULL,
  vaccine_id BIGINT NULL,
  age_months_min SMALLINT NULL,
  recurrence_months SMALLINT NULL,
  notes VARCHAR(400) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_step_plan FOREIGN KEY (health_plan_id) REFERENCES health_plan(id) ON DELETE CASCADE,
  CONSTRAINT fk_step_vaccine FOREIGN KEY (vaccine_id) REFERENCES vaccine(id),
  INDEX ix_step_plan_order (health_plan_id, step_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE animal_health_plan (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  health_plan_id BIGINT NOT NULL,
  animal_id BIGINT NULL,
  lot_id BIGINT NULL,
  assigned_at DATE NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_ahp_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_ahp_plan FOREIGN KEY (health_plan_id) REFERENCES health_plan(id),
  CONSTRAINT fk_ahp_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_ahp_lot FOREIGN KEY (lot_id) REFERENCES lot(id),
  INDEX ix_ahp_animal (account_id, animal_id),
  INDEX ix_ahp_lot (account_id, lot_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

Y seed de los 3 planes globales del spec §7.5:

```sql
INSERT INTO health_plan (id, account_id, name, description, applies_to_purpose, applies_to_sex) VALUES
  (1, NULL, 'Plan Estandar Lecheria', 'Calendario estandar para dairy', 'DAIRY', 'ANY'),
  (2, NULL, 'Plan Estandar Engorda', 'Calendario estandar para beef', 'BEEF', 'ANY'),
  (3, NULL, 'Plan Tropical Cebuino', 'Beef + Anthrax + garrapata periodica', 'ANY', 'ANY')
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO health_plan_step (health_plan_id, step_order, name, vaccine_id, age_months_min, recurrence_months) VALUES
  (1, 1, 'Brucella RB51 a 4 meses', (SELECT id FROM vaccine WHERE code='BRUCELLA_RB51'), 4, NULL),
  (1, 2, 'IBR/BVD/PI3/BRSV a 6 meses', (SELECT id FROM vaccine WHERE code='IBR_BVD_PI3_BRSV'), 6, 12),
  (1, 3, 'Leptospira a 6 meses', (SELECT id FROM vaccine WHERE code='LEPTOSPIRA_PENTAVALENTE'), 6, 6),
  (1, 4, 'Carbon sintomatico a 4 meses', (SELECT id FROM vaccine WHERE code='CARBON_SINTOMATICO'), 4, 12),
  (1, 5, 'Mastitis J5 preparto', (SELECT id FROM vaccine WHERE code='MASTITIS_J5'), 24, 6),
  (2, 1, 'IBR/BVD a 6 meses', (SELECT id FROM vaccine WHERE code='IBR_BVD_PI3_BRSV'), 6, 12),
  (2, 2, 'Carbon sintomatico a 4 meses', (SELECT id FROM vaccine WHERE code='CARBON_SINTOMATICO'), 4, 12),
  (2, 3, 'Pasteurella a 6 meses', (SELECT id FROM vaccine WHERE code='PASTEURELLA'), 6, 12),
  (3, 1, 'IBR/BVD a 6 meses', (SELECT id FROM vaccine WHERE code='IBR_BVD_PI3_BRSV'), 6, 12),
  (3, 2, 'Carbon sintomatico a 4 meses', (SELECT id FROM vaccine WHERE code='CARBON_SINTOMATICO'), 4, 12),
  (3, 3, 'Anthrax anual', (SELECT id FROM vaccine WHERE code='ANTHRAX'), 6, 12)
ON DUPLICATE KEY UPDATE name=VALUES(name);
```

- [ ] **Step 2: Pausa de revisión** — verificar FKs y seeds.

---

### Task 10: HealthPlan + Step + Assignment (entities/repos/service/controller)

**Files:**
- Create: paquete `backend/src/main/java/com/digitalcow/health/plan/` con:
  - `HealthPlan.java` (entity, multi-tenant via @Filter pero con account_id nullable; cuando es NULL = global)
  - `HealthPlanStep.java` (entity, sin account_id propio; pertenece al plan)
  - `AnimalHealthPlan.java` (assignment)
  - `HealthPlanRepository`, `HealthPlanStepRepository`, `AnimalHealthPlanRepository`
  - `HealthPlanService` con métodos: `list()`, `get(id)`, `create(dto)`, `update(id, dto)`, `delete(id)`, `addStep(planId, dto)`, `updateStep(stepId, dto)`, `deleteStep(stepId)`, `assign(planId, dto)`, `unassign(assignmentId)`
  - `HealthPlanController` con endpoints del spec §3.3
  - DTOs en `dto/`
  - Mapper

- [ ] **Step 1: HealthPlan entity** con `account_id` nullable y `@Filter` que matchea `account_id = :accountId OR account_id IS NULL` (planes globales visibles para todos):

```java
@Filter(name = "accountFilter", condition = "account_id = :accountId OR account_id IS NULL")
```

- [ ] **Step 2: HealthPlanService**

  - `update` y `delete` deben rechazar (403) intentos sobre planes globales (account_id = null). Lanzar `BusinessException(ErrorCode.FORBIDDEN, "Cannot modify global plan")`.
  - `addStep`, `updateStep`, `deleteStep` igualmente bloqueados para planes globales.
  - `assign`: acepta `animalIds: List<Long>` o `lotIds: List<Long>`. Crea filas en `animal_health_plan`. accountId lo setea TenantAwareEntityListener.
  - `delete`: falla si tiene assignments.

- [ ] **Step 3: Controller** con los endpoints listados en spec §3.3.

- [ ] **Step 4: Pausa de revisión.**

---

## Épica D — Alertas y dashboard backend

### Task 11: HealthAlertsService + endpoint

**Files:**
- Create: `backend/src/main/java/com/digitalcow/health/alerts/HealthAlertsService.java`
- Create: `backend/src/main/java/com/digitalcow/health/alerts/HealthAlertsController.java`
- Create: `backend/src/main/java/com/digitalcow/health/alerts/dto/HealthAlertsDto.java`
- Create: `backend/src/main/java/com/digitalcow/health/alerts/dto/AlertItemDto.java`

- [ ] **Step 1: AlertItemDto y HealthAlertsDto**

```java
package com.digitalcow.health.alerts.dto;

import java.time.LocalDate;

/** Item generico de alerta. */
public record AlertItemDto(
    String type,         // ej: "UPCOMING_VACCINATION"
    Long animalId,
    String animalTag,
    String label,        // texto display ya armado por backend
    LocalDate date,
    Long relatedId       // id de vaccination/treatment/diagnosis segun corresponda
) { }
```

```java
package com.digitalcow.health.alerts.dto;

import java.util.List;

public record HealthAlertsDto(
    List<AlertItemDto> upcomingVaccinations7d,
    List<AlertItemDto> upcomingVaccinations30d,
    List<AlertItemDto> withdrawalActiveMilk,
    List<AlertItemDto> withdrawalActiveMeat,
    List<AlertItemDto> activeDiagnosesWithoutTreatment
) { }
```

(El cálculo de "missingMandatoryVaccinations" del spec §3.4 queda fuera de Fase 2 inicial: requiere correlacionar `animal_health_plan` + `health_plan_step` + `vaccination`. Se deja como TODO documentado en una constante `// FUTURE: missing mandatory vaccinations` en el service. Devuelve siempre lista vacía por ahora.)

- [ ] **Step 2: HealthAlertsService**

```java
package com.digitalcow.health.alerts;

import com.digitalcow.animal.AnimalRepository;
import com.digitalcow.health.alerts.dto.*;
import com.digitalcow.health.diagnosis.DiagnosisRepository;
import com.digitalcow.health.treatment.TreatmentRepository;
import com.digitalcow.health.vaccination.VaccinationRepository;
import com.digitalcow.health.diagnosis.event.DiagnosisChangedEvent;
import com.digitalcow.health.treatment.event.TreatmentChangedEvent;
import com.digitalcow.health.vaccination.event.VaccinationChangedEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** Calculo de alertas sanitarias. Cacheable 5 min. */
@Service
@Transactional(readOnly = true)
public class HealthAlertsService {

    private final VaccinationRepository vaccinationRepository;
    private final TreatmentRepository treatmentRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final AnimalRepository animalRepository;

    public HealthAlertsService(VaccinationRepository vaccinationRepository,
                               TreatmentRepository treatmentRepository,
                               DiagnosisRepository diagnosisRepository,
                               AnimalRepository animalRepository) {
        this.vaccinationRepository = vaccinationRepository;
        this.treatmentRepository = treatmentRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.animalRepository = animalRepository;
    }

    @Cacheable(value = "health-alerts", keyGenerator = "tenantKeyGenerator")
    public HealthAlertsDto build() {
        LocalDate today = LocalDate.now();
        LocalDate in7 = today.plusDays(7);
        LocalDate in30 = today.plusDays(30);

        List<AlertItemDto> up7 = vaccinationRepository.findUpcomingDoses(today, in7).stream()
            .map(v -> toVacAlert(v, "UPCOMING_VACCINATION")).toList();

        List<AlertItemDto> up30 = vaccinationRepository.findUpcomingDoses(today, in30).stream()
            .map(v -> toVacAlert(v, "UPCOMING_VACCINATION")).toList();

        List<AlertItemDto> milk = treatmentRepository.findActiveMilkWithdrawals(today).stream()
            .map(t -> toTreatAlert(t, t.getWithdrawalMilkUntil(), "WITHDRAWAL_MILK")).toList();

        List<AlertItemDto> meat = treatmentRepository.findActiveMeatWithdrawals(today).stream()
            .map(t -> toTreatAlert(t, t.getWithdrawalMeatUntil(), "WITHDRAWAL_MEAT")).toList();

        List<AlertItemDto> diag = diagnosisRepository.findActiveWithoutTreatment().stream()
            .map(d -> new AlertItemDto(
                "ACTIVE_DIAGNOSIS_NO_TREATMENT",
                d.getAnimalId(),
                animalTag(d.getAnimalId()),
                "Sin tratamiento",
                d.getDiagnosedAt(),
                d.getId()
            )).toList();

        return new HealthAlertsDto(up7, up30, milk, meat, diag);
    }

    @EventListener
    @CacheEvict(value = "health-alerts", allEntries = true)
    public void onVaccination(VaccinationChangedEvent event) { /* invalidate */ }

    @EventListener
    @CacheEvict(value = "health-alerts", allEntries = true)
    public void onTreatment(TreatmentChangedEvent event) { /* invalidate */ }

    @EventListener
    @CacheEvict(value = "health-alerts", allEntries = true)
    public void onDiagnosis(DiagnosisChangedEvent event) { /* invalidate */ }

    private AlertItemDto toVacAlert(com.digitalcow.health.vaccination.Vaccination v, String type) {
        return new AlertItemDto(type, v.getAnimalId(), animalTag(v.getAnimalId()),
            "Proxima dosis", v.getNextDoseDue(), v.getId());
    }

    private AlertItemDto toTreatAlert(com.digitalcow.health.treatment.Treatment t, LocalDate until, String type) {
        return new AlertItemDto(type, t.getAnimalId(), animalTag(t.getAnimalId()),
            "Retiro activo", until, t.getId());
    }

    private String animalTag(Long animalId) {
        return animalId == null ? "" : animalRepository.findById(animalId).map(a -> a.getInternalTag()).orElse("");
    }
}
```

Necesitas:
- `DiagnosisRepository.findActiveWithoutTreatment()` definido como `@Query("SELECT d FROM Diagnosis d WHERE d.status = 'ACTIVE' AND NOT EXISTS (SELECT 1 FROM Treatment t WHERE t.diagnosisId = d.id)")`
- `tenantKeyGenerator` bean (creado en Fase 1 si existe; si no, agregarlo en `CacheConfig` como:

```java
@Bean
public KeyGenerator tenantKeyGenerator() {
    return (target, method, params) -> com.digitalcow.tenancy.TenantContext.getAccountId();
}
```

- [ ] **Step 3: HealthAlertsController**

```java
package com.digitalcow.health.alerts;

import com.digitalcow.health.alerts.dto.HealthAlertsDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health/alerts")
public class HealthAlertsController {

    private final HealthAlertsService service;

    public HealthAlertsController(HealthAlertsService service) {
        this.service = service;
    }

    @GetMapping
    public HealthAlertsDto get() {
        return service.build();
    }
}
```

- [ ] **Step 4: Pausa de revisión.**

---

### Task 12: DashboardHealth endpoint

**Files:**
- Create: `backend/src/main/java/com/digitalcow/dashboard/DashboardHealthController.java`
- Create: `backend/src/main/java/com/digitalcow/dashboard/DashboardHealthService.java`
- Create: `backend/src/main/java/com/digitalcow/dashboard/dto/DashboardHealthDto.java`

- [ ] **Step 1: DashboardHealthDto record**

```java
package com.digitalcow.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardHealthDto(
    long upcomingVaccinations7d,
    long upcomingVaccinations30d,
    long activeDiagnoses,
    long treatmentsActiveCount,
    BigDecimal monthVetSpend,
    List<TopDiseaseDto> topDiseasesQuarter
) {
    public record TopDiseaseDto(String diseaseCode, String name, long count) { }
}
```

- [ ] **Step 2: DashboardHealthService**

Implementa con queries agregadas:
- `upcomingVaccinations7d`: `COUNT(*) FROM vaccination WHERE next_dose_due BETWEEN today AND today+7`
- `activeDiagnoses`: `COUNT(*) FROM diagnosis WHERE status='ACTIVE'`
- `treatmentsActiveCount`: `COUNT(*) FROM treatment WHERE ended_at IS NULL`
- `monthVetSpend`: UNION ALL como en spec §5.3
- `topDiseasesQuarter`: `SELECT disease_code, COUNT(*) FROM diagnosis JOIN disease GROUP BY disease_code ORDER BY count DESC LIMIT 5`

Cache `dashboard-health` con tenantKeyGenerator, 60 segundos. Invalidación con `@EventListener` igual que alerts.

- [ ] **Step 3: Controller `/api/v1/dashboard/health` GET.**

- [ ] **Step 4: Registrar cache "dashboard-health" en CacheConfig.**

- [ ] **Step 5: Pausa de revisión.**

---

## Épica E — Frontend foundation (i18n, sidebar, rutas)

### Task 13: i18n nuevos namespaces y sidebar entries

**Files:**
- Create: `frontend/public/locales/es/health.json`
- Create: `frontend/public/locales/en/health.json`
- Create: `frontend/public/locales/es/catalog.json`
- Create: `frontend/public/locales/en/catalog.json`
- Create: `frontend/public/locales/es/alerts.json`
- Create: `frontend/public/locales/en/alerts.json`
- Modify: `frontend/src/lib/i18n.ts` (agregar namespaces a la lista)
- Modify: `frontend/src/app/AppLayout.tsx` (agregar sección Salud al sidebar)

- [ ] **Step 1: Crear JSONs ES (es/health.json)**

```json
{
  "nav": {
    "section": "Salud",
    "overview": "Resumen",
    "vaccinations": "Vacunaciones",
    "diagnoses": "Diagnosticos",
    "treatments": "Tratamientos",
    "pestControls": "Control de plagas",
    "vetVisits": "Visitas veterinarias",
    "plans": "Planes sanitarios"
  },
  "vaccination": {
    "title": "Vacunaciones",
    "new": "Nueva vacunacion",
    "newBulk": "Vacunacion por lote",
    "vaccine": "Vacuna",
    "batch": "Lote del laboratorio",
    "appliedAt": "Aplicada el",
    "doseMl": "Dosis (mL)",
    "route": "Via",
    "nextDose": "Proxima dosis",
    "cost": "Costo",
    "vetVisit": "Visita veterinaria",
    "lot": "Lote",
    "individual": "Individual",
    "byLot": "Por lote",
    "expanded": "Se crearon {{count}} registros (uno por animal activo del lote)"
  },
  "diagnosis": {
    "title": "Diagnosticos",
    "new": "Nuevo diagnostico",
    "disease": "Enfermedad",
    "diagnosedAt": "Diagnosticado el",
    "severity": "Severidad",
    "symptoms": "Sintomas",
    "status": "Estado",
    "resolvedAt": "Resuelto el"
  },
  "treatment": {
    "title": "Tratamientos",
    "new": "Nuevo tratamiento",
    "medication": "Medicamento",
    "diagnosis": "Diagnostico relacionado",
    "startedAt": "Inicio",
    "endedAt": "Termino",
    "dose": "Dosis",
    "dosesCount": "Numero de aplicaciones",
    "route": "Via",
    "withdrawalMilk": "Retiro de leche hasta",
    "withdrawalMeat": "Retiro de carne hasta",
    "withdrawalActive": "Retiro activo",
    "prescribedBy": "Prescrito por"
  },
  "pest": {
    "title": "Control de plagas",
    "new": "Nuevo control",
    "pest": "Plaga",
    "product": "Producto utilizado",
    "appliedAt": "Aplicado el",
    "nextApplication": "Proxima aplicacion"
  },
  "visit": {
    "title": "Visitas veterinarias",
    "new": "Nueva visita",
    "vetName": "Veterinario",
    "vetContact": "Contacto",
    "reason": "Motivo",
    "totalCost": "Costo total",
    "events": "Eventos asociados"
  },
  "plan": {
    "title": "Planes sanitarios",
    "new": "Nuevo plan",
    "global": "Plan global del sistema",
    "appliesToPurpose": "Aplica a proposito",
    "appliesToSex": "Aplica a sexo",
    "steps": "Pasos",
    "addStep": "Agregar paso",
    "ageMonthsMin": "Edad minima (meses)",
    "recurrenceMonths": "Recurrencia (meses)",
    "assign": "Asignar a animales/lotes"
  },
  "tab": {
    "health": "Salud",
    "timeline": "Linea de tiempo",
    "print": "Vista imprimible"
  }
}
```

`es/catalog.json`:
```json
{
  "vaccines": "Vacunas",
  "diseases": "Enfermedades",
  "medications": "Medicamentos",
  "pests": "Plagas y parasitos",
  "code": "Codigo",
  "name": "Nombre",
  "category": "Categoria",
  "zoonotic": "Zoonotica",
  "severity": "Severidad",
  "activeIngredient": "Principio activo",
  "withdrawalMilkDays": "Retiro leche (dias)",
  "withdrawalMeatDays": "Retiro carne (dias)",
  "scientificName": "Nombre cientifico",
  "type": "Tipo",
  "region": "Region"
}
```

`es/alerts.json`:
```json
{
  "title": "Alertas",
  "upcoming7d": "Vacunaciones en 7 dias",
  "upcoming30d": "Vacunaciones en 30 dias",
  "withdrawalMilk": "Retiro de leche activo",
  "withdrawalMeat": "Retiro de carne activo",
  "diagnosesNoTreatment": "Diagnosticos activos sin tratamiento",
  "noAlerts": "Sin alertas",
  "animal": "Animal",
  "until": "hasta",
  "dueIn": "Vence en"
}
```

- [ ] **Step 2: Crear las versiones EN (en/health.json, en/catalog.json, en/alerts.json)** con todas las keys traducidas a inglés. Mantener la misma estructura.

- [ ] **Step 3: Modificar `frontend/src/lib/i18n.ts`** para que `ns` incluya los nuevos namespaces:

```ts
ns: ['common', 'auth', 'animals', 'dashboard', 'team', 'ranches', 'errors', 'health', 'catalog', 'alerts'],
```

(Mantener la lista existente de Fase 1, sólo agregar `'health', 'catalog', 'alerts'` al final.)

- [ ] **Step 4: Modificar AppLayout.tsx** para agregar la sección "Salud" en el sidebar:

```tsx
{/* Section: Salud */}
<div className="px-3 py-2 text-xs font-semibold uppercase text-muted-foreground">
  {t('health:nav.section')}
</div>
<NavLink to="/health" className={navClass}>
  <Activity className="h-4 w-4" /> {t('health:nav.overview')}
</NavLink>
<NavLink to="/health/vaccinations" className={navClass}>
  <Syringe className="h-4 w-4" /> {t('health:nav.vaccinations')}
</NavLink>
<NavLink to="/health/diagnoses" className={navClass}>
  <Stethoscope className="h-4 w-4" /> {t('health:nav.diagnoses')}
</NavLink>
<NavLink to="/health/treatments" className={navClass}>
  <Pill className="h-4 w-4" /> {t('health:nav.treatments')}
</NavLink>
<NavLink to="/health/pest-controls" className={navClass}>
  <Bug className="h-4 w-4" /> {t('health:nav.pestControls')}
</NavLink>
<NavLink to="/health/vet-visits" className={navClass}>
  <CalendarDays className="h-4 w-4" /> {t('health:nav.vetVisits')}
</NavLink>
<NavLink to="/health/plans" className={navClass}>
  <ClipboardList className="h-4 w-4" /> {t('health:nav.plans')}
</NavLink>
```

Importar iconos: `import { Activity, Syringe, Stethoscope, Pill, Bug, CalendarDays, ClipboardList } from 'lucide-react'`.

- [ ] **Step 5: Pausa de revisión** — verificar que el sidebar muestra la nueva sección y los namespaces cargan.

---

### Task 14: API hooks de catálogos (TanStack Query)

**Files:**
- Create: `frontend/src/features/catalog/api/vaccines.ts`
- Create: `frontend/src/features/catalog/api/diseases.ts`
- Create: `frontend/src/features/catalog/api/medications.ts`
- Create: `frontend/src/features/catalog/api/pests.ts`
- Create: `frontend/src/features/catalog/types.ts`

- [ ] **Step 1: types.ts**

```ts
/**
 * Tipos del catalogo sanitario, alineados con DTOs del backend.
 */
export type VaccineRoute = 'IM' | 'SC' | 'ORAL' | 'INTRANASAL' | 'TOPICAL';

export interface Vaccine {
  id: number;
  code: string;
  nameEs: string;
  nameEn: string;
  targetDiseases?: string | null;
  defaultDoseMl?: number | null;
  route?: VaccineRoute | null;
  recommendedAgeMonths?: number | null;
  recommendedFrequencyMonths?: number | null;
}

export type DiseaseCategory = 'BACTERIAL' | 'VIRAL' | 'PARASITIC' | 'METABOLIC' | 'NUTRITIONAL' | 'MECHANICAL' | 'OTHER';
export type DiseaseSeverity = 'LOW' | 'MEDIUM' | 'HIGH';

export interface Disease {
  id: number;
  code: string;
  nameEs: string;
  nameEn: string;
  category: DiseaseCategory;
  zoonotic: boolean;
  severity: DiseaseSeverity;
  defaultSymptoms?: string | null;
}

export type MedicationRoute = 'IM' | 'SC' | 'IV' | 'ORAL' | 'TOPICAL' | 'INTRAMAMMARY';

export interface Medication {
  id: number;
  code: string;
  nameEs: string;
  nameEn: string;
  activeIngredient?: string | null;
  defaultDose?: string | null;
  defaultRoute?: MedicationRoute | null;
  withdrawalMilkDays: number;
  withdrawalMeatDays: number;
  notes?: string | null;
}

export type PestType = 'TICK' | 'FLY' | 'WORM' | 'LICE' | 'MITE' | 'OTHER';
export type PestRegion = 'TROPICAL' | 'TEMPERATE' | 'ANY';

export interface Pest {
  id: number;
  code: string;
  nameEs: string;
  nameEn: string;
  scientificName?: string | null;
  type: PestType;
  region: PestRegion;
  notes?: string | null;
}
```

- [ ] **Step 2: vaccines.ts (template para los demás)**

```ts
import { useQuery } from '@tanstack/react-query';
import { http } from '@/lib/http';
import type { Vaccine } from '../types';

/**
 * Lista todas las vacunas del catalogo. Cacheable largo (no cambian con frecuencia).
 */
export function useVaccines() {
  return useQuery({
    queryKey: ['catalog', 'vaccines'],
    queryFn: async () => {
      const { data } = await http.get<Vaccine[]>('/catalog/vaccines');
      return data;
    },
    staleTime: 1000 * 60 * 60,  // 1 hora
  });
}
```

Mismo patrón para `diseases.ts`, `medications.ts`, `pests.ts` cambiando endpoint y tipo.

- [ ] **Step 3: Helper para elegir name según locale**

Agregar a `frontend/src/lib/i18n.ts` (o crear `frontend/src/lib/catalog.ts`):

```ts
/**
 * Devuelve el nombre del item de catalogo segun el locale actual.
 * Acepta cualquier objeto con nameEs y nameEn.
 */
export function localizedName(item: { nameEs: string; nameEn: string }, locale: string): string {
  return locale.startsWith('en') ? item.nameEn : item.nameEs;
}
```

- [ ] **Step 4: Pausa de revisión.**

---

## Épica F — Frontend páginas de eventos

### Task 15: VetVisit feature (lista, form, detalle)

**Files:**
- Create: `frontend/src/features/health/vetVisits/api.ts`
- Create: `frontend/src/features/health/vetVisits/schemas.ts`
- Create: `frontend/src/features/health/vetVisits/types.ts`
- Create: `frontend/src/features/health/vetVisits/components/VetVisitForm.tsx`
- Create: `frontend/src/pages/health/VetVisitsPage.tsx`
- Modify: `frontend/src/app/router.tsx` (agregar ruta `/health/vet-visits`)

- [ ] **Step 1: types.ts**

```ts
export interface VetVisit {
  id: number;
  ranchId: number;
  visitedAt: string;
  vetName: string;
  vetContact?: string | null;
  reason: string;
  totalCost?: number | null;
  notes?: string | null;
}

export interface VetVisitCreate {
  ranchId: number;
  visitedAt: string;
  vetName: string;
  vetContact?: string;
  reason: string;
  totalCost?: number;
  notes?: string;
}
```

- [ ] **Step 2: schemas.ts**

```ts
import { z } from 'zod';

export const vetVisitCreateSchema = z.object({
  ranchId: z.number().int().positive(),
  visitedAt: z.string().min(1),
  vetName: z.string().min(1).max(160),
  vetContact: z.string().max(160).optional(),
  reason: z.string().min(1).max(300),
  totalCost: z.number().nonnegative().optional(),
  notes: z.string().optional(),
});

export type VetVisitCreateInput = z.infer<typeof vetVisitCreateSchema>;
```

- [ ] **Step 3: api.ts**

```ts
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { http } from '@/lib/http';
import type { VetVisit, VetVisitCreate } from './types';

const QK = ['health', 'vet-visits'] as const;

/** Lista visitas veterinarias. */
export function useVetVisits() {
  return useQuery({
    queryKey: QK,
    queryFn: async () => (await http.get<VetVisit[]>('/health/vet-visits')).data,
  });
}

/** Crea una visita veterinaria. */
export function useCreateVetVisit() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (body: VetVisitCreate) =>
      (await http.post<VetVisit>('/health/vet-visits', body)).data,
    onSuccess: () => qc.invalidateQueries({ queryKey: QK }),
  });
}

/** Actualiza una visita. */
export function useUpdateVetVisit() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, body }: { id: number; body: Partial<VetVisitCreate> }) =>
      (await http.patch<VetVisit>(`/health/vet-visits/${id}`, body)).data,
    onSuccess: () => qc.invalidateQueries({ queryKey: QK }),
  });
}

/** Borra una visita. */
export function useDeleteVetVisit() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => (await http.delete(`/health/vet-visits/${id}`)).data,
    onSuccess: () => qc.invalidateQueries({ queryKey: QK }),
  });
}
```

- [ ] **Step 4: VetVisitForm.tsx**

```tsx
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useRanches } from '@/features/ranches/api';
import { vetVisitCreateSchema, type VetVisitCreateInput } from '../schemas';

interface Props {
  defaultValues?: Partial<VetVisitCreateInput>;
  onSubmit: (data: VetVisitCreateInput) => void;
  submitting?: boolean;
}

/**
 * Formulario de visita veterinaria. Carga la lista de ranchos del usuario.
 */
export function VetVisitForm({ defaultValues, onSubmit, submitting }: Props) {
  const { t } = useTranslation(['health', 'common']);
  const ranches = useRanches();
  const { register, handleSubmit, formState: { errors } } = useForm<VetVisitCreateInput>({
    resolver: zodResolver(vetVisitCreateSchema),
    defaultValues,
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div>
        <Label htmlFor="ranchId">{t('common:ranch')}</Label>
        <select id="ranchId" {...register('ranchId', { valueAsNumber: true })} className="w-full border rounded h-10 px-2">
          <option value="">--</option>
          {ranches.data?.map(r => <option key={r.id} value={r.id}>{r.name}</option>)}
        </select>
        {errors.ranchId && <p className="text-destructive text-sm">{errors.ranchId.message}</p>}
      </div>
      <div>
        <Label htmlFor="visitedAt">{t('health:visit.title')}</Label>
        <Input id="visitedAt" type="date" {...register('visitedAt')} />
      </div>
      <div>
        <Label htmlFor="vetName">{t('health:visit.vetName')}</Label>
        <Input id="vetName" {...register('vetName')} />
      </div>
      <div>
        <Label htmlFor="vetContact">{t('health:visit.vetContact')}</Label>
        <Input id="vetContact" {...register('vetContact')} />
      </div>
      <div>
        <Label htmlFor="reason">{t('health:visit.reason')}</Label>
        <Input id="reason" {...register('reason')} />
      </div>
      <div>
        <Label htmlFor="totalCost">{t('health:visit.totalCost')}</Label>
        <Input id="totalCost" type="number" step="0.01" {...register('totalCost', { valueAsNumber: true })} />
      </div>
      <Button type="submit" disabled={submitting}>{t('common:actions.save')}</Button>
    </form>
  );
}
```

- [ ] **Step 5: VetVisitsPage.tsx**

```tsx
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Plus } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { useCreateVetVisit, useVetVisits } from '@/features/health/vetVisits/api';
import { VetVisitForm } from '@/features/health/vetVisits/components/VetVisitForm';

/**
 * Pagina de listado de visitas veterinarias. Modal para nueva visita.
 */
export default function VetVisitsPage() {
  const { t } = useTranslation(['health', 'common']);
  const visits = useVetVisits();
  const create = useCreateVetVisit();
  const [open, setOpen] = useState(false);

  return (
    <div className="p-6 space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">{t('health:visit.title')}</h1>
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger asChild>
            <Button><Plus className="h-4 w-4 mr-2" />{t('health:visit.new')}</Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader><DialogTitle>{t('health:visit.new')}</DialogTitle></DialogHeader>
            <VetVisitForm
              submitting={create.isPending}
              onSubmit={async (data) => { await create.mutateAsync(data); setOpen(false); }}
            />
          </DialogContent>
        </Dialog>
      </div>
      <table className="w-full border rounded">
        <thead><tr className="bg-muted">
          <th className="p-2 text-left">{t('health:visit.title')}</th>
          <th className="p-2 text-left">{t('health:visit.vetName')}</th>
          <th className="p-2 text-left">{t('health:visit.reason')}</th>
          <th className="p-2 text-right">{t('health:visit.totalCost')}</th>
        </tr></thead>
        <tbody>
          {visits.data?.map(v => (
            <tr key={v.id} className="border-t">
              <td className="p-2">{v.visitedAt}</td>
              <td className="p-2">{v.vetName}</td>
              <td className="p-2">{v.reason}</td>
              <td className="p-2 text-right">{v.totalCost ?? '-'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

- [ ] **Step 6: Registrar ruta en router.tsx**

Agregar dentro del bloque de rutas autenticadas:

```tsx
<Route path="/health/vet-visits" element={<VetVisitsPage />} />
```

Importar `VetVisitsPage` arriba.

- [ ] **Step 7: Pausa de revisión.**

---

### Task 16: Vaccination feature (lista, form individual y bulk)

**Files:**
- Create: `frontend/src/features/health/vaccinations/{api.ts, schemas.ts, types.ts, components/VaccinationForm.tsx, components/VaccinationBulkForm.tsx}`
- Create: `frontend/src/pages/health/VaccinationsPage.tsx`
- Modify: `frontend/src/app/router.tsx`

- [ ] **Step 1: Mismo patrón que VetVisit.** Hooks: `useVaccinations`, `useCreateVaccination`, `useCreateVaccinationBulk`, `useUpdateVaccination`, `useDeleteVaccination`. Hook adicional `useAnimalVaccinations(animalId)` para llamar a `/animals/{id}/vaccinations`.

- [ ] **Step 2: VaccinationForm individual** con selectores: animal (autocomplete sobre `useAnimals()`), vacuna (select sobre `useVaccines()`), fecha, batch, dose, route, cost, vetVisit (opcional dropdown sobre `useVetVisits()`).

- [ ] **Step 3: VaccinationBulkForm** con selector de lote (sobre `useRanches()` y `useLots(ranchId)`) + vacuna + fecha + batch + dose + route + cost. Al submit, llama `useCreateVaccinationBulk()` y muestra toast con `t('health:vaccination.expanded', { count: response.length })`.

- [ ] **Step 4: VaccinationsPage** con tabs "Individual" / "Por lote" para alternar entre los dos formularios, y tabla de vacunaciones recientes con filtro por animal.

- [ ] **Step 5: Ruta `/health/vaccinations`.**

- [ ] **Step 6: Pausa de revisión.**

---

### Task 17: Diagnosis, Treatment y PestControl features (CRUD frontend)

**Files:**
- Create: paquetes simétricos para `diagnoses/`, `treatments/`, `pestControls/` en `frontend/src/features/health/` y páginas correspondientes en `frontend/src/pages/health/`.
- Modify: `frontend/src/app/router.tsx` agregando 3 rutas.

- [ ] **Step 1: Cada feature sigue el patrón de VetVisit/Vaccination.**

- [ ] **Step 2: TreatmentForm** muestra cálculo en tiempo real del withdrawal: cuando el usuario selecciona una medicación, lee `withdrawalMilkDays` y `withdrawalMeatDays` y muestra el resultado de `startedAt + N días` como ayuda visual (no se envía al backend; el backend lo calcula).

```tsx
const medicationId = watch('medicationId');
const startedAt = watch('startedAt');
const med = medications.data?.find(m => m.id === Number(medicationId));
const milkUntil = med && startedAt && med.withdrawalMilkDays > 0
  ? new Date(new Date(startedAt).getTime() + med.withdrawalMilkDays * 86400000).toISOString().slice(0,10)
  : null;
const meatUntil = med && startedAt && med.withdrawalMeatDays > 0
  ? new Date(new Date(startedAt).getTime() + med.withdrawalMeatDays * 86400000).toISOString().slice(0,10)
  : null;
```

Render:
```tsx
{med && (
  <div className="text-sm text-muted-foreground">
    {milkUntil && <p>{t('health:treatment.withdrawalMilk')}: {milkUntil}</p>}
    {meatUntil && <p>{t('health:treatment.withdrawalMeat')}: {meatUntil}</p>}
  </div>
)}
```

- [ ] **Step 3: Rutas `/health/diagnoses`, `/health/treatments`, `/health/pest-controls`.**

- [ ] **Step 4: Pausa de revisión.**

---

## Épica G — Plan sanitario frontend

### Task 18: HealthPlan feature (lista, editor con steps, asignación)

**Files:**
- Create: `frontend/src/features/health/plans/{api.ts, schemas.ts, types.ts, components/HealthPlanEditor.tsx, components/HealthPlanStepsList.tsx, components/AssignPlanDialog.tsx}`
- Create: `frontend/src/pages/health/HealthPlansPage.tsx`
- Modify: `frontend/src/app/router.tsx`

- [ ] **Step 1: types.ts y schemas.ts** con HealthPlan, HealthPlanStep, AnimalHealthPlan.

- [ ] **Step 2: api.ts** con hooks de plans, steps, assignments.

- [ ] **Step 3: HealthPlansPage**: tabla con planes (icono de candado para planes globales no editables), botón "Nuevo plan" abre dialog con form simple (name, description, appliesToPurpose, appliesToSex). Click en una fila navega a editor.

- [ ] **Step 4: HealthPlanEditor**: pantalla full con metadatos del plan arriba y lista de steps (HealthPlanStepsList) abajo. Botón "Agregar paso" abre dialog con form (stepOrder auto-incrementado, name, vaccine selector, ageMonthsMin, recurrenceMonths, notes). Reordenamiento de steps con flechas arriba/abajo (sin drag&drop para no añadir dependencias).

- [ ] **Step 5: AssignPlanDialog**: tab "Animales" o "Lotes", multi-select. POST a `/health/plans/{id}/assign`.

- [ ] **Step 6: Ruta `/health/plans` y `/health/plans/:id`.**

- [ ] **Step 7: Pausa de revisión.**

---

## Épica H — Frontend dashboard + alertas + tab animal

### Task 19: HealthOverviewPage `/health` con alertas

**Files:**
- Create: `frontend/src/features/health/alerts/api.ts`
- Create: `frontend/src/features/health/alerts/components/AlertsList.tsx`
- Create: `frontend/src/pages/health/HealthOverviewPage.tsx`
- Modify: `frontend/src/app/router.tsx`

- [ ] **Step 1: alerts/api.ts**

```ts
import { useQuery } from '@tanstack/react-query';
import { http } from '@/lib/http';

export interface AlertItem {
  type: string;
  animalId?: number;
  animalTag?: string;
  label: string;
  date: string;
  relatedId?: number;
}

export interface HealthAlerts {
  upcomingVaccinations7d: AlertItem[];
  upcomingVaccinations30d: AlertItem[];
  withdrawalActiveMilk: AlertItem[];
  withdrawalActiveMeat: AlertItem[];
  activeDiagnosesWithoutTreatment: AlertItem[];
}

export function useHealthAlerts() {
  return useQuery({
    queryKey: ['health', 'alerts'],
    queryFn: async () => (await http.get<HealthAlerts>('/health/alerts')).data,
    staleTime: 1000 * 60, // 1 min
  });
}
```

- [ ] **Step 2: AlertsList component**

```tsx
import { useTranslation } from 'react-i18next';
import type { AlertItem } from '@/features/health/alerts/api';

interface Props {
  title: string;
  items: AlertItem[];
  emptyKey: string;
}

/** Renderiza una lista compacta de alertas o el placeholder vacio. */
export function AlertsList({ title, items, emptyKey }: Props) {
  const { t } = useTranslation(['alerts']);
  return (
    <section className="border rounded p-4">
      <h3 className="font-semibold mb-2">{title}</h3>
      {items.length === 0 ? (
        <p className="text-sm text-muted-foreground">{t(emptyKey)}</p>
      ) : (
        <ul className="space-y-1 text-sm">
          {items.map((item, i) => (
            <li key={i} className="flex justify-between border-b last:border-0 py-1">
              <span>{item.animalTag ? `${t('alerts:animal')} ${item.animalTag}` : '-'} - {item.label}</span>
              <span className="text-muted-foreground">{item.date}</span>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
```

- [ ] **Step 3: HealthOverviewPage**

```tsx
import { useTranslation } from 'react-i18next';
import { useHealthAlerts } from '@/features/health/alerts/api';
import { AlertsList } from '@/features/health/alerts/components/AlertsList';

export default function HealthOverviewPage() {
  const { t } = useTranslation(['health', 'alerts']);
  const alerts = useHealthAlerts();
  const d = alerts.data;

  return (
    <div className="p-6 space-y-6">
      <h1 className="text-2xl font-bold">{t('alerts:title')}</h1>
      {alerts.isLoading ? <p>{t('common:loading')}</p> : d && (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          <AlertsList title={t('alerts:upcoming7d')} items={d.upcomingVaccinations7d} emptyKey="alerts:noAlerts" />
          <AlertsList title={t('alerts:upcoming30d')} items={d.upcomingVaccinations30d} emptyKey="alerts:noAlerts" />
          <AlertsList title={t('alerts:withdrawalMilk')} items={d.withdrawalActiveMilk} emptyKey="alerts:noAlerts" />
          <AlertsList title={t('alerts:withdrawalMeat')} items={d.withdrawalActiveMeat} emptyKey="alerts:noAlerts" />
          <AlertsList title={t('alerts:diagnosesNoTreatment')} items={d.activeDiagnosesWithoutTreatment} emptyKey="alerts:noAlerts" />
        </div>
      )}
    </div>
  );
}
```

- [ ] **Step 4: Ruta `/health` registrada.**

- [ ] **Step 5: Pausa de revisión.**

---

### Task 20: Dashboard widgets de salud

**Files:**
- Create: `frontend/src/features/health/dashboard/api.ts`
- Modify: `frontend/src/pages/dashboard/DashboardPage.tsx` (agregar sección "Salud")

- [ ] **Step 1: dashboard/api.ts**

```ts
import { useQuery } from '@tanstack/react-query';
import { http } from '@/lib/http';

export interface TopDisease { diseaseCode: string; name: string; count: number; }
export interface DashboardHealth {
  upcomingVaccinations7d: number;
  upcomingVaccinations30d: number;
  activeDiagnoses: number;
  treatmentsActiveCount: number;
  monthVetSpend: number;
  topDiseasesQuarter: TopDisease[];
}

export function useDashboardHealth() {
  return useQuery({
    queryKey: ['dashboard', 'health'],
    queryFn: async () => (await http.get<DashboardHealth>('/dashboard/health')).data,
    staleTime: 1000 * 60,
  });
}
```

- [ ] **Step 2: Modificar DashboardPage.tsx**

Después de la sección existente de cards de animales, agregar:

```tsx
const dashboardHealth = useDashboardHealth();

// ...

<h2 className="text-xl font-semibold mt-8">{t('health:nav.section')}</h2>
{dashboardHealth.data && (
  <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4 mt-4">
    <Card title={t('alerts:upcoming7d')} value={dashboardHealth.data.upcomingVaccinations7d} />
    <Card title={t('alerts:upcoming30d')} value={dashboardHealth.data.upcomingVaccinations30d} />
    <Card title={t('health:diagnosis.title')} value={dashboardHealth.data.activeDiagnoses} />
    <Card title={t('health:treatment.title')} value={dashboardHealth.data.treatmentsActiveCount} />
    <Card title={t('dashboard:vetSpendMonth') ?? 'Gasto veterinario mes'} value={dashboardHealth.data.monthVetSpend} />
  </div>
)}
```

(`<Card>` es el mismo componente de Fase 1 usado para totales del hato.)

Agregar key `vetSpendMonth` a `frontend/public/locales/{es,en}/dashboard.json`:
- ES: `"vetSpendMonth": "Gasto veterinario del mes"`
- EN: `"vetSpendMonth": "Vet spend this month"`

- [ ] **Step 3: Pausa de revisión.**

---

### Task 21: AnimalDetailPage gana tab "Salud"

**Files:**
- Modify: `frontend/src/pages/animals/AnimalDetailPage.tsx`
- Create: `frontend/src/features/animals/components/AnimalHealthTab.tsx`

- [ ] **Step 1: AnimalHealthTab**

```tsx
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import { useAnimalVaccinations } from '@/features/health/vaccinations/api';
import { useAnimalDiagnoses } from '@/features/health/diagnoses/api';
import { useAnimalTreatments } from '@/features/health/treatments/api';
import { localizedName } from '@/lib/catalog';
import i18n from '@/lib/i18n';

/**
 * Tab Salud del detalle de animal: vacunas, diagnosticos, tratamientos.
 */
export function AnimalHealthTab() {
  const { id } = useParams();
  const animalId = Number(id);
  const { t } = useTranslation(['health', 'common']);
  const locale = i18n.language;

  const vaccinations = useAnimalVaccinations(animalId);
  const diagnoses = useAnimalDiagnoses(animalId);
  const treatments = useAnimalTreatments(animalId);

  return (
    <div className="space-y-6">
      <section>
        <h3 className="font-semibold mb-2">{t('health:vaccination.title')}</h3>
        <table className="w-full border rounded">
          <thead><tr className="bg-muted">
            <th className="p-2 text-left">{t('health:vaccination.appliedAt')}</th>
            <th className="p-2 text-left">{t('health:vaccination.vaccine')}</th>
            <th className="p-2 text-left">{t('health:vaccination.nextDose')}</th>
          </tr></thead>
          <tbody>
            {vaccinations.data?.map(v => (
              <tr key={v.id} className="border-t">
                <td className="p-2">{v.appliedAt}</td>
                <td className="p-2">{locale.startsWith('en') ? v.vaccineNameEn : v.vaccineNameEs}</td>
                <td className="p-2">{v.nextDoseDue ?? '-'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section>
        <h3 className="font-semibold mb-2">{t('health:diagnosis.title')}</h3>
        <table className="w-full border rounded">
          <thead><tr className="bg-muted">
            <th className="p-2 text-left">{t('health:diagnosis.diagnosedAt')}</th>
            <th className="p-2 text-left">{t('health:diagnosis.disease')}</th>
            <th className="p-2 text-left">{t('health:diagnosis.status')}</th>
          </tr></thead>
          <tbody>
            {diagnoses.data?.map(d => (
              <tr key={d.id} className="border-t">
                <td className="p-2">{d.diagnosedAt}</td>
                <td className="p-2">{locale.startsWith('en') ? d.diseaseNameEn : d.diseaseNameEs}</td>
                <td className="p-2">{d.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section>
        <h3 className="font-semibold mb-2">{t('health:treatment.title')}</h3>
        <table className="w-full border rounded">
          <thead><tr className="bg-muted">
            <th className="p-2 text-left">{t('health:treatment.startedAt')}</th>
            <th className="p-2 text-left">{t('health:treatment.medication')}</th>
            <th className="p-2 text-left">{t('health:treatment.withdrawalMilk')}</th>
            <th className="p-2 text-left">{t('health:treatment.withdrawalMeat')}</th>
          </tr></thead>
          <tbody>
            {treatments.data?.map(tr => (
              <tr key={tr.id} className="border-t">
                <td className="p-2">{tr.startedAt}</td>
                <td className="p-2">{locale.startsWith('en') ? tr.medicationNameEn : tr.medicationNameEs}</td>
                <td className="p-2">{tr.withdrawalMilkUntil ?? '-'}</td>
                <td className="p-2">{tr.withdrawalMeatUntil ?? '-'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  );
}
```

(Asume que las APIs de vaccinations/diagnoses/treatments exponen el hook `useAnimal<X>(animalId)` que llama a `/animals/{id}/<x>`. Crear esos hooks dentro de cada api.ts respectivo.)

- [ ] **Step 2: Modificar AnimalDetailPage para añadir tab "Salud"**

Si la página usa Tabs de Radix (de Fase 1), agregar tercer trigger y panel:

```tsx
<TabsTrigger value="health">{t('health:tab.health')}</TabsTrigger>
<TabsContent value="health"><AnimalHealthTab /></TabsContent>
```

- [ ] **Step 3: Pausa de revisión.**

---

## Épica I — Verificación y cierre

### Task 22: Actualizar DEFINITION_OF_DONE.md

**Files:**
- Modify: `docs/DEFINITION_OF_DONE.md`

- [ ] **Step 1: Agregar checklist Fase 2** (12 puntos del spec §8) al final del archivo:

```markdown

## Fase 2 — Salud y Veterinaria

- [ ] Migraciones V6-V8 aplican limpio sobre BD vacia y sobre BD con datos de Fase 1.
- [ ] Usuario crea vacunacion individual y por lote (bulk genera N filas, una por animal activo del lote).
- [ ] Tratamiento calcula y persiste `withdrawal_milk_until` y `withdrawal_meat_until` segun la medicacion catalogada.
- [ ] Visita veterinaria agrupa eventos del mismo dia; el detalle muestra eventos asociados.
- [ ] Plan sanitario asignado a animal/lote dispara alertas de "vacunas faltantes" (placeholder en Fase 2 inicial).
- [ ] Tab "Salud" en detalle de animal carga vacunas, diagnoses y tratamientos del animal.
- [ ] `/dashboard` muestra cards de salud consumiendo `/dashboard/health`.
- [ ] `/health` muestra alertas agrupadas correctamente.
- [ ] i18n ES/EN completo para namespaces `health`, `catalog`, `alerts`.
- [ ] Roles aplicados: VIEWER no puede crear, WORKER no puede borrar, plans requieren ADMIN o superior.
- [ ] Aislamiento multi-tenant validado: cuenta A no ve eventos sanitarios de cuenta B.
- [ ] Costo veterinario MTD coincide con la suma manual de vaccinations + treatments + pest_controls + vet_visits del mes.
```

- [ ] **Step 2: Pausa de revisión.**

---

## Notas finales para el ejecutor

- **No hacer commits automáticos.** Solo crear/modificar archivos. El usuario maneja git por su cuenta.
- **Sin emojis** en código, comentarios o strings. **Sin arte ASCII** en comentarios.
- **Acentos en código Java:** evitarlos (usar ASCII). En strings UI/JSON de i18n están permitidos.
- **Si un step falla inesperadamente, parar y reportar.** No modificar el plan en silencio.
- **No ejecutar tests ni builds** (sin Maven en el entorno actual). Solo crear archivos.
- Cualquier archivo `Modify:` que no exista todavía: revisar primero si Fase 1 lo creó. Si no existe y la modificación lo requiere, crearlo como nuevo siguiendo el contexto del spec.
