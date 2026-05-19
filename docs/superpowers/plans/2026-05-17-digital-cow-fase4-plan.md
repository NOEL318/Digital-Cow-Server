# Digital Cow Fase 4 — Implementation Plan (Producción y Alimentación)

> **For agentic workers:** REQUIRED SUB-SKILL: subagent-driven-development. Checkbox tracking. NO commits. NO emojis. NO ASCII art.

**Goal:** Implementar Producción (pesajes, ordeño, muestras de leche, tanque a granel, sacrificio) y Alimentación (insumos, planes, registros de consumo) sobre la plataforma.

**Architecture:** Mismo monolito. Paquetes nuevos `com.digitalcow.production.*` y `com.digitalcow.feeding.*`. Migraciones V11 y V12. Frontend: features `production/`, `feeding/`.

**Spec:** `docs/superpowers/specs/2026-05-17-digital-cow-fase4-design.md`.

**Stack y patrones:** mismos que Fases 1-3. Consultar `health/treatment/` o `reproduction/calving/` como referencia de feature CRUD multi-tenant.

---

## Patrones (referencia rápida)

**Backend feature CRUD:** entity con `@Entity @Table @EntityListeners(TenantAwareEntityListener.class) @FilterDef(...) @Filter("accountFilter")`, extends `AbstractAuditableEntity` desde `com.digitalcow.common.jpa`. Repo extends `JpaRepository, JpaSpecificationExecutor`. Service `@Service @Transactional` con `@PreAuthorize`. Controller `@RestController @RequestMapping("/api/v1/...")`. DTOs como `record`. Mapper `@Mapper(componentModel = "spring")`. Event para invalidación de caches via `ApplicationEventPublisher`.

**Errores:** `BusinessException.notFound(ErrorCode.NOT_FOUND, "msg")`, `.conflict(ErrorCode.CONFLICT, ...)`, `.badRequest(ErrorCode.VALIDATION_ERROR, ...)`, `.forbidden(ErrorCode.FORBIDDEN, ...)`.

**Frontend feature:** `features/<x>/api.ts` (TanStack Query), `schemas.ts` (zod), `types.ts`, `components/<X>Form.tsx`. Página en `pages/<x>/<X>Page.tsx`. Ruta en `app/router.tsx`. Sidebar entry en `components/sidebar.tsx`.

---

## Épica A — Migraciones

### Task 1: V11 producción

**Files:** Create `backend/src/main/resources/db/migration/V11__production.sql`

- [ ] **Step 1: DDL**

