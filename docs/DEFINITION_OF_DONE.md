# Definition of Done — Digital Cow Fase 1

Esta lista corresponde al §9 del spec
`docs/superpowers/specs/2026-05-16-digital-cow-fase1-design.md` y a la Task 54
del plan de implementacion
`docs/superpowers/plans/2026-05-16-digital-cow-fase1-plan.md`.

Cada item se marca como:

- `[ ]` PENDIENTE de verificar en un entorno real (Docker Compose levantado con
  MySQL, Cloudinary, SMTP/dev-logger). El codigo de la Fase 1 esta implementado;
  la verificacion end-to-end requiere ejecutar el stack.
- `[x]` VERIFICADO contra el sistema desplegado.

Estos items NO fueron ejecutados durante la generacion del codigo (la sesion
de implementacion solo crea/modifica archivos; no levanta contenedores, no
corre el backend ni el frontend, no envia correos reales).

## Checklist DoD

- [ ] **DoD #1 — Auth lifecycle.** Registro publico de cuenta crea
  `Account` + `AppUser` con rol `OWNER`. El correo de verificacion se entrega
  (o se loguea en dev). El token verifica el correo. Login devuelve
  `accessToken` + `refreshToken`. Logout revoca el refresh token.
- [ ] **DoD #2 — Rancho y lote.** Owner puede crear rancho con campos basicos
  y un lote dentro de ese rancho. Las listas reflejan ambos.
- [ ] **DoD #3 — Equipo y roles.** Owner/Admin pueden invitar usuarios con
  cada rol (`OWNER`, `ADMIN`, `MANAGER`, `WORKER`, `VIEWER`). El invitado
  acepta con token y queda activo con el rol correcto. Los roles inferiores
  ven solo las acciones que les corresponden.
- [ ] **DoD #4 — Animales CRUD.** Crear animal con todos sus campos,
  editar, y cambiar `status` (incluyendo `SOLD`/`DEAD`) se persisten. El
  borrado fisico esta restringido y se prefiere cambio de estado.
- [ ] **DoD #5 — Fotos.** Subir foto via firma server-side de Cloudinary
  funciona desde desktop y desde mobile (`capture="environment"`). Marcar
  como `cover_photo` actualiza el animal. Eliminar foto la quita.
- [ ] **DoD #6 — Dashboard real.** `/dashboard` consume
  `GET /api/v1/dashboard/summary` y muestra totales y graficas (raza,
  proposito, rancho, altas 30 dias). Cache backend invalida al cambiar
  animales.
- [ ] **DoD #7 — Bilingue ES/EN.** El switcher cambia el idioma de toda la
  UI inmediatamente. La preferencia persiste por usuario en backend
  (`PATCH /me { locale }`) y en `localStorage`. Errores backend se traducen
  via `messageKey`.
- [ ] **DoD #8 — PWA instalable.** `manifest.webmanifest`, service worker
  y los iconos 192/512 cargan sin errores en DevTools -> Application. La
  app se puede instalar como PWA. (Nota: los binarios PNG `icon-192.png` /
  `icon-512.png` deben generarse manualmente; ver
  `frontend/public/icons/README.md`.)
- [ ] **DoD #9 — Multi-tenancy aislado.** Dos cuentas independientes no ven
  los datos de la otra: animales, ranchos, lotes, equipo. Probado con dos
  registros y un animal por cuenta.
- [ ] **DoD #10 — Super-admin.** `/admin/login` autentica al super-admin
  bootstrap. `/admin/accounts` lista todas las cuentas. Cambiar `status`
  a `INACTIVE` bloquea el login del owner correspondiente.
- [ ] **DoD #11 — CI verde.**
  - Backend: `cd backend && ./mvnw verify` -> BUILD SUCCESS.
  - Frontend: `cd frontend && npm run typecheck && npm run lint && npm test
    && npm run build` -> todo OK.
- [ ] **DoD #12 — Bootstrap desde cero.**
  `docker compose down -v && docker compose up --build` levanta MySQL,
  backend, frontend y Adminer; las migraciones Flyway corren; el seed de
  razas y el bootstrap del super-admin se aplican; el frontend responde
  en `http://localhost:5173`.

## Notas

- Verificar siempre con la version mas reciente del plan
  (`docs/superpowers/plans/2026-05-16-digital-cow-fase1-plan.md`,
  Task 54) y del spec (`§9`).
- Si algun item falla durante la verificacion, abrir el repo y registrar
  el fallo, no marcar como `[x]`.
- Los iconos PWA `icon-192.png` y `icon-512.png` no se versionan; sin
  ellos, el manifest funciona pero la instalacion mostrara icono por
  defecto.
- Las imagenes PNG, los correos SMTP reales y los uploads a Cloudinary
  requieren credenciales validas en `.env`.

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

## Fase 3 — Reproduccion

