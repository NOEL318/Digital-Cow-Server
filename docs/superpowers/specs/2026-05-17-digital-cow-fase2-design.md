# Digital Cow — Fase 2: Salud y Veterinaria

Spec de diseño. Fecha: 2026-05-17. Estado: aprobado para pasar a plan de implementación.

Hereda todas las decisiones de plataforma del spec de Fase 1 (`2026-05-16-digital-cow-fase1-design.md`): multi-tenancy por `account_id`, JWT, roles OWNER/ADMIN/MANAGER/WORKER/VIEWER, monolito modular, Cloudinary, Docker Compose, sin microservicios, sin Kubernetes, sin emojis ni arte ASCII.

---

## 1. Alcance

### 1.1 Incluido

Catálogos sanitarios (con datos seed):
- Vacunas (vaccine)
- Enfermedades (disease)
- Medicamentos (medication, con tiempos de retiro de leche y carne)
- Plagas y parásitos (pest)

Eventos sanitarios por animal o por lote:
- Vacunación (individual o por lote)
- Diagnóstico de enfermedad
- Tratamiento (medicamento aplicado, con cálculo automático de tiempo de retiro)
- Control de plagas (por lote)
- Visita veterinaria (por rancho, agrupa varios eventos)

Plan sanitario (sanitary plan):
- Plantilla de vacunación recurrente por raza/propósito (ej. desparasitar cada 6 meses, vacuna IBR/BVD anual)
- Asignación opcional a animales individuales

Alertas (calculadas en runtime, no persistidas):
- Vacunas próximas a vencer (próxima dosis dentro de 30 días)
- Animales sin vacunas obligatorias según plan asignado
- Tratamientos con tiempo de retiro vigente (leche/carne)
- Diagnósticos activos sin tratamiento

Dashboard widgets adicionales:
- Próximas vacunaciones (7 días, 30 días)
- Costo veterinario del mes (suma de vaccinations.cost + treatments.cost + vet_visits.cost + pest_controls.cost)
- Diagnósticos activos
- Top 5 enfermedades del trimestre

Tab "Salud" en el detalle del animal:
- Timeline de eventos sanitarios (vacunaciones, diagnósticos, tratamientos)
- Vacunas vigentes y próximas
- Tratamientos activos con retiro pendiente

Reportes (en pantalla, sin PDF):
- Historial sanitario completo por animal (vista exportable a impresión nativa del navegador)
- Resumen mensual de gasto veterinario por rancho
- Lista de animales tratados en período X

### 1.2 Fuera de alcance (diferido a fases posteriores)

- Exportación PDF/Excel (Fase 5)
- Notificaciones push o email de alertas (Fase futura)
- Predicción de brotes
- Integración con laboratorios o veterinarias externas
- Tratamientos con prescripción digital (firma, regulación)
- Cálculo de costo por kg producido afectado por tratamiento (Fase 5)

---

## 2. Modelo de datos

Todas las tablas siguen las convenciones de Fase 1: `account_id` columna NOT NULL indexada (excepto catálogos globales), timestamps `created_at`/`updated_at`, FK `ON DELETE RESTRICT` por defecto.

### 2.1 Catálogos (datos seed, globales — sin `account_id`)

**vaccine**
- `id` PK
- `code` VARCHAR(60) UQ
- `name_es` VARCHAR(160) NOT NULL
- `name_en` VARCHAR(160) NOT NULL
- `target_diseases` VARCHAR(400) NULL (lista corta de códigos de enfermedad separados por coma)
- `default_dose_ml` DECIMAL(5,2) NULL
- `route` ENUM('IM','SC','ORAL','INTRANASAL','TOPICAL') NULL (vía sugerida)
- `recommended_age_months` SMALLINT NULL (mínima edad de primer aplicación)
- `recommended_frequency_months` SMALLINT NULL (ej. 12 = anual, 6 = semestral)
- `species` ENUM('BOVINE') NOT NULL DEFAULT 'BOVINE'
- IX(`code`)

