# Digital Cow — Fase 4: Producción y Alimentación

Spec de diseño. Fecha: 2026-05-17. Hereda decisiones de plataforma de Fases 1-3.

---

## 1. Alcance

### 1.1 Incluido

**Pesajes (weighing):** registro periódico del peso de un animal. Cálculo de ADG (Average Daily Gain) entre pesajes consecutivos. Curva de crecimiento.

**Engorda (beef):**
- Tracking de animal en ciclo de engorda: pesos de entrada/intermedios/salida, días en corral
- Cálculo de ADG por período
- FCR (Feed Conversion Ratio) por lote (kg materia seca consumida / kg ganados por el lote)
- Rendimiento al sacrificio: peso vivo, peso canal, % rendimiento

**Producción de leche (dairy):**
- Registro de ordeño diario por animal (puede ser AM/PM o total)
- Conteo de células somáticas (SCC) por muestreo
- % grasa, % proteína por muestreo
- Curva de lactancia (días desde último parto vs L/día)
- Producción acumulada por lactancia (305 días)
- Tanque a granel (bulk tank) opcional: registro diario del total entregado

**Alimentación:**
- Catálogo de insumos (feed_item): heno, ensilaje, concentrado, sales minerales, etc., con costo por kg
- Plan de alimentación (feeding_plan): ración por categoría (lactancia, seco, engorda intensiva, etc.) con insumos y cantidades
- Asignación de plan a lote (lot_feeding_plan)
- Registro diario de consumo por lote (feeding_record): qué insumo, cuántos kg, fecha — para calcular costo real y FCR

**Reportes y dashboard:**
- Curva de crecimiento por animal (línea pesajes vs fecha)
- Curva de lactancia por vaca (línea L/día vs días de lactancia)
- Producción de tanque diaria/mensual
- Costo de alimento por animal/lote/mes
- Top vacas productoras del mes
- ADG promedio por lote
- FCR por lote (cuando hay datos)

**Tab "Producción" en /animals/:id:** historial de pesajes, ordeños (si dairy), curva de lactancia activa.

### 1.2 Fuera de alcance

- Integración con balanza automática (IoT)
- Integración con sistemas robóticos de ordeño
- Análisis de leche por laboratorio externo (sólo se captura el dato manual)
- Predicción de pico de producción con ML
- Pastoreo rotacional con biomasa
- Sales de venta de leche/animales (eso va a Fase 5 — Economía)

---

## 2. Modelo de datos

Mismo patrón multi-tenant (`account_id`).

**weighing**
- `id` PK, `account_id` FK, `animal_id` FK NOT NULL
- `weighed_at` DATE NOT NULL
- `weight_kg` DECIMAL(7,2) NOT NULL
- `method` ENUM('SCALE','TAPE','VISUAL_ESTIMATE') NULL
- `body_condition_score` DECIMAL(3,1) NULL (1.0-5.0)
- `weighed_by_user_id` FK
- `notes` TEXT NULL
- `created_at`, `updated_at`
- IX(account_id, animal_id, weighed_at)

**milking** (un registro por animal por día, o por sesión AM/PM)
- `id` PK, `account_id` FK, `animal_id` FK NOT NULL
- `milking_date` DATE NOT NULL
- `session` ENUM('TOTAL','AM','PM') NOT NULL DEFAULT 'TOTAL'
- `liters` DECIMAL(6,2) NOT NULL
- `recorded_by_user_id` FK
- `notes` TEXT NULL
- `created_at`, `updated_at`
- UQ(account_id, animal_id, milking_date, session)
- IX(account_id, milking_date)

**milk_sample** (resultados de laboratorio o medidor)
- `id` PK, `account_id` FK, `animal_id` FK NOT NULL
- `sampled_at` DATE NOT NULL
- `scc_cells_per_ml` INT NULL (somatic cell count)
- `fat_pct` DECIMAL(4,2) NULL
- `protein_pct` DECIMAL(4,2) NULL
- `lactose_pct` DECIMAL(4,2) NULL
- `notes` TEXT NULL
- `created_at`, `updated_at`
- IX(account_id, animal_id, sampled_at)

