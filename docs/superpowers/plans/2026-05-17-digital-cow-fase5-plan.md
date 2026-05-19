# Digital Cow Fase 5 — Implementation Plan (Economía y Reportes)

> **For agentic workers:** REQUIRED SUB-SKILL: subagent-driven-development. Checkbox tracking. NO commits. NO emojis. NO ASCII art.

**Goal:** Modulo de Economia (expenses, incomes, animal_sales, milk_sales, categorias) y Reportes (vistas imprimibles, descargas CSV cliente-side). Integra costos importados de Fases 2-4 en P&L. Ultima fase de la roadmap.

**Architecture:** Mismo monolito. Paquetes `com.digitalcow.finance.*` y `com.digitalcow.report.*`. Migracion V13. Frontend: features `finance/`, `report/`.

**Spec:** `docs/superpowers/specs/2026-05-17-digital-cow-fase5-design.md`.

**Stack y patrones:** mismos que Fases 1-4. Ver `feeding/feeditem/` como referencia de catalogo con account_id nullable; ver `health/treatment/` como referencia de evento con costo.

---

## Patrones (referencia)

**Backend feature CRUD:** mismo patron consolidado (entity con accountFilter, repo + service @Transactional con @PreAuthorize, controller, DTOs como record, mapper MapStruct, event para invalidacion de caches).

**BusinessException:** factories `.notFound(ErrorCode.NOT_FOUND, msg)`, `.conflict(ErrorCode.CONFLICT, msg)`, `.badRequest(ErrorCode.VALIDATION_ERROR, msg)`, `.forbidden(ErrorCode.FORBIDDEN, msg)`.

**Catalogos con seed global:** account_id nullable, @Filter("accountFilter") usa `account_id = :accountId OR account_id IS NULL`. Service rechaza modificar registros con account_id=null lanzando FORBIDDEN. Seed via Flyway con `INSERT ... ON DUPLICATE KEY UPDATE`.

**Frontend feature:** `features/<x>/{api,schemas,types}.ts` + `components/`, paginas en `pages/<x>/`, rutas en `app/router.tsx`, sidebar en `components/sidebar.tsx`.

---

## Epica A — Migracion

### Task 1: V13 finance (catalogos + transacciones + ventas + seeds)

**Files:** Create `backend/src/main/resources/db/migration/V13__finance.sql`

- [ ] **Step 1: DDL**