**disease**
- `id` PK
- `code` VARCHAR(60) UQ
- `name_es` VARCHAR(160) NOT NULL
- `name_en` VARCHAR(160) NOT NULL
- `category` ENUM('BACTERIAL','VIRAL','PARASITIC','METABOLIC','NUTRITIONAL','MECHANICAL','OTHER') NOT NULL
- `zoonotic` BOOLEAN NOT NULL DEFAULT FALSE
- `severity` ENUM('LOW','MEDIUM','HIGH') NOT NULL DEFAULT 'MEDIUM'
- `default_symptoms` VARCHAR(500) NULL
- IX(`code`)

**medication**
- `id` PK
- `code` VARCHAR(60) UQ
- `name_es` VARCHAR(160) NOT NULL
- `name_en` VARCHAR(160) NOT NULL
- `active_ingredient` VARCHAR(200) NULL
- `default_dose` VARCHAR(120) NULL (texto libre porque varía: "1 mL por 50 kg")
- `default_route` ENUM('IM','SC','IV','ORAL','TOPICAL','INTRAMAMMARY') NULL
- `withdrawal_milk_days` SMALLINT NOT NULL DEFAULT 0
- `withdrawal_meat_days` SMALLINT NOT NULL DEFAULT 0
- `notes` VARCHAR(400) NULL
- IX(`code`)

**pest**
- `id` PK
- `code` VARCHAR(60) UQ
- `name_es` VARCHAR(160) NOT NULL
- `name_en` VARCHAR(160) NOT NULL
- `scientific_name` VARCHAR(160) NULL
- `type` ENUM('TICK','FLY','WORM','LICE','MITE','OTHER') NOT NULL
- `region` ENUM('TROPICAL','TEMPERATE','ANY') NOT NULL DEFAULT 'ANY'
- `notes` VARCHAR(400) NULL
- IX(`code`)

### 2.2 Eventos sanitarios (multi-tenant)

**vet_visit** (visita veterinaria; agrupa eventos sanitarios del mismo día/rancho)
- `id` PK
- `account_id` FK NOT NULL
- `ranch_id` FK NOT NULL
- `visited_at` DATE NOT NULL
- `vet_name` VARCHAR(160) NOT NULL
- `vet_contact` VARCHAR(160) NULL (teléfono o email)
- `reason` VARCHAR(300) NOT NULL
- `total_cost` DECIMAL(12,2) NULL
- `notes` TEXT NULL
- `created_by_user_id` FK
- `created_at`, `updated_at`
- IX(`account_id`, `ranch_id`, `visited_at`)

**vaccination**
- `id` PK
- `account_id` FK NOT NULL
- `animal_id` FK NULL (si la vacunación es individual)
- `lot_id` FK NULL (si la vacunación fue masiva a un lote; si ambos NULL es inválido)
- `vaccine_id` FK NOT NULL (catálogo)
- `batch_number` VARCHAR(80) NULL (lote del laboratorio)
- `applied_at` DATE NOT NULL
- `dose_ml` DECIMAL(5,2) NULL
- `route` ENUM('IM','SC','ORAL','INTRANASAL','TOPICAL') NULL
- `next_dose_due` DATE NULL (calculada como applied_at + recommended_frequency_months si el plan lo define)
- `cost` DECIMAL(10,2) NULL
- `applied_by_user_id` FK
- `vet_visit_id` FK NULL
- `notes` TEXT NULL
- `created_at`, `updated_at`
- IX(`account_id`, `animal_id`, `applied_at`)
- IX(`account_id`, `lot_id`, `applied_at`)
- IX(`account_id`, `next_dose_due`) WHERE next_dose_due IS NOT NULL
- CHECK: animal_id IS NOT NULL OR lot_id IS NOT NULL (a nivel servicio, MySQL CHECK opcional)

Cuando se vacuna un lote completo, el backend expande en N filas individuales (una por animal activo del lote) o crea una sola fila lot-level y N "vaccination_animal" que enlazan? Para mantener simple y permitir consultar historial por animal eficientemente, **se expande**: una fila por animal afectado, con un mismo `batch_number` y `vet_visit_id` (si aplica) para correlacionar. Eso simplifica las consultas de "última vacuna por animal".

