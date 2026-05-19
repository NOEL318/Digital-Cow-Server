# Digital Cow — Fase 5: Economía y Reportes

Spec de diseño. Fecha: 2026-05-17. Hereda decisiones de plataforma de Fases 1-4. Última fase de la roadmap inicial.

---

## 1. Alcance

### 1.1 Incluido

**Movimientos económicos:**
- Catálogo de categorías de gasto e ingreso (expense_category, income_category) con seed de las más comunes
- Egresos manuales (expense): alimento, veterinaria, mano de obra, infraestructura, transporte, otros
- Ingresos (income): venta de animales, venta de leche, venta de subproductos, otros
- **Importación automática de costos ya capturados:** los `treatment.cost`, `vaccination.cost`, `pest_control.cost`, `vet_visit.total_cost`, `feeding_record.cost` y costos de servicios reproductivos cuentan como gastos. No se duplican en `expense` — el reporte económico los integra.
- Venta de animales (animal_sale): vincula con un `Animal` y registra precio, comprador, peso, fecha; cambia el `animal.status` a `SOLD` y genera un `income` automático
- Venta de leche (milk_sale): puede atar a una `bulk_tank_delivery` o ser independiente

**Cálculos económicos:**
- Costo por animal acumulado en su vida (alimentación + salud + reproducción + gastos individuales que apliquen)
- ROI por animal (ingresos - costos imputados)
- Margen por lote / por rancho
- P&L mensual: ingresos totales vs egresos totales por categoría
- Costo por kg de carne producido (suma costos beef / kg producidos en período)
- Costo por litro de leche producido (suma costos dairy / litros producidos en período)
- Flujo de caja simple por mes

**Reportes exportables:**
- Reporte por animal (HTML printable + descargable como JSON/CSV; PDF es opcional via `window.print`)
- P&L mensual (CSV descargable + vista web)
- Inventario actual (todos los animales activos con valores estimados)
- Histórico de ventas
- Resumen de salud (eventos sanitarios y costos por período)

Implementación de exportación: **CSV generado client-side** desde los datos JSON ya recibidos (sin necesidad de librería server-side de PDF). PDF queda fuera de scope para esta fase — el usuario usa "Imprimir → Guardar como PDF" del navegador.

**Dashboard widgets:**
- Ingreso MTD
- Egreso MTD (suma de manuales + costos importados)
- Margen MTD
- Top 3 categorías de egreso del mes
- Próximas ventas planeadas (animal_sale con sold_at futuro)

**Página `/finances` (overview):**
- Cards con ingresos/egresos/margen YTD
- BarChart mensual de los últimos 12 meses (ingresos vs egresos)
- PieChart de egresos por categoría
- Tabla de últimas 20 transacciones (mix de ingresos y egresos)

**Páginas adicionales:**
- `/finances/expenses` — lista, filtros, form
- `/finances/incomes` — idem
- `/finances/animal-sales` — venta de animales con form que cambia status
- `/finances/milk-sales` — venta de leche
- `/finances/categories` — gestión catálogo de categorías
- `/reports/animal/:id` — vista imprimible (historial completo)
- `/reports/pnl` — P&L con filtros, descarga CSV
- `/reports/inventory` — inventario actual, descarga CSV
- `/reports/sales-history`
- `/reports/health-summary`

### 1.2 Fuera de alcance

- Integración con SAT, facturación electrónica
- Conciliación bancaria
- Multimoneda con conversión automática (sí campos de currency, sin conversión)
- Presupuestos / forecasts
- Generación de PDF server-side
- Notificaciones por email de reportes

---

## 2. Modelo de datos

**expense_category** (catálogo multi-tenant; seed global con `account_id=NULL`)
- `id` PK
- `account_id` FK NULL (NULL = global seed)
- `code` VARCHAR(60) NOT NULL
- `name_es`, `name_en` VARCHAR(160) NOT NULL
- `kind` ENUM('FEED','HEALTH','LABOR','INFRASTRUCTURE','TRANSPORT','REPRODUCTION','OTHER') NOT NULL
- `notes` VARCHAR(400) NULL
- UQ(account_id, code), IX(account_id)

**income_category** (mismo patrón)
- `id`, `account_id` NULL, `code`, `name_es`, `name_en`, `kind` ENUM('ANIMAL_SALE','MILK_SALE','BYPRODUCT','SERVICE','OTHER'), `notes`, UQ(account_id, code)

**expense**
- `id` PK, `account_id` FK NOT NULL
- `expense_category_id` FK NOT NULL
- `incurred_at` DATE NOT NULL
- `amount` DECIMAL(12,2) NOT NULL
- `currency` CHAR(3) NOT NULL DEFAULT 'MXN'
- `ranch_id` FK NULL (si aplica a un rancho específico)
- `lot_id` FK NULL
- `animal_id` FK NULL (si aplica a un animal específico)
- `description` VARCHAR(400) NULL
- `vendor` VARCHAR(160) NULL
- `invoice_number` VARCHAR(80) NULL
- `created_by_user_id` FK
- `created_at`, `updated_at`
- IX(account_id, incurred_at), IX(account_id, ranch_id), IX(account_id, animal_id)