```sql
CREATE TABLE expense_category (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NULL,
  code VARCHAR(60) NOT NULL,
  name_es VARCHAR(160) NOT NULL,
  name_en VARCHAR(160) NOT NULL,
  kind ENUM('FEED','HEALTH','LABOR','INFRASTRUCTURE','TRANSPORT','REPRODUCTION','OTHER') NOT NULL,
  notes VARCHAR(400) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_exc_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT uq_exc_account_code UNIQUE (account_id, code),
  INDEX ix_exc_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE income_category (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NULL,
  code VARCHAR(60) NOT NULL,
  name_es VARCHAR(160) NOT NULL,
  name_en VARCHAR(160) NOT NULL,
  kind ENUM('ANIMAL_SALE','MILK_SALE','BYPRODUCT','SERVICE','OTHER') NOT NULL,
  notes VARCHAR(400) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_inc_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT uq_inc_account_code UNIQUE (account_id, code),
  INDEX ix_inc_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE expense (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  expense_category_id BIGINT NOT NULL,
  incurred_at DATE NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'MXN',
  ranch_id BIGINT NULL,
  lot_id BIGINT NULL,
  animal_id BIGINT NULL,
  description VARCHAR(400) NULL,
  vendor VARCHAR(160) NULL,
  invoice_number VARCHAR(80) NULL,
  created_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_exp_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_exp_cat FOREIGN KEY (expense_category_id) REFERENCES expense_category(id),
  CONSTRAINT fk_exp_ranch FOREIGN KEY (ranch_id) REFERENCES ranch(id),
  CONSTRAINT fk_exp_lot FOREIGN KEY (lot_id) REFERENCES lot(id),
  CONSTRAINT fk_exp_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_exp_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  INDEX ix_exp_acct_date (account_id, incurred_at),
  INDEX ix_exp_acct_ranch (account_id, ranch_id),
  INDEX ix_exp_acct_animal (account_id, animal_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE income (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  income_category_id BIGINT NOT NULL,
  received_at DATE NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'MXN',
  ranch_id BIGINT NULL,
  lot_id BIGINT NULL,
  animal_id BIGINT NULL,
  description VARCHAR(400) NULL,
  payer VARCHAR(160) NULL,
  invoice_number VARCHAR(80) NULL,
  source_type ENUM('MANUAL','ANIMAL_SALE','MILK_SALE','BULK_TANK','OTHER') NOT NULL DEFAULT 'MANUAL',
  source_id BIGINT NULL,
  created_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_inc_acct FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_inc_cat FOREIGN KEY (income_category_id) REFERENCES income_category(id),
  CONSTRAINT fk_inc_ranch FOREIGN KEY (ranch_id) REFERENCES ranch(id),
  CONSTRAINT fk_inc_lot FOREIGN KEY (lot_id) REFERENCES lot(id),
  CONSTRAINT fk_inc_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_inc_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  INDEX ix_inc_acct_date (account_id, received_at),
  INDEX ix_inc_acct_source (account_id, source_type, source_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE animal_sale (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  sold_at DATE NOT NULL,
  live_weight_kg DECIMAL(7,2) NULL,
  price_per_kg DECIMAL(10,4) NULL,
  total_price DECIMAL(12,2) NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'MXN',
  buyer VARCHAR(160) NULL,
  notes TEXT NULL,
  created_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_as_acct FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_as_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_as_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  CONSTRAINT uq_as_account_animal UNIQUE (account_id, animal_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE milk_sale (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  sale_date DATE NOT NULL,
  total_liters DECIMAL(10,2) NOT NULL,
  price_per_liter DECIMAL(10,4) NOT NULL,
  total_price DECIMAL(12,2) NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'MXN',
  buyer VARCHAR(160) NULL,
  bulk_tank_delivery_id BIGINT NULL,
  ranch_id BIGINT NULL,
  notes TEXT NULL,
  created_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_ms_acct FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_ms_btd FOREIGN KEY (bulk_tank_delivery_id) REFERENCES bulk_tank_delivery(id),
  CONSTRAINT fk_ms_ranch FOREIGN KEY (ranch_id) REFERENCES ranch(id),
  CONSTRAINT fk_ms_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  INDEX ix_ms_acct_date (account_id, sale_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

Seeds:

```sql
INSERT INTO expense_category (account_id, code, name_es, name_en, kind) VALUES
  (NULL, 'FEED_GENERAL', 'Alimentacion general', 'General feed', 'FEED'),
  (NULL, 'FEED_CONCENTRATE', 'Concentrados', 'Concentrates', 'FEED'),
  (NULL, 'FEED_FORAGE', 'Forrajes', 'Forages', 'FEED'),
  (NULL, 'HEALTH_VET', 'Veterinaria', 'Veterinary', 'HEALTH'),
  (NULL, 'HEALTH_VACCINES', 'Vacunas', 'Vaccines', 'HEALTH'),
  (NULL, 'HEALTH_MEDICATIONS', 'Medicamentos', 'Medications', 'HEALTH'),
  (NULL, 'LABOR_SALARIES', 'Salarios', 'Salaries', 'LABOR'),
  (NULL, 'LABOR_CONTRACTORS', 'Personal eventual', 'Contractors', 'LABOR'),
  (NULL, 'INFRASTRUCTURE_REPAIR', 'Infraestructura - reparacion', 'Infrastructure repair', 'INFRASTRUCTURE'),
  (NULL, 'INFRASTRUCTURE_NEW', 'Infraestructura - nueva', 'Infrastructure new', 'INFRASTRUCTURE'),
  (NULL, 'TRANSPORT_FUEL', 'Combustible', 'Fuel', 'TRANSPORT'),
  (NULL, 'TRANSPORT_FREIGHT', 'Fletes', 'Freight', 'TRANSPORT'),
  (NULL, 'REPRODUCTION_SEMEN', 'Semen', 'Semen', 'REPRODUCTION'),
  (NULL, 'REPRODUCTION_VET', 'Veterinaria reproductiva', 'Reproductive vet', 'REPRODUCTION'),
  (NULL, 'ENERGY', 'Energia', 'Energy', 'OTHER'),
  (NULL, 'WATER', 'Agua', 'Water', 'OTHER'),
  (NULL, 'OTHER', 'Otros', 'Other', 'OTHER')