**diagnosis**
- `id` PK
- `account_id` FK NOT NULL
- `animal_id` FK NOT NULL
- `disease_id` FK NOT NULL
- `diagnosed_at` DATE NOT NULL
- `severity` ENUM('LOW','MEDIUM','HIGH') NOT NULL DEFAULT 'MEDIUM'
- `symptoms` VARCHAR(500) NULL
- `status` ENUM('ACTIVE','RECOVERED','CHRONIC','DECEASED') NOT NULL DEFAULT 'ACTIVE'
- `resolved_at` DATE NULL
- `diagnosed_by_user_id` FK
- `vet_visit_id` FK NULL
- `notes` TEXT NULL
- `created_at`, `updated_at`
- IX(`account_id`, `animal_id`, `diagnosed_at`)
- IX(`account_id`, `status`, `diagnosed_at`)

**treatment**
- `id` PK
- `account_id` FK NOT NULL
- `animal_id` FK NOT NULL
- `diagnosis_id` FK NULL (puede ser tratamiento preventivo sin diagnóstico)
- `medication_id` FK NOT NULL
- `started_at` DATE NOT NULL
- `ended_at` DATE NULL (si terminó; null = en curso)
- `dose` VARCHAR(120) NULL
- `doses_count` SMALLINT NULL (número total de aplicaciones planeadas)
- `route` ENUM('IM','SC','IV','ORAL','TOPICAL','INTRAMAMMARY') NULL
- `withdrawal_milk_until` DATE NULL (calculado: ended_at OR started_at + withdrawal_milk_days)
- `withdrawal_meat_until` DATE NULL
- `cost` DECIMAL(10,2) NULL
- `prescribed_by` VARCHAR(160) NULL (texto libre — veterinario externo)
- `vet_visit_id` FK NULL
- `created_by_user_id` FK
- `notes` TEXT NULL
- `created_at`, `updated_at`
- IX(`account_id`, `animal_id`, `started_at`)
- IX(`account_id`, `withdrawal_milk_until`) WHERE withdrawal_milk_until IS NOT NULL
- IX(`account_id`, `withdrawal_meat_until`) WHERE withdrawal_meat_until IS NOT NULL

**pest_control**
- `id` PK
- `account_id` FK NOT NULL
- `ranch_id` FK NULL
- `lot_id` FK NULL (si el control fue por lote específico)
- `pest_id` FK NOT NULL
- `product_used` VARCHAR(200) NOT NULL (texto libre; ej. "Cipermetrina 15%")
- `dose` VARCHAR(120) NULL
- `applied_at` DATE NOT NULL
- `next_application_at` DATE NULL
- `cost` DECIMAL(10,2) NULL
- `applied_by_user_id` FK
- `notes` TEXT NULL
- `created_at`, `updated_at`
- IX(`account_id`, `ranch_id`, `applied_at`)
- IX(`account_id`, `lot_id`, `applied_at`)
- IX(`account_id`, `next_application_at`)
- CHECK: ranch_id OR lot_id (a nivel servicio)

### 2.3 Plan sanitario

**health_plan** (plantilla reutilizable)
- `id` PK
- `account_id` FK NULL (NULL = plan global del sistema; los plans por cuenta tienen account_id)
- `name` VARCHAR(160) NOT NULL
- `description` VARCHAR(500) NULL
- `applies_to_purpose` ENUM('BEEF','DAIRY','DUAL','ANY') NOT NULL DEFAULT 'ANY'
- `applies_to_sex` ENUM('FEMALE','MALE','ANY') NOT NULL DEFAULT 'ANY'
- `created_at`, `updated_at`
- IX(`account_id`)

**health_plan_step** (pasos del plan)
- `id` PK
- `health_plan_id` FK NOT NULL
- `step_order` SMALLINT NOT NULL
- `name` VARCHAR(160) NOT NULL (ej. "Primera dosis IBR/BVD")
- `vaccine_id` FK NULL
- `age_months_min` SMALLINT NULL (edad mínima del animal para aplicar)
- `recurrence_months` SMALLINT NULL (NULL = una sola vez; valor = se repite cada X meses)
- `notes` VARCHAR(400) NULL
- IX(`health_plan_id`, `step_order`)

