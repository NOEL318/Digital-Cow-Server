# Digital Cow — Fase 3: Reproducción

Spec de diseño. Fecha: 2026-05-17. Hereda decisiones de plataforma de Fases 1 y 2.

---

## 1. Alcance

### 1.1 Incluido

Catálogos:
- Toros (bull): propios de la cuenta y de uso externo (referencia para IA). Multi-tenant.
- Inventario de pajillas de semen (semen_straw): cantidad disponible, vencimiento, proveedor.

Eventos reproductivos:
- Detección de celo (heat)
- Servicio reproductivo (service): inseminación artificial (IA) o monta natural; usa pajilla de semen si IA
- Diagnóstico de gestación (pregnancy_check): positivo, negativo, dudoso
- Parto (calving): fecha, facilidad (FREE/EASY/ASSISTED/DIFFICULT/SURGERY), vivo/muerto/gemelar, peso al nacer, sexo del becerro
- Aborto (abortion): fecha, etapa de gestación estimada, causa
- Destete (weaning): fecha, peso al destete del becerro
- Secado (dry-off, sólo dairy): fecha, días en lactancia

Genealogía (extensión a `animal`):
- `sire_id` FK -> animal.id (padre, si es un toro registrado en la cuenta) NULL
- `external_sire_name` VARCHAR(160) NULL (toro externo sin registro)
- `dam_id` FK -> animal.id (madre) NULL
- `birth_weight_kg` DECIMAL(5,2) NULL

KPIs reproductivos (calculados, no almacenados):
- Edad al primer parto (días)
- Intervalo entre partos (IEP, días)
- Días abiertos (open days)
- Tasa de concepción al primer servicio (por mes/año)
- Servicios por concepción
- Tasa de gestación (preñez confirmada / vacas servidas)

Alertas:
- Vacas próximas a parir (próximas 21 días)
- Vacas a secar (en lactancia >305 días, sólo dairy)
- Vacas servidas sin diagnóstico de gestación (>40 días desde servicio)
- Vacas vacías mucho tiempo (>120 días abiertos)

Dashboard widgets:
- Vacas preñadas confirmadas
- Próximos partos 21 días
- Vacas vacías
- Días abiertos promedio del hato

Tab "Reproducción" en detalle de animal: timeline de heats, services, pregnancy checks, calvings, abortions, weanings, dry-offs.

Páginas frontend:
- `/reproduction` overview con alertas
- `/reproduction/bulls` catálogo de toros
- `/reproduction/semen` inventario de pajillas
- `/reproduction/services` registros de servicios
- `/reproduction/pregnancy-checks`
- `/reproduction/calvings`
- `/reproduction/kpis` reporte de KPIs reproductivos

### 1.2 Fuera de alcance

- Sincronización hormonal (OvSync, CIDR)
- Transferencia de embriones (cubierto parcialmente: incluye un evento opcional, sin inventario de embriones)
- Genotipado / pruebas genéticas
- Predicción de fertilidad con ML

---

## 2. Modelo de datos

### 2.1 Catálogos (multi-tenant)

**bull** (toros)
- `id` PK
- `account_id` FK NOT NULL
- `internal_code` VARCHAR(60) NOT NULL (UQ por cuenta)
- `name` VARCHAR(160) NOT NULL
- `breed_id` FK NULL (referencia al catálogo breed de Fase 1)
- `source` ENUM('OWN','EXTERNAL') NOT NULL — OWN si es animal registrado; EXTERNAL si es referencia para IA
- `animal_id` FK NULL (si source=OWN, apunta al animal en `animal`)
- `registry_number` VARCHAR(80) NULL (registro genealógico)
- `notes` TEXT NULL
- `created_at`, `updated_at`
- UQ(account_id, internal_code)

**semen_straw** (inventario)
- `id` PK
- `account_id` FK NOT NULL
- `bull_id` FK NOT NULL
- `provider` VARCHAR(160) NULL (Alta Genetics, Select Sires, etc.)
- `batch_number` VARCHAR(80) NULL
- `total_quantity` INT NOT NULL DEFAULT 0
- `available_quantity` INT NOT NULL DEFAULT 0
- `received_at` DATE NULL
- `expires_at` DATE NULL
- `cost_per_straw` DECIMAL(10,2) NULL
- `storage_location` VARCHAR(120) NULL
- `notes` TEXT NULL
- `created_at`, `updated_at`
- IX(account_id, bull_id, expires_at)