ON DUPLICATE KEY UPDATE name_es=VALUES(name_es), name_en=VALUES(name_en);

INSERT INTO income_category (account_id, code, name_es, name_en, kind) VALUES
  (NULL, 'ANIMAL_SALE_BEEF', 'Venta animal carne', 'Beef animal sale', 'ANIMAL_SALE'),
  (NULL, 'ANIMAL_SALE_DAIRY', 'Venta animal lechero', 'Dairy animal sale', 'ANIMAL_SALE'),
  (NULL, 'ANIMAL_SALE_CULL', 'Venta de descarte', 'Cull sale', 'ANIMAL_SALE'),
  (NULL, 'MILK_SALE_BULK', 'Venta leche a granel', 'Bulk milk sale', 'MILK_SALE'),
  (NULL, 'MILK_SALE_DIRECT', 'Venta leche directa', 'Direct milk sale', 'MILK_SALE'),
  (NULL, 'BYPRODUCT_MANURE', 'Estiercol', 'Manure', 'BYPRODUCT'),
  (NULL, 'BYPRODUCT_OTHER', 'Otros subproductos', 'Other byproducts', 'BYPRODUCT'),
  (NULL, 'SERVICE_BULL_RENTAL', 'Renta de toro', 'Bull rental', 'SERVICE'),
  (NULL, 'OTHER', 'Otros', 'Other', 'OTHER')