**animal_health_plan** (asignación de plan a animal o lote)
- `id` PK
- `account_id` FK NOT NULL
- `health_plan_id` FK NOT NULL
- `animal_id` FK NULL
- `lot_id` FK NULL (si se asigna a todo el lote)
- `assigned_at` DATE NOT NULL DEFAULT CURRENT_DATE
- `created_at`, `updated_at`
- IX(`account_id`, `animal_id`)
- IX(`account_id`, `lot_id`)

---

## 3. API REST

Versionado `/api/v1`. Mismo formato de error que Fase 1.

### 3.1 Catálogos (read-only para usuarios autenticados)

- `GET /api/v1/catalog/vaccines`
- `GET /api/v1/catalog/diseases`
- `GET /api/v1/catalog/medications`
- `GET /api/v1/catalog/pests`

Cacheables. Cliente puede cachear en TanStack Query con stale time largo.

### 3.2 Eventos sanitarios

**Vacunaciones:**
- `GET /api/v1/health/vaccinations` (paginado, filtros: `animalId`, `lotId`, `vaccineId`, `from`, `to`)
- `POST /api/v1/health/vaccinations` (individual: requiere `animalId`)
- `POST /api/v1/health/vaccinations/bulk` (lote: requiere `lotId`; backend expande a una fila por animal activo)
- `GET /api/v1/health/vaccinations/{id}`
- `PATCH /api/v1/health/vaccinations/{id}`
- `DELETE /api/v1/health/vaccinations/{id}`
- `GET /api/v1/animals/{id}/vaccinations` (helper de timeline)

**Diagnósticos:**
- `GET /api/v1/health/diagnoses` (paginado, filtros: `animalId`, `diseaseId`, `status`, `from`, `to`)
- `POST /api/v1/health/diagnoses`
- `GET /api/v1/health/diagnoses/{id}`
- `PATCH /api/v1/health/diagnoses/{id}` (puede cambiar status a RECOVERED/CHRONIC/DECEASED, setea `resolved_at`)
- `DELETE /api/v1/health/diagnoses/{id}` (solo si no tiene tratamientos asociados)
- `GET /api/v1/animals/{id}/diagnoses`

**Tratamientos:**
- `GET /api/v1/health/treatments` (filtros: `animalId`, `medicationId`, `active=true/false`, `withdrawalActive=true`, `from`, `to`)
- `POST /api/v1/health/treatments`
- `GET /api/v1/health/treatments/{id}`
- `PATCH /api/v1/health/treatments/{id}` (cierra con `ended_at`, recalcula withdrawals)
- `DELETE /api/v1/health/treatments/{id}`
- `GET /api/v1/animals/{id}/treatments`

**Controles de plagas:**
- `GET /api/v1/health/pest-controls` (filtros: `ranchId`, `lotId`, `pestId`, `from`, `to`)
- `POST /api/v1/health/pest-controls`
- `GET /api/v1/health/pest-controls/{id}`
- `PATCH /api/v1/health/pest-controls/{id}`
- `DELETE /api/v1/health/pest-controls/{id}`

**Visitas veterinarias:**
- `GET /api/v1/health/vet-visits` (filtros: `ranchId`, `from`, `to`)
- `POST /api/v1/health/vet-visits`
- `GET /api/v1/health/vet-visits/{id}` (devuelve también eventos asociados: vacunaciones, diagnósticos, tratamientos con `vet_visit_id` = id)
- `PATCH /api/v1/health/vet-visits/{id}`
- `DELETE /api/v1/health/vet-visits/{id}` (solo si no tiene eventos asociados, o desasocia y borra; el spec elige: requiere desasociación previa, devuelve 409 con `VET_VISIT_HAS_EVENTS`)

### 3.3 Plan sanitario