**bulk_tank_delivery** (entrega de tanque a granel)
- `id` PK, `account_id` FK, `ranch_id` FK NOT NULL
- `delivery_date` DATE NOT NULL
- `total_liters` DECIMAL(10,2) NOT NULL
- `buyer` VARCHAR(160) NULL
- `notes` TEXT NULL
- `created_by_user_id` FK
- `created_at`, `updated_at`
- IX(account_id, ranch_id, delivery_date)

**slaughter_result** (rendimiento al sacrificio)
- `id` PK, `account_id` FK, `animal_id` FK NOT NULL
- `slaughtered_at` DATE NOT NULL
- `live_weight_kg` DECIMAL(7,2) NULL
- `carcass_weight_kg` DECIMAL(7,2) NULL
- `yield_pct` DECIMAL(5,2) NULL (calculado al guardar si no viene: carcass/live * 100)
- `grade` VARCHAR(40) NULL (USDA Choice, EUROP R3, etc.)
- `buyer` VARCHAR(160) NULL
- `notes` TEXT NULL
- `created_by_user_id` FK
- `created_at`, `updated_at`
- IX(account_id, animal_id, slaughtered_at)

**feed_item** (catálogo de insumos, multi-tenant)
- `id` PK, `account_id` FK NULL (NULL = seed global; cuenta crea sus propios)
- `code` VARCHAR(60) NOT NULL
- `name_es`, `name_en` VARCHAR(160) NOT NULL
- `category` ENUM('FORAGE','SILAGE','CONCENTRATE','MINERAL','BYPRODUCT','OTHER') NOT NULL
- `dry_matter_pct` DECIMAL(5,2) NULL
- `protein_pct` DECIMAL(5,2) NULL
- `energy_mcal_kg` DECIMAL(5,2) NULL
- `unit_cost` DECIMAL(10,4) NULL (costo por kg)
- `currency` CHAR(3) NULL DEFAULT 'MXN'
- `notes` VARCHAR(400) NULL
- `created_at`, `updated_at`
- UQ(account_id, code)

**feeding_plan**
- `id` PK, `account_id` FK NOT NULL
- `name` VARCHAR(160) NOT NULL
- `category` ENUM('DAIRY_LACTATION','DAIRY_DRY','BEEF_GROWING','BEEF_FINISHING','CALF','OTHER') NOT NULL
- `description` VARCHAR(500) NULL
- `created_at`, `updated_at`

**feeding_plan_item** (insumos del plan con cantidad diaria por cabeza)
- `id` PK
- `feeding_plan_id` FK NOT NULL (ON DELETE CASCADE)
- `feed_item_id` FK NOT NULL
- `kg_per_head_day` DECIMAL(6,2) NOT NULL
- `notes` VARCHAR(200) NULL

**lot_feeding_plan** (asignación)
- `id` PK, `account_id` FK NOT NULL
- `lot_id` FK NOT NULL
- `feeding_plan_id` FK NOT NULL
- `assigned_at` DATE NOT NULL
- `unassigned_at` DATE NULL (cuando se quita el plan)
- IX(account_id, lot_id, assigned_at)

**feeding_record** (consumo real registrado)
- `id` PK, `account_id` FK NOT NULL
- `lot_id` FK NOT NULL (siempre por lote, no por animal individual)
- `feed_item_id` FK NOT NULL
- `consumed_at` DATE NOT NULL
- `total_kg` DECIMAL(10,2) NOT NULL
- `cost` DECIMAL(10,2) NULL (calculado: total_kg * feed_item.unit_cost si no se especifica)
- `recorded_by_user_id` FK
- `notes` VARCHAR(300) NULL
- IX(account_id, lot_id, consumed_at)

---

## 3. API REST

`/api/v1/production/...` y `/api/v1/feeding/...`