ON DUPLICATE KEY UPDATE name_es=VALUES(name_es), name_en=VALUES(name_en);
```

---

## Epica B — Catalogos backend

### Task 2: ExpenseCategory + IncomeCategory features

**Files:** paquetes `backend/src/main/java/com/digitalcow/finance/category/`:
- `ExpenseCategory.java`, `ExpenseCategoryRepository.java`, `ExpenseCategoryService.java`, `ExpenseCategoryController.java`
- `IncomeCategory.java`, `IncomeCategoryRepository.java`, `IncomeCategoryService.java`, `IncomeCategoryController.java`
- `dto/{ExpenseCategoryCreateDto, ExpenseCategoryUpdateDto, ExpenseCategoryResponseDto}.java` y mismo para Income
- `mapper/ExpenseCategoryMapper.java`, `mapper/IncomeCategoryMapper.java`
- Enums `ExpenseKind { FEED, HEALTH, LABOR, INFRASTRUCTURE, TRANSPORT, REPRODUCTION, OTHER }`, `IncomeKind { ANIMAL_SALE, MILK_SALE, BYPRODUCT, SERVICE, OTHER }`

- [ ] **Step 1: Entities** con `account_id` nullable, `@Filter("accountFilter")` condition `account_id = :accountId OR account_id IS NULL` (patron HealthPlan/FeedItem).

- [ ] **Step 2: Services CRUD** que rechazan modificar items globales con `BusinessException.forbidden(ErrorCode.FORBIDDEN, "Cannot modify global category")`.

- [ ] **Step 3: Controllers** `/api/v1/finance/expense-categories` y `/api/v1/finance/income-categories`.

- [ ] **Step 4: Pausa de revision.**

---

## Epica C — Transacciones backend

### Task 3: Expense feature

**Files:** paquete `backend/src/main/java/com/digitalcow/finance/expense/` con Expense entity, repo (extends JpaSpecificationExecutor), specifications (filtros from/to/categoryId/ranchId/lotId/animalId), service, controller, DTOs, mapper, event `ExpenseChangedEvent`.

- [ ] **Step 1: CRUD estandar.**

- [ ] **Step 2: ExpenseSpecifications** con builders por cada filtro y compose con `Specification.and(...)`.

- [ ] **Step 3: Endpoints** `/api/v1/finance/expenses` con filtros via query params.

### Task 4: Income feature (con SourceType)

**Files:** paquete `backend/src/main/java/com/digitalcow/finance/income/` analogo a Expense.

- [ ] **Step 1: Income entity** con `source_type` enum y `source_id` Long.

- [ ] **Step 2: CRUD estandar** + filtros similares. Eventos `IncomeChangedEvent`.

### Task 5: AnimalSale feature (con cambio de status + income auto)

**Files:** paquete `backend/src/main/java/com/digitalcow/finance/animalsale/`.

- [ ] **Step 1: Entity** con UQ(account_id, animal_id).

- [ ] **Step 2: AnimalSaleService.create** transaccional:

```java
@PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
public AnimalSaleResponseDto create(AnimalSaleCreateDto dto) {
    Animal animal = animalRepository.findById(dto.animalId())
        .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Animal not found"));
    if (animal.getStatus() != AnimalStatus.ACTIVE) {
        throw BusinessException.conflict(ErrorCode.CONFLICT, "Animal is not active");
    }
    animal.setStatus(AnimalStatus.SOLD);

    AnimalSale sale = mapper.fromCreate(dto);
    AnimalSale saved = repository.save(sale);

    // Crear income automatico
    IncomeCategory category = incomeCategoryRepository
        .findFirstByAccountIdAndKindOrAccountIdIsNullAndKind(TenantContext.requireAccountId(), IncomeKind.ANIMAL_SALE)
        .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "No income category for ANIMAL_SALE"));
    Income income = new Income();
    income.setIncomeCategoryId(category.getId());
    income.setReceivedAt(dto.soldAt());
    income.setAmount(dto.totalPrice());
    income.setCurrency(dto.currency() != null ? dto.currency() : "MXN");
    income.setAnimalId(dto.animalId());
    income.setPayer(dto.buyer());
    income.setSourceType(IncomeSourceType.ANIMAL_SALE);
    income.setSourceId(saved.getId());
    incomeRepository.save(income);

    events.publishEvent(new AnimalSaleChangedEvent(TenantContext.requireAccountId()));
    return mapper.toDto(saved);
}
```

(El metodo del repository `findFirstByAccountIdAndKindOrAccountIdIsNullAndKind` puede simplificarse a dos queries; aceptable.)

- [ ] **Step 3: AnimalSaleService.delete** revierte:
  - Borra `income` con `source_type='ANIMAL_SALE' AND source_id = sale.id`
  - Revierte `animal.status = ACTIVE`
  - Borra `animal_sale`

- [ ] **Step 4: Controller** `/api/v1/finance/animal-sales` CRUD.

### Task 6: MilkSale feature

**Files:** paquete `backend/src/main/java/com/digitalcow/finance/milksale/`.

- [ ] **Step 1: CRUD** con creacion automatica de `income` (source_type='MILK_SALE', kind='MILK_SALE'). Delete revierte income.

- [ ] **Step 2: Endpoints** `/api/v1/finance/milk-sales`.

---

## Epica D — Calculos economicos backend

### Task 7: PnlService + endpoint

**Files:** paquete `backend/src/main/java/com/digitalcow/finance/pnl/`:
- `PnlService.java`, `PnlController.java`, `dto/PnlDto.java`

- [ ] **Step 1: DTO**

```java
public record PnlDto(
    LocalDate from,
    LocalDate to,
    String groupBy,                                 // "month" o "category"
    BigDecimal totalIncome,
    BigDecimal totalExpense,
    BigDecimal margin,
    List<PnlBucket> buckets,
    BreakdownDto importedCosts
) {
    public record PnlBucket(String key, String label, BigDecimal income, BigDecimal expense, BigDecimal margin) { }
    public record BreakdownDto(
        BigDecimal treatments,
        BigDecimal vaccinations,
        BigDecimal pestControls,
        BigDecimal vetVisits,
        BigDecimal feedingRecords,
        BigDecimal services
    ) { }
}
```

- [ ] **Step 2: Service** que suma:
  - `expense.amount` filtrado por periodo
  - `treatment.cost`, `vaccination.cost`, `pest_control.cost`, `vet_visit.total_cost`, `feeding_record.cost`, `service_event.cost` filtrados por periodo
  - Y agrupa por month (DATE_FORMAT '%Y-%m') o category (LEFT JOIN expense_category).
  - Para income: sumar `income.amount`.

- [ ] **Step 3: Controller** `/api/v1/finance/pnl?from=...&to=...&groupBy=month|category`.

### Task 8: CashFlowService + endpoint

**Files:** `backend/src/main/java/com/digitalcow/finance/cashflow/CashFlowService.java`, `CashFlowController.java`, `dto/CashFlowDto.java`.

- [ ] **Step 1: DTO**

```java
public record CashFlowDto(
    int year,
    List<MonthFlow> months
) {
    public record MonthFlow(int month, BigDecimal income, BigDecimal expense, BigDecimal net) { }
}
```

- [ ] **Step 2: Service** con queries native que devuelven 12 meses del año (incluso vacios).

- [ ] **Step 3: Controller** `/api/v1/finance/cash-flow?year=YYYY`.

### Task 9: AnimalRoiService + endpoint

**Files:** `backend/src/main/java/com/digitalcow/finance/roi/AnimalRoiService.java`, `AnimalRoiController.java`, `dto/AnimalRoiDto.java`.

- [ ] **Step 1: DTO**

```java
public record AnimalRoiDto(
    Long animalId,
    BigDecimal totalIncome,
    BigDecimal totalCost,
    BigDecimal roi,
    CostBreakdown costs
) {
    public record CostBreakdown(
        BigDecimal treatments,
        BigDecimal vaccinationsIndividual,
        BigDecimal vaccinationsProportionalLot,
        BigDecimal services,
        BigDecimal manualExpenses,
        BigDecimal feedingProportional
    ) { }
}
```

- [ ] **Step 2: Service** que:
  - Suma `treatment.cost WHERE animal_id = ?`
  - Suma `vaccination.cost WHERE animal_id = ?` (vacunaciones individuales por animal)
  - Para vacunaciones por lote (rows con mismo `batch_number` y mismo `applied_at`): asignar `cost / total_filas_de_ese_batch`. Simplificacion: usar `cost` tal como esta porque la expansion ya cuenta cost por fila individual de la vacunacion bulk; documentar.
  - Suma `service_event.cost WHERE animal_id = ?` (para vacas)
  - Suma `expense.amount WHERE animal_id = ?`
  - Suma alimentacion proporcional: para cada `feeding_record` del lote actual del animal, asignar `cost / N_animales_activos_en_lote` (snapshot actual).
  - Ingreso: suma `income.amount WHERE animal_id = ?` (incluye animal_sale auto-creada).

- [ ] **Step 3: Controller** `/api/v1/finance/animal-roi/{animalId}`.

### Task 10: CostPerUnitService + endpoint

**Files:** `backend/src/main/java/com/digitalcow/finance/costunit/`.

- [ ] **Step 1: DTO**

```java
public record CostPerUnitDto(
    LocalDate from,
    LocalDate to,
    String purpose,                       // "BEEF" o "DAIRY"
    BigDecimal totalCost,
    BigDecimal totalUnits,                 // kg para beef, litros para dairy
    BigDecimal costPerUnit
) { }
```

- [ ] **Step 2: Service.** Para `DAIRY`: sumar costos del periodo asociados a animales con `purpose='DAIRY'` (treatments/vaccinations/feeding del lote dairy / etc.). Litros = `SUM(milking.liters)`.

Para `BEEF`: sumar costos del periodo de animales `purpose='BEEF'`. Unidades = ganancia kg en el periodo (diff entre primer y ultimo `weighing` de cada animal beef activo).

Simplificacion documentada: si no hay datos suficientes, devuelve totalUnits = 0 y costPerUnit = null.

- [ ] **Step 3: Controller** `/api/v1/finance/cost-per-unit?from=...&to=...&purpose=BEEF|DAIRY`.

### Task 11: DashboardFinance endpoint

**Files:** `backend/src/main/java/com/digitalcow/dashboard/DashboardFinance{Service,Controller}.java`, `dto/DashboardFinanceDto.java`.

- [ ] **Step 1: DTO**

```java
public record DashboardFinanceDto(
    BigDecimal mtdIncome,
    BigDecimal mtdExpense,
    BigDecimal mtdMargin,
    BigDecimal ytdMargin,
    List<TopCategoryDto> topExpenseCategoriesMonth
) {
    public record TopCategoryDto(String categoryCode, String nameEs, String nameEn, BigDecimal total) { }
}
```

- [ ] **Step 2: Service** cacheable `dashboard-finance` + listeners `ExpenseChangedEvent`, `IncomeChangedEvent`, `AnimalSaleChangedEvent`, `MilkSaleChangedEvent`.

- [ ] **Step 3: Controller** `/api/v1/dashboard/finance`. Registrar cache en `CacheConfig`.

---

## Epica E — Reportes backend

### Task 12: ReportController endpoints

**Files:** `backend/src/main/java/com/digitalcow/report/ReportController.java` + `report/dto/{AnimalReportDto, InventoryReportDto, SalesHistoryDto, HealthSummaryDto}.java`.

- [ ] **Step 1: AnimalReportDto** agregando datos del animal + sus eventos historicos (vacunaciones, diagnoses, treatments, pesajes, milkings recientes, calvings, ventas si existe). Endpoint `GET /api/v1/reports/animal/{animalId}`.

- [ ] **Step 2: InventoryReportDto** con lista de animales activos: id, internal_tag, breed, sex, purpose, age_days, current_lot, current_ranch, last_weight_kg. Endpoint `GET /api/v1/reports/inventory`.

- [ ] **Step 3: SalesHistoryDto** lista combinada de animal_sales + milk_sales en periodo. Endpoint `GET /api/v1/reports/sales-history?from=...&to=...`.

- [ ] **Step 4: HealthSummaryDto** agregado por mes: # vacunaciones, # diagnoses por severidad, # tratamientos, costo total. Endpoint `GET /api/v1/reports/health-summary?from=...&to=...`.

- [ ] **Step 5: Pausa de revision.**

---

## Epica F — Frontend

### Task 13: i18n + sidebar

**Files:**
- Create `frontend/public/locales/{es,en}/{finance,reports}.json` con cobertura completa.
- Modify `frontend/src/lib/i18n.ts` agregando `'finance', 'reports'` a `ns`.
- Modify `frontend/src/components/sidebar.tsx` con secciones "Finanzas" (icono `DollarSign`, entradas: overview, expenses, incomes, animal-sales, milk-sales, categories) y "Reportes" (icono `FileText`, entradas: pnl, inventory, sales-history, health-summary).

- [ ] **Step 1-3:** crear JSONs ES+EN y modificar i18n.ts/sidebar.

### Task 14: Features y paginas de finanzas

**Files:**
- `frontend/src/features/finance/{categories,expenses,incomes,animalSales,milkSales,pnl,cashFlow,animalRoi,costPerUnit,dashboard}/` con `api.ts`, `schemas.ts`, `types.ts`, `components/<X>Form.tsx`.
- Paginas en `frontend/src/pages/finance/`:
  - `FinanceOverviewPage.tsx` (cards + BarChart 12 meses + PieChart por categoria + tabla ultimas transacciones)
  - `ExpensesPage.tsx`, `IncomesPage.tsx`
  - `AnimalSalesPage.tsx`, `MilkSalesPage.tsx`
  - `CategoriesPage.tsx` (con candado para globales)
- Rutas en `app/router.tsx`.

- [ ] **Step 1: CRUD estandar** para expenses, incomes (con filtros), categories (con tabs Expense/Income).

- [ ] **Step 2: AnimalSaleForm**: selector de animales `status='ACTIVE'`; calcula `total_price = liveWeightKg * pricePerKg` reactivamente; submit avisa "Esto cambiara el animal a SOLD y creara un income".

- [ ] **Step 3: MilkSaleForm**: selector opcional de `bulk_tank_delivery` (filtrado por rancho), `total_price = total_liters * price_per_liter` reactivo.

- [ ] **Step 4: FinanceOverviewPage**: consume `/finance/cash-flow?year=current`, `/finance/pnl?...&groupBy=category` para PieChart, y `/finance/incomes?size=20`, `/finance/expenses?size=20` ordenadas por fecha desc.

### Task 15: Features y paginas de reportes

**Files:**
- `frontend/src/features/reports/{animalReport,pnlReport,inventoryReport,salesHistory,healthSummary}/api.ts`
- Helper `frontend/src/lib/csv.ts`:

```ts
/**
 * Convierte un array de objetos a CSV string y dispara la descarga.
 */