- `GET /api/v1/health/plans` (lista plans de la cuenta + globales del sistema)
- `POST /api/v1/health/plans` (crea plan custom de la cuenta; requiere ADMIN o superior)
- `GET /api/v1/health/plans/{id}`
- `PATCH /api/v1/health/plans/{id}` (no se puede editar planes globales del sistema; 403)
- `DELETE /api/v1/health/plans/{id}` (solo cuenta-propios, sin asignaciones activas)
- `POST /api/v1/health/plans/{id}/steps`
- `PATCH /api/v1/health/plans/{id}/steps/{stepId}`
- `DELETE /api/v1/health/plans/{id}/steps/{stepId}`
- `POST /api/v1/health/plans/{id}/assign` (body: `{animalIds?, lotIds?}`)
- `DELETE /api/v1/health/plans/{id}/assignments/{assignmentId}`

### 3.4 Alertas (computadas)

- `GET /api/v1/health/alerts` devuelve:

```json
{
  "upcomingVaccinations7d": [{ "animalId": 1, "internalTag": "A001", "vaccineCode": "IBR-BVD", "dueDate": "2026-05-22" }],
  "upcomingVaccinations30d": [...],
  "missingMandatoryVaccinations": [{ "animalId": 2, "planName": "Plan Dairy Standard", "missingSteps": ["Vacuna IBR/BVD 12m"] }],
  "withdrawalActiveMilk": [{ "animalId": 3, "treatmentId": 5, "until": "2026-05-25", "medicationName": "Oxitetraciclina" }],
  "withdrawalActiveMeat": [...],
  "activeDiagnosesWithoutTreatment": [{ "animalId": 4, "diagnosisId": 9, "diseaseName": "Mastitis", "diagnosedAt": "2026-05-15" }]
}
```

Cacheado 5 minutos por cuenta (Caffeine). Invalidado en cualquier mutación de salud.

### 3.5 Dashboard extendido

- `GET /api/v1/dashboard/health` (independiente del summary base de Fase 1; el frontend hace dos llamadas y compone):

```json
{
  "upcomingVaccinations7d": 12,
  "upcomingVaccinations30d": 45,
  "activeDiagnoses": 3,
  "monthVetSpend": { "total": 1250.50, "currency": "MXN" },
  "topDiseasesQuarter": [{ "diseaseCode": "MASTITIS", "name": "Mastitis", "count": 8 }],
  "treatmentsActiveCount": 5
}
```

### 3.6 Reportes

- `GET /api/v1/health/reports/animal/{id}` devuelve historial sanitario completo (vacunaciones, diagnoses, treatments en orden cronológico) en formato vista HTML-friendly. Sin generación de PDF — el frontend usa CSS `@media print` para una vista imprimible.

---

## 4. Matriz de roles

Sobre la matriz de Fase 1, se agrega:

| Acción | OWNER | ADMIN | MANAGER | WORKER | VIEWER |
|---|---|---|---|---|---|
| Ver alertas y reportes de salud | sí | sí | sí | sí | sí |
| Registrar vacunación/tratamiento/diagnóstico | sí | sí | sí | sí | no |
| Borrar evento sanitario | sí | sí | sí | no | no |
| Crear/editar planes sanitarios de la cuenta | sí | sí | no | no | no |
| Borrar plan sanitario | sí | sí | no | no | no |
| Asignar plan sanitario a animales/lotes | sí | sí | sí | no | no |

---

## 5. Cálculos del backend

### 5.1 Next dose due

Al crear una vacunación con `vaccine.recommended_frequency_months` no null:
- `next_dose_due = applied_at + recommended_frequency_months` (calculado en Java con `LocalDate.plusMonths()`).

Si el animal está asignado a un health plan, el cálculo prefiere `health_plan_step.recurrence_months` cuando la vacuna coincide con un step.

### 5.2 Withdrawal calculation

Al crear o actualizar un tratamiento:
- `withdrawal_milk_until = (ended_at OR last_dose_date OR started_at) + medication.withdrawal_milk_days`
- `withdrawal_meat_until = (ended_at OR last_dose_date OR started_at) + medication.withdrawal_meat_days`

Si `medication.withdrawal_milk_days = 0` se setea `withdrawal_milk_until = NULL`.