### 2.2 Eventos reproductivos

**heat** (detección de celo)
- `id` PK, `account_id` FK NOT NULL
- `animal_id` FK NOT NULL
- `detected_at` TIMESTAMP NOT NULL (cuando se detectó, no día)
- `detection_method` ENUM('VISUAL','PEDOMETER','HEAT_PATCH','CAMERA','OTHER') NULL
- `intensity` ENUM('WEAK','MODERATE','STRONG') NULL
- `notes` TEXT NULL
- `detected_by_user_id` FK
- IX(account_id, animal_id, detected_at)

**service** (inseminación o monta)
- `id` PK, `account_id` FK NOT NULL
- `animal_id` FK NOT NULL (la hembra servida)
- `service_type` ENUM('AI','NATURAL','EMBRYO_TRANSFER') NOT NULL
- `service_date` DATE NOT NULL
- `bull_id` FK NULL (toro usado; obligatorio si NATURAL o AI)
- `semen_straw_id` FK NULL (si AI: se decrementa available_quantity en 1)
- `technician_name` VARCHAR(160) NULL
- `heat_id` FK NULL (link al celo detectado, opcional)
- `cost` DECIMAL(10,2) NULL
- `notes` TEXT NULL
- `created_by_user_id` FK
- IX(account_id, animal_id, service_date)
- IX(account_id, service_date)

**pregnancy_check**
- `id` PK, `account_id` FK NOT NULL
- `animal_id` FK NOT NULL
- `service_id` FK NULL (servicio que se está validando)
- `checked_at` DATE NOT NULL
- `method` ENUM('PALPATION','ULTRASOUND','BLOOD_TEST','MILK_TEST') NULL
- `result` ENUM('POSITIVE','NEGATIVE','DOUBTFUL') NOT NULL
- `estimated_gestation_days` SMALLINT NULL
- `estimated_calving_date` DATE NULL (sólo si POSITIVE; calculado = checked_at + (283 - estimated_gestation_days))
- `vet_visit_id` FK NULL
- `checked_by_user_id` FK
- `notes` TEXT NULL
- IX(account_id, animal_id, checked_at)

**calving** (parto)
- `id` PK, `account_id` FK NOT NULL
- `animal_id` FK NOT NULL (la madre)
- `calved_at` DATE NOT NULL
- `ease` ENUM('FREE','EASY','ASSISTED','DIFFICULT','SURGERY') NOT NULL DEFAULT 'FREE'
- `outcome` ENUM('LIVE','STILLBORN','TWIN_LIVE','TWIN_MIXED','TWIN_STILLBORN') NOT NULL DEFAULT 'LIVE'
- `calf_animal_id` FK NULL (si el becerro fue registrado como Animal nuevo en la cuenta)
- `calf_sex` ENUM('FEMALE','MALE') NULL
- `calf_birth_weight_kg` DECIMAL(5,2) NULL
- `pregnancy_check_id` FK NULL (link al check positivo que precedió)
- `notes` TEXT NULL
- `created_by_user_id` FK
- IX(account_id, animal_id, calved_at)

**abortion**
- `id` PK, `account_id` FK NOT NULL
- `animal_id` FK NOT NULL
- `aborted_at` DATE NOT NULL
- `estimated_gestation_days` SMALLINT NULL
- `cause` VARCHAR(300) NULL
- `pregnancy_check_id` FK NULL
- `notes` TEXT NULL
- `created_by_user_id` FK
- IX(account_id, animal_id, aborted_at)

**weaning**
- `id` PK, `account_id` FK NOT NULL
- `animal_id` FK NOT NULL (el becerro destetado)
- `weaned_at` DATE NOT NULL
- `weight_kg` DECIMAL(6,2) NULL
- `notes` TEXT NULL
- `created_by_user_id` FK
- IX(account_id, animal_id, weaned_at)

**dry_off** (secado, sólo dairy)
- `id` PK, `account_id` FK NOT NULL
- `animal_id` FK NOT NULL
- `dried_off_at` DATE NOT NULL
- `lactation_days` SMALLINT NULL
- `notes` TEXT NULL
- `created_by_user_id` FK
- IX(account_id, animal_id, dried_off_at)

### 2.3 Extensión a tabla `animal`