export function downloadCsv(rows: Record<string, unknown>[], filename: string) {
  if (rows.length === 0) return;
  const headers = Object.keys(rows[0]);
  const escape = (v: unknown) => {
    const s = v == null ? '' : String(v);
    return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
  };
  const csv = [headers.join(','), ...rows.map(r => headers.map(h => escape(r[h])).join(','))].join('\n');
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
}
```

- Paginas en `frontend/src/pages/reports/`:
  - `PnlReportPage.tsx`: filtros from/to/groupBy + tabla + boton "Descargar CSV" usando `downloadCsv(rows, 'pnl-YYYY-MM-DD.csv')`
  - `InventoryReportPage.tsx`: tabla + CSV download
  - `AnimalReportPage.tsx`: vista imprimible con `@media print` que oculta navbar/sidebar (agregar clase `print:hidden` en AppLayout) y boton "Imprimir" que llama `window.print()`
  - `SalesHistoryPage.tsx`: tabla mixta + CSV download
  - `HealthSummaryPage.tsx`: tabla mensual + CSV download
- Rutas en `app/router.tsx`.

- [ ] **Step 1: Pages.**

- [ ] **Step 2: lib/csv.ts.**

- [ ] **Step 3: Reglas CSS print:** modificar `AppLayout.tsx` agregando `className="print:hidden"` al sidebar y navbar, y `print:p-0` al main.

### Task 16: Tab Finanzas en AnimalDetailPage + DashboardPage widgets

**Files:**
- Create `frontend/src/features/animals/components/AnimalFinanceTab.tsx`: consume `/finance/animal-roi/{id}`, muestra cards ROI + tabla desglose de costos.
- Modify `pages/animals/AnimalDetailPage.tsx`: agregar 6ta tab "Finanzas".
- Modify `pages/dashboard/DashboardPage.tsx`: agregar seccion "Finanzas" con 5 cards (mtdIncome, mtdExpense, mtdMargin, ytdMargin, top expense category) del DashboardFinanceDto.

- [ ] **Step 1-3.**

### Task 17: Actualizar DEFINITION_OF_DONE.md

**Files:** Modify `docs/DEFINITION_OF_DONE.md`.

- [ ] **Step 1:** Agregar al final seccion "## Fase 5 — Economia y Reportes" con los 12 items del spec §7.

---

## Notas finales

- Sin commits, sin emojis, sin ASCII art.
- Java sin acentos en comentarios; strings UI ES con acentos OK.
- Sin ejecutar mvn/npm/tests.
- `tenantKeyGenerator` ya existe; reutilizar.
- Registrar cache `dashboard-finance` en `CacheConfig`.
- Si una clase referenciada no existe con el nombre exacto, adapta al equivalente real.
- Esta es la ULTIMA fase de la roadmap inicial. Al completarla, el sistema cubre las 5 fases planeadas.