### 5.3 Costo veterinario del mes

```sql
SELECT COALESCE(SUM(cost),0)
  FROM (
    SELECT cost FROM vaccination WHERE account_id = ? AND applied_at >= ? AND applied_at < ?
    UNION ALL
    SELECT cost FROM treatment WHERE account_id = ? AND started_at >= ? AND started_at < ?
    UNION ALL
    SELECT cost FROM pest_control WHERE account_id = ? AND applied_at >= ? AND applied_at < ?
    UNION ALL
    SELECT total_cost FROM vet_visit WHERE account_id = ? AND visited_at >= ? AND visited_at < ?
  ) sub
```

Donde `?` son `accountId, startOfMonth, startOfNextMonth`.

### 5.4 Alertas

Cada bucket de alertas es una query parametrizada con índices ya definidos:

- `upcomingVaccinations7d`: `WHERE account_id = ? AND next_dose_due BETWEEN today AND today+7`
- `withdrawalActiveMilk`: `WHERE account_id = ? AND withdrawal_milk_until >= today`
- `activeDiagnosesWithoutTreatment`: `LEFT JOIN treatment ON treatment.diagnosis_id = diagnosis.id WHERE diagnosis.status = 'ACTIVE' AND treatment.id IS NULL`

---

## 6. Frontend

### 6.1 Nuevas páginas

- `/health` — landing con tarjetas de resumen + lista de alertas
- `/health/vaccinations` — listado y formulario
- `/health/diagnoses` — listado y formulario
- `/health/treatments` — listado y formulario
- `/health/pest-controls` — listado y formulario
- `/health/vet-visits` — listado y detalle (con sub-eventos)
- `/health/plans` — gestión de planes y steps

Nueva sección "Salud" en sidebar. Iconos lucide (`Syringe`, `Stethoscope`, `Pill`, `Bug`, `CalendarDays`).

### 6.2 Tab Salud en detalle de animal

En `/animals/:id`, agregar 3ra tab "Salud" (las dos existentes son Información y Fotos). La tab muestra:
- Vacunas: tabla con `applied_at`, `vaccine_name`, `next_dose_due`, badge de "vence en N días" si <=30
- Diagnósticos: tabla con `diagnosed_at`, `disease`, `status`, `severity`
- Tratamientos: tabla con `started_at`, `medication`, badge "retiro hasta YYYY-MM-DD" si vigente
- Botón "Vista imprimible" → abre nueva pestaña con vista de reporte

### 6.3 Dashboard extendido

En la página `/dashboard`, después de las cards y gráficas de Fase 1, agregar sección "Salud":
- Card "Próximas vacunaciones 7 días" (número grande)
- Card "Diagnósticos activos"
- Card "Tratamientos activos"
- Card "Gasto veterinario del mes"
- Lista compacta de alertas (top 5 más urgentes)

### 6.4 Componentes nuevos

- `VaccinationForm` (individual y bulk; el bulk usa un selector de lote y permite vacunar todo el lote en un POST)
- `DiagnosisForm`
- `TreatmentForm` (calcula y muestra withdrawals en tiempo real al elegir medicación)
- `PestControlForm`
- `VetVisitForm` (con sub-secciones para agregar vacunaciones/diagnoses/treatments dentro de la visita)
- `HealthPlanEditor` (CRUD de steps con drag-and-drop de orden)
- `HealthAlertsList` (consume `/health/alerts`, agrupado por bucket)
- `AnimalHealthTab` (consume `/animals/{id}/vaccinations`, `/diagnoses`, `/treatments`)

### 6.5 i18n

Nuevos namespaces:
- `health` (todas las strings de páginas de salud)
- `catalog` (nombres de vacunas/enfermedades/medicamentos/plagas; el catálogo tiene name_es/name_en, así que el JSON solo lleva labels UI como "Lote del laboratorio", "Vía", etc.)
- `alerts` (mensajes de cada bucket)

Los nombres de elementos del catálogo (vaccine, disease, medication, pest) vienen del backend ya traducidos (campos `name_es`/`name_en`); el frontend elige según el locale activo.