Migración añade columnas:
- `sire_id` BIGINT NULL FK -> animal.id
- `external_sire_name` VARCHAR(160) NULL
- `dam_id` BIGINT NULL FK -> animal.id
- `birth_weight_kg` DECIMAL(5,2) NULL

---

## 3. API REST

`/api/v1/reproduction/...`

- `GET/POST /reproduction/bulls`, `GET/PATCH/DELETE /reproduction/bulls/{id}`
- `GET/POST /reproduction/semen-straws`, `PATCH/DELETE /reproduction/semen-straws/{id}` (al crear servicio AI con straw, backend decrementa available_quantity)
- `GET/POST /reproduction/heats`, helper `/animals/{id}/heats`
- `GET/POST /reproduction/services`, helper `/animals/{id}/services`
- `GET/POST /reproduction/pregnancy-checks`, helper `/animals/{id}/pregnancy-checks`
- `GET/POST /reproduction/calvings`, helper `/animals/{id}/calvings`. POST puede crear opcionalmente un Animal hijo nuevo (campo `createCalfAnimal: true` en el dto + datos para crearlo).
- `GET/POST /reproduction/abortions`
- `GET/POST /reproduction/weanings`
- `GET/POST /reproduction/dry-offs`
- `GET /reproduction/alerts` (calculadas)
- `GET /reproduction/kpis?from=...&to=...` (KPIs agregados del período)
- `GET /dashboard/reproduction` (widgets)

---

## 4. Reglas de negocio

- Crear `service` con `service_type='AI'` y `semen_straw_id`: backend decrementa `available_quantity` atómicamente; falla si available <= 0 con `SEMEN_STRAW_EMPTY`.
- Crear `pregnancy_check` con `result='POSITIVE'` calcula `estimated_calving_date` si viene `estimated_gestation_days`.
- Crear `calving` con `outcome` que indica `LIVE` y `calf_sex` no nulo: opcionalmente crea un `Animal` hijo con `sire_id`/`dam_id` enlazados al servicio anterior.
- KPIs:
  - **Días abiertos por vaca** = días desde último parto hasta concepción confirmada (o "hoy" si no ha concebido). Stats: mediana, p75, p90, máx.
  - **IEP** = promedio de (`calving[n].calved_at - calving[n-1].calved_at`) por vaca con >=2 partos
  - **Edad al primer parto** = `calving[1].calved_at - animal.birth_date` cuando birth_date no es null
  - **Tasa concepción al primer servicio**: vacas con check POSITIVE asociado a su primer servicio post-parto / total de servicios post-parto

---

## 5. Frontend

Nueva sección "Reproducción" en sidebar (icono `Heart` de lucide).

Páginas según §1.1. Componentes principales:
- `BullForm`, `BullsTable`
- `SemenStrawForm`, `SemenStrawsTable`
- `HeatForm`, `ServiceForm` (con cálculo en tiempo real de `estimated_calving_date = service_date + 283 días`)
- `PregnancyCheckForm`
- `CalvingForm` con sub-form de "Registrar becerro" opcional
- `AbortionForm`, `WeaningForm`, `DryOffForm`
- `ReproductionAlertsList`
- `ReproductionKPIsPage` con tarjetas de KPIs y gráficas (boxplot de días abiertos con Recharts BarChart simulando distribución por buckets)

Tab "Reproducción" en detalle de animal: timeline ordenado descendente con cards por tipo de evento.

i18n: nuevos namespaces `reproduction`, `reproductionAlerts`. Mismo patrón que Fase 2.

---

## 6. Definición de "Fase 3 terminada"

1. Migración V9 (extensión animal) y V10 (catálogos+eventos reproductivos) aplican limpio.
2. Crear `bull`, `semen_straw`, registrar `heat`, `service` (AI con decremento de straw), `pregnancy_check`, `calving`.
3. Calving que crea un Animal hijo enlazado por `sire_id` y `dam_id`.
4. KPIs reproductivos calculan valores correctos contra una muestra manual.
5. Alertas muestran vacas próximas a parir y servidas sin check.
6. Tab Reproducción en /animals/:id carga timeline de eventos del animal.
7. Dashboard `/dashboard` muestra cards de reproducción.
8. i18n ES/EN completo para reproduction namespaces.
9. Aislamiento multi-tenant.
10. Roles aplicados: VIEWER solo lectura, WORKER crea pero no borra.