**income**
- `id` PK, `account_id` FK NOT NULL
- `income_category_id` FK NOT NULL
- `received_at` DATE NOT NULL
- `amount` DECIMAL(12,2) NOT NULL
- `currency` CHAR(3) NOT NULL DEFAULT 'MXN'
- `ranch_id` FK NULL, `lot_id` FK NULL, `animal_id` FK NULL
- `description` VARCHAR(400) NULL
- `payer` VARCHAR(160) NULL
- `invoice_number` VARCHAR(80) NULL
- `source_type` ENUM('MANUAL','ANIMAL_SALE','MILK_SALE','BULK_TANK','OTHER') NOT NULL DEFAULT 'MANUAL'
- `source_id` BIGINT NULL (id del registro fuente cuando source_type != MANUAL)
- `created_by_user_id` FK
- `created_at`, `updated_at`
- IX(account_id, received_at), IX(account_id, source_type, source_id)

**animal_sale**
- `id` PK, `account_id` FK NOT NULL
- `animal_id` FK NOT NULL UNIQUE (un animal solo se vende una vez)
- `sold_at` DATE NOT NULL
- `live_weight_kg` DECIMAL(7,2) NULL
- `price_per_kg` DECIMAL(10,4) NULL
- `total_price` DECIMAL(12,2) NOT NULL
- `currency` CHAR(3) NOT NULL DEFAULT 'MXN'
- `buyer` VARCHAR(160) NULL
- `notes` TEXT NULL
- `created_by_user_id` FK
- `created_at`, `updated_at`
- UQ(account_id, animal_id)

**milk_sale**
- `id` PK, `account_id` FK NOT NULL
- `sale_date` DATE NOT NULL
- `total_liters` DECIMAL(10,2) NOT NULL
- `price_per_liter` DECIMAL(10,4) NOT NULL
- `total_price` DECIMAL(12,2) NOT NULL
- `currency` CHAR(3) NOT NULL DEFAULT 'MXN'
- `buyer` VARCHAR(160) NULL
- `bulk_tank_delivery_id` FK NULL (si proviene de una entrega de tanque)
- `ranch_id` FK NULL
- `notes` TEXT NULL
- `created_by_user_id` FK
- `created_at`, `updated_at`
- IX(account_id, sale_date)

### 2.1 Reglas para `animal_sale` e `income` automático

Al crear un `animal_sale`:
1. Validar que `animal.status='ACTIVE'`. Si no, `CONFLICT`.
2. Setear `animal.status='SOLD'`.
3. Crear un `income` con `source_type='ANIMAL_SALE'`, `source_id=animal_sale.id`, `amount=total_price`, `received_at=sold_at`, `income_category` con `kind='ANIMAL_SALE'` (la primera de la cuenta o global).

Similar para `milk_sale`: crear `income` con `source_type='MILK_SALE'` y `source_id=milk_sale.id`.

Si se borra un `animal_sale`: borrar el `income` asociado y revertir `animal.status='ACTIVE'`. (Auditado.)

---

## 3. API REST

`/api/v1/finance/...` y `/api/v1/reports/...`

**Categorías:**
- `GET/POST /finance/expense-categories` (POST con `account_id` propio; los globales no se editan)
- `GET/POST /finance/income-categories`

**Egresos / ingresos:**
- `GET/POST /finance/expenses` con filtros: `from, to, categoryId, ranchId, lotId, animalId`
- `GET/POST /finance/incomes` con filtros similares
- PATCH/DELETE estándar

**Ventas:**
- `GET/POST /finance/animal-sales` (POST cambia status + crea income; DELETE revierte)
- `GET/POST /finance/milk-sales`
- PATCH (no permite cambiar animal_id; sí precio/comprador/notas)
- DELETE animal_sale revierte status y borra income

**Cálculos:**
- `GET /finance/pnl?from=...&to=...&groupBy=month|category` (P&L agregado con costos importados)
- `GET /finance/cash-flow?year=2026` (flujo de caja por mes del año)
- `GET /finance/animal-roi/{animalId}` (costos imputados + ingresos del animal específico)
- `GET /finance/cost-per-unit?from=...&to=...&purpose=BEEF|DAIRY` (costo por kg / costo por L)
- `GET /dashboard/finance` (cards del dashboard)

**Reportes:**
- `GET /reports/animal/{id}` (datos completos para vista imprimible)
- `GET /reports/pnl?from=...&to=...` (datos para CSV cliente-side)
- `GET /reports/inventory` (animales activos con datos)
- `GET /reports/sales-history?from=...&to=...`
- `GET /reports/health-summary?from=...&to=...`

---

## 4. Cálculos económicos

### 4.1 Costos imputados al animal