---

## 7. Seed data (catálogos)

### 7.1 Vacunas (vaccine)

| code | name_es | name_en | route | freq_months |
|---|---|---|---|---|
| BRUCELLA_RB51 | Brucella RB51 | Brucella RB51 | SC | 0 (una sola vez ternera 3-8m) |
| IBR_BVD_PI3_BRSV | IBR/BVD/PI3/BRSV (Bovi-Shield) | IBR/BVD/PI3/BRSV (Bovi-Shield) | IM | 12 |
| LEPTOSPIRA_PENTAVALENTE | Leptospira Pentavalente | Leptospira Pentavalent | IM | 6 |
| CARBON_SINTOMATICO | Carbon sintomatico (Clostridiosis) | Blackleg (Clostridiosis) | SC | 12 |
| RABIA_BOVINA | Rabia bovina | Bovine rabies | IM | 12 |
| PASTEURELLA | Pasteurella multocida | Pasteurella multocida | SC | 12 |
| FIEBRE_AFTOSA | Fiebre aftosa | Foot-and-mouth disease | IM | 6 |
| ANTHRAX | Antrax | Anthrax | SC | 12 |
| MASTITIS_J5 | Mastitis Coliforme J5 | Coliform Mastitis J5 | IM | 6 |

### 7.2 Enfermedades (disease)

| code | name_es | name_en | category | zoonotic | severity |
|---|---|---|---|---|---|
| MASTITIS | Mastitis | Mastitis | BACTERIAL | false | MEDIUM |
| BRD | Complejo respiratorio bovino | Bovine Respiratory Disease | BACTERIAL | false | HIGH |
| DIARREA_NEONATAL | Diarrea neonatal | Neonatal diarrhea | BACTERIAL | false | HIGH |
| COJERA | Cojera | Lameness | MECHANICAL | false | MEDIUM |
| BRUCELOSIS | Brucelosis | Brucellosis | BACTERIAL | true | HIGH |
| TUBERCULOSIS | Tuberculosis bovina | Bovine tuberculosis | BACTERIAL | true | HIGH |
| LEPTOSPIROSIS | Leptospirosis | Leptospirosis | BACTERIAL | true | HIGH |
| FIEBRE_AFTOSA | Fiebre aftosa | Foot-and-mouth disease | VIRAL | false | HIGH |
| IBR | Rinotraqueitis infecciosa bovina | Infectious bovine rhinotracheitis | VIRAL | false | MEDIUM |
| BVD | Diarrea viral bovina | Bovine viral diarrhea | VIRAL | false | HIGH |
| ANAPLASMOSIS | Anaplasmosis | Anaplasmosis | PARASITIC | false | HIGH |
| BABESIOSIS | Piroplasmosis (Babesiosis) | Babesiosis | PARASITIC | false | HIGH |
| ACETOSIS | Acetonemia | Ketosis | METABOLIC | false | MEDIUM |
| HIPOCALCEMIA | Hipocalcemia (Fiebre de leche) | Milk fever | METABOLIC | false | HIGH |
| METRITIS | Metritis | Metritis | BACTERIAL | false | MEDIUM |
| RETENCION_PLACENTA | Retencion placentaria | Retained placenta | OTHER | false | MEDIUM |

### 7.3 Medicamentos (medication)

| code | name_es | name_en | active_ingredient | withdrawal_milk | withdrawal_meat |
|---|---|---|---|---|---|
| OXITETRACICLINA_LA | Oxitetraciclina LA | Long-acting Oxytetracycline | Oxitetraciclina | 21 | 28 |
| PENICILINA | Penicilina G procainica | Penicillin G procaine | Penicillin G | 3 | 14 |
| ENROFLOXACINA | Enrofloxacina | Enrofloxacin | Enrofloxacin | 4 | 14 |
| IVERMECTINA | Ivermectina 1% | Ivermectin 1% | Ivermectin | 28 | 35 |
| FLUNIXIN | Flunixin meglumine | Flunixin meglumine | Flunixin meglumine | 2 | 4 |
| CEFTIOFUR | Ceftiofur | Ceftiofur | Ceftiofur | 0 | 4 |
| AMOXICILINA | Amoxicilina | Amoxicillin | Amoxicillin | 4 | 25 |
| DEXAMETASONA | Dexametasona | Dexamethasone | Dexamethasone | 3 | 14 |
| TIAMULINA | Tiamulina | Tiamulin | Tiamulin | 1 | 5 |
| CLORSULON | Clorsulon (fasciolicida) | Clorsulon (flukicide) | Clorsulon | 30 | 8 |