**Producción:**
- `POST/GET /production/weighings` + helper `/animals/{id}/weighings`
- `POST/GET /production/milkings` + helper `/animals/{id}/milkings`
- `POST /production/milkings/bulk` (registro masivo: array de animal_id + liters para un día)
- `POST/GET /production/milk-samples` + helper `/animals/{id}/milk-samples`
- `POST/GET /production/bulk-tank-deliveries`
- `POST/GET /production/slaughter-results`
- `GET /production/growth-curve/{animalId}` (pesajes + ADG entre puntos consecutivos)
- `GET /production/lactation-curve/{animalId}?lactationStartDate=...` (milkings agrupados por día)
- `GET /production/kpis?from=...&to=...&ranchId=...&lotId=...` (ADG promedio, total milk, top productoras)

**Alimentación:**
- `GET/POST /feeding/items`, PATCH/DELETE
- `GET/POST /feeding/plans`, PATCH/DELETE + endpoints anidados de items
- `POST /feeding/lot-assignments` para asignar plan a lote; `DELETE /feeding/lot-assignments/{id}` desasigna (set unassigned_at)
- `GET/POST /feeding/records`, PATCH/DELETE (calcula cost si no viene)
- `GET /feeding/cost-summary?from=...&to=...&groupBy=lot|ranch|month`

**Dashboard:**
- `GET /dashboard/production` (today milk total, MTD milk, avg ADG, top producers count)

---

## 4. Cálculos

- **ADG entre dos pesajes:** `(w2 - w1) / días entre ellos`. Devuelto en el endpoint growth-curve por cada par consecutivo.
- **Curva de lactancia:** días desde último `Calving` del animal vs `liters` agregados por día (suma de AM+PM o TOTAL). Devuelto en lactation-curve.
- **Producción de tanque MTD:** `SUM(bulk_tank_delivery.total_liters)` del mes.
- **Costo de alimento:** `SUM(feeding_record.cost)` por filtro.
- **FCR de lote:** `SUM(feeding_record.total_kg * feed_item.dry_matter_pct/100)` / `SUM(ganancia kg de animales del lote en el período)`. Cálculo opcional, se ofrece como endpoint si hay datos suficientes.

---

## 5. Frontend

Nueva sección "Producción" en sidebar (iconos: `Scale`, `Milk`, `Beef`). Y sección "Alimentación" con icono `Wheat`.

**Páginas:**
- `/production/weighings`, `/production/milkings`, `/production/milk-samples`, `/production/bulk-tank`, `/production/slaughter`
- `/production/growth-curve` (selector de animal → muestra Recharts LineChart)
- `/production/lactation-curve` (selector de animal → LineChart)
- `/production/kpis` (filtros + cards)
- `/feeding/items`, `/feeding/plans`, `/feeding/records`
- `/feeding/cost-summary`

Forms: `WeighingForm`, `MilkingForm`, `MilkingBulkForm` (tabla editable: filas = animales del lote seleccionado, columnas = AM/PM/TOTAL litros), `MilkSampleForm`, `BulkTankDeliveryForm`, `SlaughterResultForm`, `FeedItemForm`, `FeedingPlanEditor` (CRUD plan + items con drag-and-drop simulado), `LotAssignmentDialog`, `FeedingRecordForm`.

Tab "Produccion" en /animals/:id: timeline de weighings, milkings (si dairy), milk_samples, slaughter (si aplicable) + curva de crecimiento embedded.

i18n: nuevos namespaces `production`, `feeding`.

---

## 6. Definición de Fase 4 terminada

1. Migraciones V11 (producción) y V12 (alimentación) aplican limpio.
2. Crear weighing → ADG aparece en growth-curve correctamente.
3. Crear milking diario → lactation-curve dibuja la curva.
4. Bulk milking permite registrar 10+ animales en una sesión.
5. Slaughter calcula yield_pct automáticamente.
6. Catálogo seed de 6 feed_items globales (alfalfa, sorgo, maíz molido, soya, sal mineral, ensilaje maíz).
7. Feeding plan creado + asignado a lote + 3 feeding_records → cost-summary refleja gasto correcto.
8. Tab Producción en /animals/:id carga eventos.
9. Dashboard `/dashboard` muestra cards de producción.
10. i18n ES/EN completo.
11. Aislamiento multi-tenant.
12. Roles aplicados.