Para un `animal_id`:
- Tratamientos: `SUM(treatment.cost) WHERE animal_id = ?`
- Vacunaciones individuales: `SUM(vaccination.cost) WHERE animal_id = ?`
- Vacunaciones por lote (proporcional): `SUM(vaccination.cost / count_animales_lote) WHERE lot_id = animal.lot_id en momento de vacunación` — simplificación: dividir el costo de cada vacunación por lote entre el número de filas creadas (1 por animal del lote, por la expansión de Fase 2).
- Servicios reproductivos (`service_event.cost`): a la hembra `service_event.animal_id`.
- Gastos individuales (`expense.animal_id = ?`).
- Alimentación proporcional: por cada `feeding_record` del lote del animal en una fecha, asignar `cost / animales_activos_del_lote_en_ese_día`. Simplificación: usar el conteo actual de animales del lote como aproximación (se documenta como aproximación; mejorar a snapshot histórico es Fase futura).

### 4.2 Costo total del período

P&L:
- Ingresos = `SUM(income.amount) en período`
- Egresos = `SUM(expense.amount) + SUM(treatment.cost + vaccination.cost + pest_control.cost + vet_visit.total_cost + feeding_record.cost + service_event.cost en período)`
- Margen = Ingresos - Egresos

### 4.3 Costo por kg / por litro

- Período = un mes específico.
- Costo dairy: egresos vinculados a animales con `purpose='DAIRY'` + porción de costos generales (50% por defecto si no se discrimina; se documenta).
- Litros producidos: `SUM(milking.liters) en período`.
- Costo por L = costo_dairy / litros.
- Costo beef análogo: ganancia kg en período = `SUM((peso_final - peso_inicial) por animal beef)`.

---

## 5. Seed de categorías

**expense_category (account_id NULL):**
- FEED_GENERAL ("Alimentacion general" / "General feed"), FEED_CONCENTRATE, FEED_FORAGE
- HEALTH_VET ("Veterinaria" / "Vet"), HEALTH_VACCINES, HEALTH_MEDICATIONS
- LABOR_SALARIES ("Salarios" / "Salaries"), LABOR_CONTRACTORS
- INFRASTRUCTURE_REPAIR ("Infraestructura - reparacion" / "Infrastructure repair"), INFRASTRUCTURE_NEW
- TRANSPORT_FUEL, TRANSPORT_FREIGHT
- REPRODUCTION_SEMEN, REPRODUCTION_VET
- ENERGY, WATER, OTHER

**income_category (account_id NULL):**
- ANIMAL_SALE_BEEF, ANIMAL_SALE_DAIRY, ANIMAL_SALE_CULL
- MILK_SALE_BULK, MILK_SALE_DIRECT
- BYPRODUCT_MANURE, BYPRODUCT_OTHER
- SERVICE_BULL_RENTAL
- OTHER

---

## 6. Frontend

Nueva sección "Finanzas" en sidebar (icono `DollarSign`, `TrendingUp`, etc.) y sección "Reportes" (icono `FileText`).

Páginas según §1.1. Componentes:
- `ExpenseForm`, `IncomeForm` (con selectores de category, ranch/lot/animal opcionales)
- `AnimalSaleForm` (selector de animales `status=ACTIVE`, calcula `total_price = weight * price_per_kg` o ingresa manual)
- `MilkSaleForm`
- `CategoryForm` (con candado para globales)
- `FinanceOverviewPage`: cards + BarChart 12 meses + PieChart por categoría + tabla últimas transacciones
- `PnlReportPage`: filtros + tabla + botón "Descargar CSV" (genera CSV cliente-side desde el JSON)
- `InventoryReportPage`: tabla + descarga CSV
- `AnimalReportPage`: imprime con `window.print` (CSS @media print esconde sidebar/navbar)
- `SalesHistoryPage`, `HealthSummaryPage`

Tab "Finanzas" en `/animals/:id`: cards ROI del animal, costos imputados desglosados, ingresos.

i18n: namespaces `finance`, `reports`.

---

## 7. Definición de Fase 5 terminada

1. Migración V13 (catalogos + transacciones + ventas) aplica limpio.
2. Seed de expense_categories e income_categories cargado.
3. Crear `expense` manual + ver en lista filtrada.
4. Crear `animal_sale` cambia `animal.status='SOLD'` y crea `income` automático.
5. Borrar `animal_sale` revierte status y borra income (auditado).
6. `/finance/pnl` retorna ingresos + egresos (incluyendo costos importados de salud/repro/alimentación) coherentes.
7. `/finance/animal-roi/{id}` desglosa correctamente costos imputados.
8. Dashboard `/dashboard` muestra cards de finanzas.
9. `/finances` overview con BarChart 12 meses y PieChart por categoría.
10. `/reports/pnl` permite descargar CSV.
11. `/reports/animal/:id` se imprime sin sidebar/navbar.
12. i18n ES/EN completo. Aislamiento multi-tenant. Roles aplicados.