```sql
CREATE TABLE weighing (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  weighed_at DATE NOT NULL,
  weight_kg DECIMAL(7,2) NOT NULL,
  method ENUM('SCALE','TAPE','VISUAL_ESTIMATE') NULL,
  body_condition_score DECIMAL(3,1) NULL,
  weighed_by_user_id BIGINT NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_weigh_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_weigh_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_weigh_user FOREIGN KEY (weighed_by_user_id) REFERENCES app_user(id),
  INDEX ix_weigh_acct_animal_date (account_id, animal_id, weighed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE milking (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  milking_date DATE NOT NULL,
  session ENUM('TOTAL','AM','PM') NOT NULL DEFAULT 'TOTAL',
  liters DECIMAL(6,2) NOT NULL,
  recorded_by_user_id BIGINT NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_milk_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_milk_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_milk_user FOREIGN KEY (recorded_by_user_id) REFERENCES app_user(id),
  CONSTRAINT uq_milk_acct_animal_date_session UNIQUE (account_id, animal_id, milking_date, session),
  INDEX ix_milk_acct_date (account_id, milking_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE milk_sample (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  sampled_at DATE NOT NULL,
  scc_cells_per_ml INT NULL,
  fat_pct DECIMAL(4,2) NULL,
  protein_pct DECIMAL(4,2) NULL,
  lactose_pct DECIMAL(4,2) NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_ms_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_ms_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  INDEX ix_ms_acct_animal_date (account_id, animal_id, sampled_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE bulk_tank_delivery (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  ranch_id BIGINT NOT NULL,
  delivery_date DATE NOT NULL,
  total_liters DECIMAL(10,2) NOT NULL,
  buyer VARCHAR(160) NULL,
  notes TEXT NULL,
  created_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_btd_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_btd_ranch FOREIGN KEY (ranch_id) REFERENCES ranch(id),
  CONSTRAINT fk_btd_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  INDEX ix_btd_acct_ranch_date (account_id, ranch_id, delivery_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE slaughter_result (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  slaughtered_at DATE NOT NULL,
  live_weight_kg DECIMAL(7,2) NULL,
  carcass_weight_kg DECIMAL(7,2) NULL,
  yield_pct DECIMAL(5,2) NULL,
  grade VARCHAR(40) NULL,
  buyer VARCHAR(160) NULL,
  notes TEXT NULL,
  created_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_slaughter_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_slaughter_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_slaughter_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  INDEX ix_slaughter_acct_animal_date (account_id, animal_id, slaughtered_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Task 2: V12 alimentación (con seed de 6 insumos globales)

**Files:** Create `backend/src/main/resources/db/migration/V12__feeding.sql`

- [ ] **Step 1: DDL + seed**

```sql
CREATE TABLE feed_item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NULL,
  code VARCHAR(60) NOT NULL,
  name_es VARCHAR(160) NOT NULL,
  name_en VARCHAR(160) NOT NULL,
  category ENUM('FORAGE','SILAGE','CONCENTRATE','MINERAL','BYPRODUCT','OTHER') NOT NULL,
  dry_matter_pct DECIMAL(5,2) NULL,
  protein_pct DECIMAL(5,2) NULL,
  energy_mcal_kg DECIMAL(5,2) NULL,
  unit_cost DECIMAL(10,4) NULL,
  currency CHAR(3) NULL DEFAULT 'MXN',
  notes VARCHAR(400) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_feed_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT uq_feed_account_code UNIQUE (account_id, code),
  INDEX ix_feed_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE feeding_plan (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  name VARCHAR(160) NOT NULL,
  category ENUM('DAIRY_LACTATION','DAIRY_DRY','BEEF_GROWING','BEEF_FINISHING','CALF','OTHER') NOT NULL,
  description VARCHAR(500) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_fp_account FOREIGN KEY (account_id) REFERENCES account(id),
  INDEX ix_fp_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE feeding_plan_item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  feeding_plan_id BIGINT NOT NULL,
  feed_item_id BIGINT NOT NULL,
  kg_per_head_day DECIMAL(6,2) NOT NULL,
  notes VARCHAR(200) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_fpi_plan FOREIGN KEY (feeding_plan_id) REFERENCES feeding_plan(id) ON DELETE CASCADE,
  CONSTRAINT fk_fpi_feed FOREIGN KEY (feed_item_id) REFERENCES feed_item(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE lot_feeding_plan (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  lot_id BIGINT NOT NULL,
  feeding_plan_id BIGINT NOT NULL,
  assigned_at DATE NOT NULL,
  unassigned_at DATE NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_lfp_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_lfp_lot FOREIGN KEY (lot_id) REFERENCES lot(id),
  CONSTRAINT fk_lfp_plan FOREIGN KEY (feeding_plan_id) REFERENCES feeding_plan(id),
  INDEX ix_lfp_acct_lot_date (account_id, lot_id, assigned_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE feeding_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  lot_id BIGINT NOT NULL,
  feed_item_id BIGINT NOT NULL,
  consumed_at DATE NOT NULL,
  total_kg DECIMAL(10,2) NOT NULL,
  cost DECIMAL(10,2) NULL,
  recorded_by_user_id BIGINT NULL,
  notes VARCHAR(300) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_fr_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_fr_lot FOREIGN KEY (lot_id) REFERENCES lot(id),
  CONSTRAINT fk_fr_feed FOREIGN KEY (feed_item_id) REFERENCES feed_item(id),
  CONSTRAINT fk_fr_user FOREIGN KEY (recorded_by_user_id) REFERENCES app_user(id),
  INDEX ix_fr_acct_lot_date (account_id, lot_id, consumed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO feed_item (account_id, code, name_es, name_en, category, dry_matter_pct, protein_pct, energy_mcal_kg, unit_cost, currency) VALUES
  (NULL, 'ALFALFA_HENO', 'Heno de alfalfa', 'Alfalfa hay', 'FORAGE', 90.0, 18.0, 2.2, 5.00, 'MXN'),
  (NULL, 'SORGO_GRANO', 'Grano de sorgo', 'Sorghum grain', 'CONCENTRATE', 88.0, 9.5, 3.2, 6.50, 'MXN'),
  (NULL, 'MAIZ_MOLIDO', 'Maiz molido', 'Ground corn', 'CONCENTRATE', 88.0, 9.0, 3.3, 7.20, 'MXN'),
  (NULL, 'SOYA_PASTA', 'Pasta de soya', 'Soybean meal', 'CONCENTRATE', 90.0, 47.0, 3.4, 14.50, 'MXN'),
  (NULL, 'SAL_MINERAL', 'Sal mineral', 'Mineral salt', 'MINERAL', 99.0, 0, 0, 12.00, 'MXN'),
  (NULL, 'ENSILAJE_MAIZ', 'Ensilaje de maiz', 'Corn silage', 'SILAGE', 32.0, 8.0, 2.6, 2.20, 'MXN')
ON DUPLICATE KEY UPDATE name_es=VALUES(name_es), name_en=VALUES(name_en);
```

---

## Épica B — Backend producción

### Task 3: Weighing feature (con ADG en growth-curve)

**Files:** paquete `backend/src/main/java/com/digitalcow/production/weighing/` con `Weighing`, `WeighingRepository`, `WeighingService`, `WeighingController`, `AnimalWeighingsController`, `dto/{Create,Update,Response}Dto`, `mapper/`, `event/WeighingChangedEvent`, `WeighingMethod` enum.

- [ ] **Step 1: Sigue el patrón.** CRUD multi-tenant. Endpoint `/api/v1/production/weighings` + helper `/api/v1/animals/{id}/weighings`.

- [ ] **Step 2: GrowthCurveService** con método `GET /api/v1/production/growth-curve/{animalId}` que devuelve:

```java
public record GrowthCurveDto(
    Long animalId,
    List<GrowthPoint> points
) {
    public record GrowthPoint(LocalDate date, BigDecimal weightKg, BigDecimal adgSincePrevious) { }
}
```

ADG: para cada par de puntos consecutivos `(w_n, d_n)` y `(w_{n+1}, d_{n+1})`, `adg = (w_{n+1} - w_n) / días entre fechas`. Punto inicial tiene `adgSincePrevious = null`.

Controller en `production/growth/GrowthCurveController.java`.

### Task 4: Milking feature (con bulk)

**Files:** paquete `backend/src/main/java/com/digitalcow/production/milking/` estándar + `MilkingBulkDto` para registro masivo.

- [ ] **Step 1: Milking entity y CRUD.** Enum `MilkingSession { TOTAL, AM, PM }`. UQ por (account_id, animal_id, milking_date, session) enforced en DB; el service captura `DataIntegrityViolationException` y lanza `BusinessException.conflict("MILKING_DUPLICATE", "Ya existe ordeño para esa sesión")`.

- [ ] **Step 2: Bulk endpoint** `POST /api/v1/production/milkings/bulk`:

```java
public record MilkingBulkDto(
    @NotNull LocalDate milkingDate,
    @NotNull MilkingSession session,
    @NotEmpty List<AnimalMilking> animals
) {
    public record AnimalMilking(@NotNull Long animalId, @NotNull BigDecimal liters) { }
}
```

Service crea N milkings en una transacción. Si alguno duplica, rollback completo con error de cuál animal falló.

- [ ] **Step 3: LactationCurveService** `GET /api/v1/production/lactation-curve/{animalId}?lactationStartDate=YYYY-MM-DD`:

```java
public record LactationCurveDto(
    Long animalId,
    LocalDate lactationStart,
    List<LactationPoint> points
) {
    public record LactationPoint(int dayOfLactation, LocalDate date, BigDecimal totalLiters) { }
}
```

- Si no se pasa `lactationStartDate`, se busca el último `calving` del animal y se usa su `calvedAt`.
- Para cada día desde lactationStart hasta hoy, sumar `liters` de todas las sesiones del animal en ese día (TOTAL + AM + PM).
- Si no hay ordeños el día, se omite el punto.

Controller `production/lactation/LactationCurveController.java`.

### Task 5: MilkSample, BulkTankDelivery, SlaughterResult features

**Files:** 3 paquetes estándar bajo `production/milksample/`, `production/bulktank/`, `production/slaughter/`.

- [ ] **Step 1: CRUD estándar** para los 3.

- [ ] **Step 2: SlaughterService.create** calcula `yield_pct` si no viene y ambos pesos están presentes: `yield = carcass / live * 100`. Redondeo a 2 decimales.

- [ ] **Step 3: Helpers** `/api/v1/animals/{id}/{weighings|milkings|milk-samples}` (algunos ya en tasks anteriores; sólo `milk-samples` aquí).

### Task 6: ProductionKpisService

**Files:** paquete `backend/src/main/java/com/digitalcow/production/kpis/`

- [ ] **Step 1: Dto**

```java
public record ProductionKpisDto(
    LocalDate from,
    LocalDate to,
    BigDecimal totalMilkLiters,
    BigDecimal avgDailyMilkLiters,
    Double avgAdgKgDay,
    List<TopProducer> topProducers
) {
    public record TopProducer(Long animalId, String internalTag, BigDecimal liters) { }
}
```

- [ ] **Step 2: Service** con queries SQL nativas:
  - `totalMilkLiters` = `SUM(liters) FROM milking WHERE account=? AND milking_date BETWEEN ? AND ?`
  - `avgDailyMilkLiters` = total / (days(to-from)+1)
  - `avgAdgKgDay` = promedio de ADG entre primer y último pesaje de cada animal en el período (cálculo en memoria con queries por animal o reducido en Java)
  - `topProducers` LIMIT 5 ordenado por SUM(liters) DESC.

- [ ] **Step 3: Controller** `/api/v1/production/kpis?from=...&to=...`.

### Task 7: DashboardProduction endpoint

**Files:** `backend/src/main/java/com/digitalcow/dashboard/DashboardProduction{Service,Controller}.java` + `dto/DashboardProductionDto.java`.

- [ ] **Step 1: DTO**

```java
public record DashboardProductionDto(
    BigDecimal todayMilkLiters,
    BigDecimal mtdMilkLiters,
    Double avgAdgKgDayThisMonth,
    long activeMilkingCows
) { }
```

- [ ] **Step 2: Service + cache `dashboard-production` + listeners (`MilkingChangedEvent`, `WeighingChangedEvent`).**

- [ ] **Step 3: Controller** `/api/v1/dashboard/production`. Registrar cache en `CacheConfig`.

---

## Épica C — Backend alimentación

### Task 8: FeedItem feature

**Files:** paquete `backend/src/main/java/com/digitalcow/feeding/feeditem/` con FeedItem entity (multi-tenant con account_id nullable para globals, igual que HealthPlan de Fase 2), enum `FeedCategory`.

- [ ] **Step 1: CRUD.** Service rechaza modificar items globales (account_id = null) con `FORBIDDEN`.

- [ ] **Step 2: Endpoint** `/api/v1/feeding/items`.

### Task 9: FeedingPlan feature

**Files:** paquete `backend/src/main/java/com/digitalcow/feeding/plan/` con `FeedingPlan`, `FeedingPlanItem`, `LotFeedingPlan` entities, repos, service, controller, DTOs, mapper, enum `FeedingPlanCategory`.

- [ ] **Step 1: CRUD plans.** Métodos: `list`, `get`, `create`, `update`, `delete` (sólo si no tiene `lot_feeding_plan` activos), `addItem`, `updateItem`, `removeItem`, `assignToLot`, `unassignFromLot` (setea `unassigned_at`).

- [ ] **Step 2: Endpoints**:
  - `/api/v1/feeding/plans` CRUD
  - `/api/v1/feeding/plans/{id}/items` POST/PATCH/DELETE
  - `/api/v1/feeding/lot-assignments` POST/DELETE

### Task 10: FeedingRecord feature

**Files:** paquete `backend/src/main/java/com/digitalcow/feeding/record/` estándar.

- [ ] **Step 1: Service.create** calcula `cost` si no viene: `cost = total_kg * feed_item.unit_cost`.

- [ ] **Step 2: Endpoints** `/api/v1/feeding/records` CRUD.

- [ ] **Step 3: FeedingCostSummaryService**:

```java
public record FeedingCostSummaryDto(
    LocalDate from,
    LocalDate to,
    String groupBy,                     // "lot" | "ranch" | "month"
    List<CostBucket> buckets
) {
    public record CostBucket(String key, String label, BigDecimal totalCost, BigDecimal totalKg) { }
}
```

Controller `/api/v1/feeding/cost-summary?from=...&to=...&groupBy=lot|ranch|month`.

Queries SQL nativas con GROUP BY apropiado:
- `lot`: GROUP BY lot.id (label = lot.name)
- `ranch`: JOIN lot ON lot.id=fr.lot_id GROUP BY lot.ranch_id (label = ranch.name)
- `month`: GROUP BY DATE_FORMAT(consumed_at, '%Y-%m') (label = YYYY-MM)

---

## Épica D — Frontend

### Task 11: i18n + sidebar

**Files:**
- Create JSONs `frontend/public/locales/{es,en}/{production,feeding}.json` con todas las strings necesarias (sigue el patrón de los namespaces existentes de Fase 2-3).
- Modify `frontend/src/lib/i18n.ts` agregando `'production', 'feeding'` a `ns`.
- Modify `frontend/src/components/sidebar.tsx` con dos secciones: "Produccion" (icono `Scale`) con NavLinks a /production/weighings, /production/milkings, /production/milk-samples, /production/bulk-tank, /production/slaughter, /production/growth-curve, /production/lactation-curve, /production/kpis; y "Alimentacion" (icono `Wheat`) con /feeding/items, /feeding/plans, /feeding/records, /feeding/cost-summary.

- [ ] **Step 1-3:** crear JSONs ES (estructura mínima por namespace con todas las labels usadas en componentes) y EN traducidos.

- [ ] **Step 4:** modificar i18n.ts y sidebar.

### Task 12: Features y páginas de producción

**Files:**
- `frontend/src/features/production/{weighings,milkings,milkSamples,bulkTank,slaughter,kpis,dashboard}/` con `api.ts`, `schemas.ts`, `types.ts`, `components/<X>Form.tsx`.
- Páginas en `frontend/src/pages/production/`: `WeighingsPage`, `MilkingsPage` (con tabs Individual/Bulk; `MilkingBulkForm` muestra tabla editable de animales del lote), `MilkSamplesPage`, `BulkTankPage`, `SlaughterPage`, `GrowthCurvePage` (selector de animal + LineChart), `LactationCurvePage` (selector animal + LineChart), `ProductionKpisPage` (date range + cards + tabla top producers).
- Rutas en `router.tsx`.

- [ ] **Step 1: Forms estándar** siguiendo el patrón de Fase 2-3.

- [ ] **Step 2: MilkingBulkForm**: tabla con filas = animales activos del lote seleccionado, columna `liters` editable; un submit envía el array a `/production/milkings/bulk`.

- [ ] **Step 3: GrowthCurvePage / LactationCurvePage**: selector de animal + LineChart Recharts con eje X (fecha o día de lactancia) y eje Y (peso o litros).

### Task 13: Features y páginas de alimentación

**Files:**
- `frontend/src/features/feeding/{items,plans,records,costSummary}/...`
- Páginas: `FeedItemsPage`, `FeedingPlansPage`, `FeedingPlanEditor` (full page con lista de items + agregar/eliminar), `LotAssignmentDialog`, `FeedingRecordsPage`, `FeedingCostSummaryPage` (date range + select groupBy + BarChart Recharts).
- Rutas en router.

- [ ] **Step 1-4: CRUD estándar.**

### Task 14: Tab Producción en AnimalDetailPage + Dashboard widgets

**Files:**
- Create `frontend/src/features/animals/components/AnimalProductionTab.tsx`: tabla de pesajes + curva embed (mini LineChart) + tabla de últimos 30 milkings si dairy + tabla de milk-samples.
- Modify `pages/animals/AnimalDetailPage.tsx`: agregar 5ta tab "Produccion".
- Modify `pages/dashboard/DashboardPage.tsx`: agregar sección "Produccion" con 4 cards del DashboardProductionDto, después de la sección "Reproduccion".

- [ ] **Step 1: AnimalProductionTab.**

- [ ] **Step 2: AnimalDetailPage tab.**

- [ ] **Step 3: DashboardPage widgets.**

### Task 15: Actualizar DEFINITION_OF_DONE.md con sección Fase 4

**Files:** Modify `docs/DEFINITION_OF_DONE.md`.

- [ ] **Step 1:** Agregar al final los 12 ítems del spec §6.

---

## Notas finales

- Sin commits, sin emojis, sin ASCII art.
- Java sin acentos en comentarios; strings UI ES con acentos OK.
- Sin ejecutar mvn/npm/tests.
- Reutilizar bean `tenantKeyGenerator` ya existente.
- Registrar nuevos caches `dashboard-production` (y opcionalmente `feeding-cost-summary`) en `CacheConfig`.
- Si el helper `useAnimals` no soporta filtro `purpose=DAIRY` o `purpose=BEEF` para los selectores, filtra client-side.