- [ ] Migraciones V9 (extension de `animal`) y V10 (catalogos + eventos) aplican limpio.
- [ ] Crear bull (OWN + EXTERNAL), semen_straw, heat, service AI con decremento de pajilla.
- [ ] Pregnancy check POSITIVE calcula `estimated_calving_date`.
- [ ] Calving con `createCalfAnimal=true` crea Animal hijo con `sire_id`/`dam_id` enlazados.
- [ ] KPIs reproductivos (mediana dias abiertos, IEP, edad 1er parto, tasa concepcion, services/concepcion, tasa preñez) retornan valores coherentes.
- [ ] Alertas muestran proximos partos, dry-off due, sin check, vacas vacias.
- [ ] Tab Reproduccion en `/animals/:id` muestra timeline cronologico.
- [ ] Dashboard `/dashboard` muestra cards de reproduccion.
- [ ] i18n ES/EN completo para `reproduction` y `reproductionAlerts`.
- [ ] Aislamiento multi-tenant validado.
- [ ] Roles aplicados.

Codigo entregado (no validado en build):
- Backend: 11 paquetes en `com.digitalcow.reproduction.*` (bull, semen, heat, service, pregnancy, calving, abortion, weaning, dryoff, alerts, kpis) + `dashboard/DashboardReproduction*`.
- Frontend: 11 paginas en `pages/reproduction/` + features completas + tab Reproduccion en AnimalDetailPage + widgets en DashboardPage.

## Fase 4 — Produccion y Alimentacion

- [ ] Migraciones V11 (produccion) y V12 (alimentacion) aplican limpio sobre BD vacia y sobre BD con datos de Fases 1-3.
- [ ] Crear weighing -> ADG aparece en growth-curve correctamente.
- [ ] Crear milking diario -> lactation-curve dibuja la curva (dias de lactancia vs litros).
- [ ] Bulk milking permite registrar 10+ animales en una sesion (TOTAL/AM/PM) en una sola llamada.
- [ ] Slaughter calcula yield_pct automaticamente cuando vienen liveWeightKg y carcassWeightKg.
- [ ] Catalogo seed de 6 feed_items globales (alfalfa, sorgo, maiz molido, soya, sal mineral, ensilaje maiz) presente.
- [ ] Feeding plan creado + asignado a lote + 3 feeding_records -> cost-summary refleja gasto correcto agrupado por lote/rancho/mes.
- [ ] Tab Produccion en /animals/:id carga pesajes, milkings (si dairy/dual), milk-samples y slaughter (si aplicable) + mini grafico de crecimiento.
- [ ] Dashboard `/dashboard` muestra cards de produccion (leche hoy, MTD, ADG promedio, vacas en ordeño activas).
- [ ] i18n ES/EN completo para namespaces `production` y `feeding`.
- [ ] Aislamiento multi-tenant validado: cuenta A no ve pesajes / ordeños / planes / consumos de cuenta B.
- [ ] Roles aplicados: VIEWER no puede crear; WORKER no puede borrar; planes y catalogos requieren ADMIN o superior; items globales son solo lectura para todas las cuentas.

## Fase 5 — Economia y Reportes

- [ ] Migracion V13 (catalogos + transacciones + ventas) aplica limpio.
- [ ] Seed de expense_categories e income_categories cargado.
- [ ] Crear `expense` manual + ver en lista filtrada.
- [ ] Crear `animal_sale` cambia `animal.status='SOLD'` y crea `income` automatico.
- [ ] Borrar `animal_sale` revierte status y borra income (auditado).
- [ ] `/finance/pnl` retorna ingresos + egresos (incluyendo costos importados de salud/repro/alimentacion) coherentes.
- [ ] `/finance/animal-roi/{id}` desglosa correctamente costos imputados.
- [ ] Dashboard `/dashboard` muestra cards de finanzas.
- [ ] `/finances` overview con BarChart 12 meses y PieChart por categoria.
- [ ] `/reports/pnl` permite descargar CSV.
- [ ] `/reports/animal/:id` se imprime sin sidebar/navbar.
- [ ] i18n ES/EN completo. Aislamiento multi-tenant. Roles aplicados.

Codigo entregado (no validado en build):
- Frontend: 5 paginas en `pages/finance/` (Overview, Expenses, Incomes, AnimalSales, MilkSales, Categories) + 5 paginas en `pages/reports/` (Pnl, Inventory, Animal, SalesHistory, HealthSummary) + features `finance/{categories,expenses,incomes,animalSales,milkSales,pnl,cashFlow,animalRoi,costPerUnit,dashboard}` y `reports/{animalReport,pnlReport,inventoryReport,salesHistory,healthSummary}` + helper `lib/csv.ts` + tab Finanzas en AnimalDetailPage + seccion Finanzas en DashboardPage + reglas `@media print` en `index.css` + clases `print:hidden`/`print:p-0` en `AppLayout.tsx`.