### 7.4 Plagas (pest)

| code | name_es | name_en | scientific | type | region |
|---|---|---|---|---|---|
| GARRAPATA_COMUN | Garrapata comun | Common cattle tick | Rhipicephalus microplus | TICK | TROPICAL |
| MOSCA_CUERNO | Mosca del cuerno | Horn fly | Haematobia irritans | FLY | ANY |
| MOSCA_BRAVA | Mosca brava | Stable fly | Stomoxys calcitrans | FLY | ANY |
| GUSANO_BARRENADOR | Gusano barrenador | Screwworm | Cochliomyia hominivorax | WORM | TROPICAL |
| PIOJO_BOVINO | Piojo bovino | Cattle louse | Haematopinus eurysternus | LICE | TEMPERATE |
| ACARO_SARCOPTICO | Acaro sarcoptico | Sarcoptic mite | Sarcoptes scabiei | MITE | ANY |
| GASTERINTESTINALES | Parasitos gastrointestinales | Gastrointestinal parasites | varios | WORM | ANY |
| FASCIOLA | Fasciola hepatica | Liver fluke | Fasciola hepatica | WORM | TEMPERATE |

### 7.5 Health plans globales del sistema

Tres planes seed (account_id = NULL):

1. **Plan Estandar Lecheria (Dairy)**: aplica a DAIRY, ANY sexo
   - Step 1: Brucella RB51 a 4 meses (FEMALE solo — pero como sex se filtra en aplicación, marcamos applies_to_sex=FEMALE)
   - Step 2: IBR/BVD/PI3/BRSV a 6 meses, recurrencia 12 meses
   - Step 3: Leptospira a 6 meses, recurrencia 6 meses
   - Step 4: Carbon sintomatico a 4 meses, recurrencia 12 meses
   - Step 5: Mastitis J5 a 24 meses (preparto), recurrencia 6 meses

2. **Plan Estandar Engorda (Beef)**: aplica a BEEF, ANY sexo
   - Step 1: IBR/BVD a 6 meses, recurrencia 12 meses
   - Step 2: Carbon sintomatico a 4 meses, recurrencia 12 meses
   - Step 3: Pasteurella a 6 meses, recurrencia 12 meses

3. **Plan Tropical Cebuino**: aplica a ANY, ANY sexo, region tropical
   - Igual al beef + Anthrax + control garrapata periódico (este último es pest_control, no vaccine, así que se documenta en `description`)

---

## 8. Definición de Fase 2 terminada

1. Migración Flyway aplica limpio sobre BD vacía y con datos de Fase 1.
2. Usuario puede crear vacunación individual y por lote (expandida correctamente).
3. Tratamiento calcula y muestra correctamente `withdrawal_milk_until` y `withdrawal_meat_until`.
4. Visita veterinaria agrupa varios eventos del mismo día.
5. Plan sanitario asignado dispara alertas de "vacunas faltantes" en el endpoint de alertas.
6. Tab "Salud" en detalle de animal carga vacunas, diagnoses y tratamientos del animal.
7. Dashboard `/dashboard` muestra las nuevas cards y consume `/dashboard/health`.
8. Página `/health` muestra alertas agrupadas correctamente.
9. i18n ES/EN completo para todas las strings nuevas.
10. Roles aplicados: VIEWER no puede crear, WORKER no puede borrar, plan management requiere ADMIN.
11. Aislamiento multi-tenant validado: cuenta A no ve eventos sanitarios de cuenta B.
12. Costo veterinario MTD coincide con suma manual de los cuatro orígenes.
