# Fase 6 — Simplificacion, Accesibilidad y UX Unificada

Fecha: 2026-05-17
Autor: noel (con Claude)
Estado: spec en revision
Fases anteriores: 1 (plataforma + animales), 2 (salud), 3 (reproduccion), 4 (produccion + alimentacion), 5 (economia y reportes) — todas con codigo completo.

## 1. Objetivo

Rediseñar la experiencia de Digital Cow para que sea simple, visual y accesible. La plataforma actual tiene 11 secciones en la sidebar y ~40 sub-vistas anidadas en tabs; el catalogo, la salud, la alimentacion, las finanzas y los reportes viven cada uno en su panel con sus propios tabs internos. Aunque cada panel funciona, el conjunto se percibe disperso y dificil de aprender, especialmente para usuarios de campo con baja alfabetizacion.

Esta fase NO añade dominios funcionales nuevos relevantes (los dominios ya existen). Reorganiza la UI, simplifica los flujos de captura, añade ayudas visuales y dos capacidades funcionales menores que el usuario pidio expresamente: catalogo de medicamentos con escaneo de codigo de barras y graficas comparativas por animal.

## 2. Alcance

### Dentro de alcance

1. **Nueva arquitectura de informacion**: aplanar la navegacion, agrupar por tareas en vez de por entidades.
2. **Shell tipo aplicacion**: barra inferior fija de 5 destinos en movil, sidebar lateral colapsada en escritorio.
3. **Paneles unificados**: Salud, Alimentacion y Dinero pasan de N tabs a UNA pagina compuesta con secciones colapsables.
4. **Launcher "Hacer una nota"**: punto unico de captura con wizards de un paso por pregunta, iconos grandes, texto plano, lectura por voz opcional.
5. **Accesibilidad para baja alfabetizacion**: iconos por accion, color como significado, ilustraciones, audio (Web Speech API), confirmaciones visuales y texto en lenguaje simple.
6. **Catalogo visual de animales**: foto-thumbnail en cada fila, vista de tarjetas, vista por lote con ocupacion visual, vista mapa con pin por rancho.
7. **Compra / venta de animales como flujo integrado** (no escondida en tabs de Finanzas).
8. **Formularios mas descriptivos**: cada campo con label, helper text, ejemplo, icono y donde aplica imagen ilustrativa.
9. **Catalogo de medicamentos** con escaneo de codigo de barras (lookup local + servicio externo opcional con cache).
10. **Graficas comparativas por animal**: peso contra alimento contra gasto contra ingreso a lo largo del tiempo.
11. **Foto del animal omnipresente**: hero en detalle, thumbnail en listas, avatar en cualquier referencia (filas de salud, finanzas, alimentacion, reportes).
12. **Migracion de envio de correos a Resend**: reemplazar el proveedor actual por Resend manteniendo las mismas plantillas y casos de uso (verificacion de email, invitaciones, restablecimiento de password, alertas).
13. **Configuracion segura de Cloudinary**: las credenciales se inyectan exclusivamente por variables de entorno; nunca se versionan; se documenta en README.

### Fuera de alcance

- Cambios al stack o a las decisiones globales (ver `project-digital-cow-status`).
- Migracion a un sistema de diseño distinto a shadcn/ui.
- Aplicacion nativa (la PWA actual sigue siendo el envoltorio movil).
- Reescritura del backend; solo se añaden migraciones nuevas y endpoints nuevos donde sea estrictamente necesario.
- Voz como sustituto del texto: la lectura por voz es ayuda, no input. El input por voz queda fuera.
- Cambios en los KPIs reportados ni en los calculos financieros existentes.

## 3. Decisiones de diseño clave

| # | Decision | Razon |
|---|----------|-------|
| D1 | Navegacion principal de 5 destinos | Cumple regla de oro de navegacion movil; reduce carga cognitiva |
| D2 | Eliminar tabs anidados de paneles | El usuario reporta que los tabs internos son la fuente principal de confusion |
| D3 | Wizards de un paso por pregunta para captura | Reduce intimidacion para usuarios con baja alfabetizacion |
| D4 | Iconos siempre acompañados de etiqueta corta | "Icon-only" excluye a quien no conoce la convencion del icono |
| D5 | Lectura por voz via Web Speech API en el navegador | Cero backend, cero costo, funciona offline |
| D6 | Modo simple es el modo por defecto, no opt-in | Si funciona para usuarios con baja alfabetizacion, funciona para todos |
| D7 | "Ver mas / Modo avanzado" expone campos opcionales sin sacarlos del flujo | No esconder funcionalidad de usuarios power |
| D8 | Catalogo de medicamentos = tabla local + lookup externo opcional | Funciona offline; el escaneo solo enriquece |
| D9 | Mapa de animales usa lat/lng del rancho (no GPS por animal) | Datos ya existen a nivel rancho; geolocalizacion individual queda fuera |
| D10 | Foto del animal cacheada por TanStack Query, con fallback a placeholder con la marca del animal | Funciona sin Cloudinary disponible |
| D11 | Sin abreviaciones en ningun texto visible al usuario | El usuario lo exige; abreviaturas como "ej.", "etc.", "kg/dia" excluyen a quien no las conoce; siempre usar palabras completas |
| D12 | Resend como unico proveedor de email saliente | Mejor entregabilidad, API moderna, plantillas React; reemplaza el proveedor anterior por completo |
| D13 | Secretos exclusivamente por variables de entorno; nunca en codigo ni en archivos versionados | Buena practica universal; aplicable a Cloudinary, Resend, JWT, base de datos |

## 4. Nueva arquitectura de informacion

### 4.1 Cinco destinos principales

La barra inferior fija (movil) y la sidebar (escritorio) muestran exactamente cinco destinos:

1. **Inicio** — vista del dia: alertas, tareas pendientes, tres numeros clave (animales activos, balance del mes, vacunas atrasadas). Reemplaza el dashboard actual (largo y apilado).
2. **Animales** — catalogo visual mas busqueda por marca o nombre, mapa, venta y compra. Incluye la lista, el detalle, los lotes y la geografia.
3. **Hacer una nota** — boton central destacado. Abre el launcher de wizards (vacunar, pesar, vender, gastar, alimentar y similares). Reemplaza la entrada dispersa por formularios sueltos en cada panel.
4. **Panel** — concentra las areas de gestion y sus reportes. Sub-paginas: Salud, Alimentacion, Dinero, Reproduccion, Produccion. Cada sub-pagina es una vista compuesta con KPIs grandes, una grafica principal, accesos a "Ver todo" para tablas profundas y a las graficas comparativas por animal.
5. **Ajustes** — perfil, cuenta, ranchos, lotes, equipo, categorias de gastos e ingresos, catalogo de medicamentos, idioma, tema, salir. Antes vivian en cuatro lugares.

Reproduccion y Produccion son sub-paginas del Panel y tambien viven dentro del flujo del animal (detalle del animal -> pestaña "Reproduccion" o "Produccion") y dentro de "Hacer una nota" (registrar celo, registrar pesaje y similares). Esto refleja el hecho de que estos eventos pertenecen al animal; tener accesos desde varios contextos es deliberado para acercarlos al usuario donde sea que este trabajando.

### 4.2 Mapa de las rutas viejas a las nuevas

```
/dashboard                    -> /inicio
/animals                      -> /animales
/animals/:id                  -> /animales/:id   (con tabs internos: General, Salud, Alimentacion, Produccion, Reproduccion, Finanzas)
/animals/new                  -> /animales/nuevo  (wizard)
/ranches, /ranches/:id        -> /ajustes/ranchos y /ajustes/ranchos/:id
/team                         -> /ajustes/equipo
/health                       -> /panel/salud  (vista unica, no tabs)
/health/*                     -> redirect a /panel/salud o a /hacer-nota/...
/feeding                      -> /panel/alimentacion
/feeding/*                    -> redirect
/finance                      -> /panel/dinero
/finance/*                    -> redirect; /finance?tab=animal-sales -> /animales (boton vender)
/finance?tab=categories       -> /ajustes/categorias
/reproduction                 -> /panel/reproduccion
/production                   -> /panel/produccion
/reports                      -> /panel
/reports/animal/:id           -> /animales/:id?seccion=reporte
/settings/*                   -> /ajustes/*
```

Toda ruta vieja sigue funcionando vía redirect (`<Navigate replace>`) para no romper bookmarks ni deep-links externos.

### 4.3 Estructura visual del shell

- **Header**: marca a la izquierda; cambiador de idioma, tema y usuario a la derecha. Se oculta al imprimir.
- **Movil**: bottom-nav fija de 5 destinos. El destino central ("Hacer una nota") es un FAB destacado (mas grande, color de marca, icono lapiz).
- **Escritorio**: sidebar lateral colapsable de 64 px o 240 px. Mismos 5 destinos.
- **Area principal**: padding consistente, breadcrumbs solo cuando hay mas de 2 niveles.

## 5. Componentes UI nuevos

Todos viven en `frontend/src/components/` o `frontend/src/components/ui/`. Reutilizan Radix + Tailwind via shadcn (sin cambio de sistema).

| Componente | Proposito | Notas |
|------------|-----------|-------|
| `BottomNav` | Barra inferior fija movil | 5 slots; FAB central |
| `AppShell` | Reemplaza `AppLayout` | Maneja bottom-nav vs sidebar segun viewport |
| `IconCard` | Card grande con icono + titulo + 1 linea | Base de los wizards y launcher |
| `WizardStep` | Un paso de wizard | Encabezado con paso N de M, contenido grande, botones Atras / Siguiente |
| `BigPicker` | Selector tipo grid de tarjetas en vez de `<select>` | Para opciones cortas (sexo, proposito, motivo) |
| `BigButton` | Boton de 56 px minimo con icono + label | Cumple WCAG 2.5.5 |
| `AnimalAvatar` | Foto circular del animal con fallback a iniciales sobre color de marca | Reutilizable en cualquier referencia |
| `AnimalCard` | Tarjeta para grids: foto, marca, nombre, rancho, estado | Usado en lista visual y por lote |
| `AnimalListRow` | Fila con thumbnail + marca + chips de estado | Default en /animales |
| `RanchMap` | Mapa Leaflet con pin por rancho, click abre lista de animales | Solo si rancho tiene lat/lng |
| `ComparisonChart` | Grafico multi-serie por animal (peso/alimento/gasto/ingreso) | Recharts; toggles por serie |
| `BarcodeScanner` | Componente que abre camera y emite codigo escaneado | Libreria `@zxing/browser` |
| `MedicineSearchInput` | Input combinado: texto + boton "Escanear" | Usa BarcodeScanner |
| `HelpfulField` | Wrapper de input con label, icono, helper text, ejemplo, lectura por voz | Reemplaza el patron de `<label>+<input>` actual |
| `SpeakButton` | Boton pequeño que lee en voz alta el texto adyacente | Web Speech API |
| `EmptyState` | Estado vacio con ilustracion, texto simple y CTA grande | Reemplaza los `<p>{t('empty')}</p>` |
| `QuickActionLauncher` | Pantalla de "Hacer una nota" | Grid de IconCards |

## 6. "Hacer una nota" — launcher y wizards

El destino central de la nav abre un launcher con 8 a 12 acciones en grid de tarjetas grandes (3 columnas en escritorio, 2 en movil), cada una con icono, titulo y una linea de descripcion:

1. **Vacuné** — registra vacunacion (individual o por lote).
2. **Diagnostique** — registra enfermedad o sintoma.
3. **Traté** — registra tratamiento (medicamento + dosis).
4. **Pesé** — registra pesaje.
5. **Ordeñé** — registra ordeño (individual o tanque).
6. **Alimenté** — registra consumo de alimento.
7. **Compré animal** — flujo de alta de animal con datos de compra.
8. **Vendí animal** — flujo de baja con datos de venta.
9. **Gasté** — registra gasto.
10. **Recibí dinero** — registra ingreso.
11. **Vi un celo** — registra deteccion de celo.
12. **Detecté preñez** — registra chequeo de preñez.

Cada accion abre un wizard con la siguiente estructura comun:

- **Paso 1**: ¿A cual animal? (busqueda con AnimalAvatar + marca, o seleccion multiple para lote)
- **Paso 2**: ¿Que paso? (campos especificos de la accion, con BigPickers cuando aplique)
- **Paso 3**: Detalles opcionales (colapsable "Ver mas")
- **Paso 4**: Confirmacion visual (animal + accion + fecha + costo si aplica) con un boton grande "Guardar"

Reglas:
- Una sola pregunta principal por paso.
- Todo paso tiene un boton "Atras" siempre visible.
- El boton "Siguiente" se habilita solo cuando el paso es valido.
- El usuario puede saltar a "Confirmacion" en cualquier momento (boton "Listo, guardar" si solo lo basico esta lleno).
- Despues de guardar, toast verde con SpeakButton ("Guardado: vacune a Estrella") y opcion "Hacer otra nota" o "Volver al inicio".

Los wizards llaman a los mismos endpoints existentes (`/vaccinations`, `/treatments`, `/animal-sales` y similares). No hay endpoints nuevos por esto.

## 7. Accesibilidad para baja alfabetizacion

### 7.1 Principios

- **Texto simple y corto**: maximo nivel de lectura aproximado grado 5 (verbos en imperativo, frases de menos de 12 palabras).
- **Sin abreviaciones**: nunca usar "kg", "ej.", "etc.", "Dr.", "Sr.", "vs.", "min.", "max." en texto visible. Siempre palabras completas: "kilogramos", "por ejemplo", "y mas", "doctor", "señor", "comparado con", "minimo", "maximo". Las unidades cortas usadas en graficas y tablas (donde el espacio es limitado) se etiquetan en pleno en titulos, leyendas y tooltips.
- **Icono SIEMPRE acompañado de etiqueta**: nunca icono solo (excepto el menu de usuario, que tiene tooltip).
- **Color como significado**: verde igual a bueno o completado, rojo igual a atrasado o problema, amarillo igual a atencion, gris igual a neutral. Cada chip de color repite la informacion en texto.
- **Confirmacion visual al guardar**: animacion breve mas toast con icono y opcion de escuchar.
- **Lectura por voz**: cada `HelpfulField` y cada wizard step tiene un boton pequeño "escuchar". Usa `window.speechSynthesis` con el idioma actual (i18n.language).
- **Imagenes ilustrativas en campos clave**: sexo (icono hembra y macho grande), proposito (vaca lechera, vaca de carne, doble proposito), estado (verde activo, gris vendido y similares).
- **Numeros con unidades grandes y visibles**: "350 kilogramos" en titulares y formularios; "350 kg" solo en celdas de tabla densa o ejes de grafica donde la leyenda completa va aparte.
- **Fechas en lenguaje natural**: "hoy", "ayer", "hace tres dias" cuando aplique; calendario visual con fines de semana marcados.

### 7.2 Cumplimiento WCAG

- Contraste minimo AAA en texto principal, AA en secundario.
- Targets tactiles minimo 44x44 px (BigButton es 56 px).
- Foco visible con outline de 2 px en color de marca.
- `aria-label` obligatorio en todo boton icon-only excepcional.
- `aria-live="polite"` en toasts.
- Navegacion por teclado completa.
- `prefers-reduced-motion` respetado.

### 7.3 Modo voz (no input)

Switch global en Ajustes "Leer en voz alta" (off por defecto). Cuando esta on:
- Wizard reads el titulo del paso al entrar.
- Toast lee el mensaje al aparecer.
- SpeakButton siempre visible junto a labels.
- No hay input por voz (queda fuera).

## 8. Animales: catalogo visual y compra/venta

### 8.1 Vistas del catalogo

`/animales` tiene tres modos, conmutables por un toggle de iconos:

- **Lista** (default en escritorio): tabla con thumbnail (40 px), marca, nombre, sexo (icono), raza, rancho, estado (chip). Click va al detalle.
- **Tarjetas** (default en movil): grid de `AnimalCard` (foto 1:1, marca grande, chip de estado).
- **Mapa**: `RanchMap` con un pin por rancho; click muestra popover con conteo y boton "ver animales de aqui".

### 8.2 Filtros simplificados

Reemplazar los 5 selects sueltos por:
- Una barra de busqueda grande con icono lupa, placeholder "Marca, nombre, raza, rancho...".
- Un boton "Filtros" que abre un drawer con BigPickers (Rancho, Raza, Sexo, Proposito, Estado).
- Los filtros activos aparecen como chips removibles bajo la busqueda.

### 8.3 Detalle del animal

Hero de imagen 16:9 con la foto principal (`AnimalAvatar` extendido). Carrusel horizontal de fotos. Tabs internos cortos: General, Salud, Alimentacion, Produccion, Reproduccion, Finanzas, Linea de tiempo.

- **General**: datos basicos editables inline.
- **Salud, Alimentacion, Produccion, Reproduccion, Finanzas**: lo que ya existe pero con `AnimalAvatar` en cada item, fotos referenciadas, accion rapida "Añadir nota" que abre el wizard apropiado.
- **Linea de tiempo**: vista cronologica de todos los eventos del animal (vacunas, pesajes, tratamientos, cambios de lote, ventas, gastos) con icono y resumen de una linea.

### 8.4 Compra / Venta integradas

Compra: `/animales/nuevo` reemplaza el form actual de animal por un wizard de 4 pasos:
1. Foto (camara o subir).
2. ¿Quien es? (marca, nombre, sexo, raza).
3. ¿De donde viene? (rancho, lote, ¿es compra? si/no, si si: precio, vendedor, fecha).
4. Confirmacion.

Si en el paso 3 marca "es compra", el wizard crea ATOMICAMENTE el animal y un registro de gasto en la categoria "Compra de animales". Endpoint nuevo: `POST /api/animals/with-purchase` que crea ambos en una transaccion.

Venta: boton "Vender" en el detalle del animal y accion "Vendi animal" en el launcher. Wizard de 3 pasos:
1. Animal (preseleccionado si vino del detalle).
2. ¿A quien y por cuanto? (comprador, fecha, precio).
3. Confirmacion.

Marca el animal como `SOLD`, crea el income en categoria "Venta de animales". Reutiliza el endpoint existente `POST /api/animal-sales` mas la transicion de estado del animal en una transaccion.

## 9. Salud unificada y catalogo de medicamentos con escaneo

### 9.1 Pagina unica de salud

`/panel/salud` muestra en una sola vista:
- **3 KPIs grandes** en la parte superior: vacunas atrasadas, diagnosticos activos, gasto veterinario del mes.
- **Alertas** (lista con AnimalAvatar y accion rapida "Marcar vacunado" / "Programar").
- **Grafica unica**: eventos por mes (barras apiladas: vacunaciones, diagnosticos, tratamientos).
- **Tabla colapsable por tipo**: Vacunaciones, Diagnosticos, Tratamientos, Plagas, Visitas vet, Planes. Cada tabla con boton "Ver todo" que la expande a pagina completa si el usuario lo necesita.

Las tablas siguen existiendo (las vistas actuales `VaccinationsPage` y sus pares quedan como subpaginas accesibles desde "Ver todo"). El cambio es que el panel principal no las apila como tabs; las muestra como secciones colapsadas.

### 9.2 Catalogo de medicamentos

Nueva tabla `medicine_catalog` (entidad nueva en `com.digitalcow.health.medicine`):

```sql
CREATE TABLE medicine_catalog (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  name VARCHAR(160) NOT NULL,
  active_substance VARCHAR(240),
  presentation VARCHAR(120),     -- "inyectable 100 ml", "bolo", etc.
  manufacturer VARCHAR(160),
  barcode_ean13 VARCHAR(13),     -- nullable, indexado
  barcode_other VARCHAR(60),     -- otros formatos (UPC, etc.)
  category ENUM('VACCINE','ANTIBIOTIC','ANTIPARASITIC','HORMONE','VITAMIN','OTHER') NOT NULL,
  withdrawal_days_meat INT,      -- dias de retiro en carne
  withdrawal_days_milk INT,
  default_dose VARCHAR(80),
  notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_medicine_account (account_id),
  INDEX idx_medicine_barcode (account_id, barcode_ean13)
);
```

Migracion: `V14__medicine_catalog.sql`.

Endpoints REST (modulo `com.digitalcow.health.medicine`):
- `GET /api/medicines` — listar con filtros (texto, categoria).
- `GET /api/medicines/{id}` — detalle.
- `GET /api/medicines/by-barcode/{barcode}` — lookup por barcode (404 si no existe).
- `POST /api/medicines` — crear.
- `PUT /api/medicines/{id}` — editar.
- `DELETE /api/medicines/{id}` — soft delete via flag o eliminacion fisica si no esta referenciada.

Referencias: `VaccinationEntity` y `TreatmentEntity` ganan columna opcional `medicine_id BIGINT NULL` con FK a `medicine_catalog`. Migracion `V15__health_medicine_link.sql`.

Validacion: `withdrawal_days_meat/milk` se usa para mostrar advertencia visual en sacrificios y ventas durante el periodo de retiro (calculo en frontend; no bloquea, solo advierte).

### 9.3 Escaneo de codigo de barras

Frontend usa `@zxing/browser` (libreria mantenida, sin tracking). Flujo:

1. En cualquier wizard que pida medicamento, el `MedicineSearchInput` muestra input de texto + boton camara.
2. Click camara abre modal con preview de video y target visual.
3. ZXing emite el codigo; el modal cierra.
4. Frontend hace `GET /api/medicines/by-barcode/{codigo}`.
5. Si existe: precarga el form con los datos.
6. Si no existe: pregunta "No conozco este medicamento. ¿Lo agregamos?" y abre wizard de alta del catalogo prellenando el codigo.

No hay servicio externo de lookup en Fase 6 (queda como mejora futura). El usuario va construyendo su propio catalogo.

Permisos camara: solo HTTPS; si el dispositivo niega, el modal cae a input manual con un texto explicativo.

## 10. Alimentacion unificada

`/panel/alimentacion` reemplaza los cuatro tabs actuales por una vista:

- **3 KPIs**: gasto del mes en alimento, kg de alimento del mes, costo promedio por kg.
- **Grafica de consumo por dia** (barras apiladas por tipo de item).
- **Tabla de items con costo unitario** (collapsada por defecto, expansible).
- **Tabla de planes activos** con badge por lote asignado.
- **Acciones rapidas**: "Registrar alimentacion", "Crear plan", "Añadir item".

Los detalles de plan siguen viviendo en `/feeding/plans/:id` accesible desde el listado.

## 11. Finanzas unificada

`/panel/dinero` reemplaza los seis tabs:

- **3 KPIs grandes**: ingreso del mes, gasto del mes, margen del mes (color segun signo).
- **Grafica linea ingreso vs gasto** ultimos 12 meses.
- **Top 3 categorias de gasto** del mes (chips).
- **Tabla unificada de movimientos** (ingreso o gasto, ordenados por fecha) con filtros simples: tipo, categoria, rango fechas, animal asociado.
- **Acciones rapidas**: "Registrar gasto", "Registrar ingreso", "Venta de animal", "Venta de leche".

Las vistas separadas de Expenses, Incomes, AnimalSales, MilkSales, Categories siguen existiendo accesibles desde la tabla unificada (click en un movimiento abre el detalle nativo). Categorias se mueven a Ajustes ("Ajustes > Categorias de dinero").

## 12. Graficas comparativas por animal

Componente `ComparisonChart` en el detalle del animal y en `/animales/:id?seccion=reporte`. Permite seleccionar dos a cuatro series simultaneas en el mismo eje horizontal (fecha):

- **Peso** (kg) — de la tabla `weighings`.
- **Alimento consumido** (kilogramos por dia, mostrado como "kg/dia" solo en ejes de grafica con leyenda completa aparte) — agregado de `feeding_records`.
- **Gasto acumulado** ($/mes) — agregado de expenses referenciados al animal.
- **Ingreso acumulado** ($/mes) — agregado de incomes referenciados.

Endpoint nuevo `GET /api/animals/{id}/comparison?series=weight,feed,expense,income&from=...&to=...` devuelve un payload normalizado para Recharts. Se calcula on-the-fly en backend con queries agregadas (no se crea materialized view en Fase 6).

Default: serie de peso + serie de alimento, ultimos 6 meses, agregacion mensual.

UI: toggles arriba del chart, leyenda interactiva, tooltip con valores de cada serie.

## 13. Formularios mas descriptivos

Todos los formularios migran al patron `HelpfulField`:

```
[icono] ETIQUETA (obligatorio)         [escuchar]
texto de ayuda en una linea simple
ejemplo: "ESM-042"
[input]
```

Reglas:
- Etiqueta en lenguaje plano, sin jerga ("Marca del animal" en vez de "internalTag").
- Texto de ayuda explica QUE es y POR QUE importa, maximo doce palabras.
- Ejemplo siempre presente para campos de texto libre, introducido con la palabra completa "Ejemplo:" (nunca "ej.").
- Icono semantico (etiqueta para marca, calendario para fecha, balanza para peso y similares).
- Boton "escuchar" cuando el modo voz esta activado.
- Errores en lenguaje plano: "Falta poner una marca" en vez de "internalTag is required".
- Sin abreviaciones en ninguna parte del campo (ni etiqueta, ni ayuda, ni placeholder, ni mensaje de error).

Las traducciones existentes en `frontend/public/locales/{lng}/{ns}.json` se enriquecen con claves `helper`, `example`, `errorPlain` por cada campo. Nuevo namespace `forms` con los componentes genericos. Auditoria de strings existentes: barrido para eliminar toda abreviacion en strings visibles.

## 13.5 Migracion del envio de correos a Resend

### Motivacion

Reemplazar el proveedor SMTP actual (`com.digitalcow.mail`) por Resend para mejorar entregabilidad, simplificar la configuracion y disponer de plantillas mas mantenibles.

### Cambios

- Nueva dependencia Maven: cliente HTTP minimo (`spring-web` ya disponible). Resend expone REST `POST https://api.resend.com/emails`. No requiere SDK propietario.
- Nuevo componente `ResendMailClient` en `com.digitalcow.mail` que implementa la interfaz existente `MailSender` (o equivalente actual) usando `RestClient`.
- Configuracion via `application.yml`:
  ```yaml
  digitalcow:
    mail:
      provider: resend
      from: "Digital Cow <no-reply@${MAIL_FROM_DOMAIN}>"
      resend:
        api-key: ${RESEND_API_KEY}
  ```
- Variables de entorno nuevas: `RESEND_API_KEY`, `MAIL_FROM_DOMAIN`.
- Reintentos: usar Resilience4j ya configurado para Cloudinary; aplicar el mismo patron (retry + circuit breaker) al cliente de Resend.
- Plantillas: mantener las plantillas HTML actuales (no se migran a React Email en esta fase para minimizar riesgo). Resend acepta `html` y `text` planos.
- Tests: test de integracion con `WireMockServer` simulando la API de Resend; verificar payload, headers y manejo de errores 4xx/5xx.
- DKIM, SPF, DMARC: documentar en README los DNS que el operador debe configurar para `MAIL_FROM_DOMAIN`. La validacion del dominio se hace en el panel de Resend antes del primer envio.

### Eliminacion del proveedor anterior

- El antiguo `JavaMailSender` y sus propiedades `spring.mail.*` quedan en el codigo durante una iteracion como fallback opt-in via `digitalcow.mail.provider=smtp`, luego se eliminan en la iteracion 6.10.
- Documentar el cambio en `README.md` y migrar `docker-compose.yml` para retirar variables SMTP no usadas.

## 13.6 Configuracion segura de Cloudinary y otros secretos

### Principio

Ningun secreto vive en el repositorio. Todos llegan al proceso via variables de entorno inyectadas por Docker Compose / systemd / el proveedor de hosting.

### Inventario de secretos del proyecto

| Variable | Descripcion |
|----------|-------------|
| `JWT_SECRET` | Secreto HMAC para firmar JWT |
| `DB_PASSWORD` | Password del usuario MySQL |
| `CLOUDINARY_CLOUD_NAME` | Nombre publico de la cuenta Cloudinary |
| `CLOUDINARY_API_KEY` | Clave publica de API |
| `CLOUDINARY_API_SECRET` | Secreto de API (NUNCA al cliente) |
| `RESEND_API_KEY` | Clave del proveedor de correo |
| `MAIL_FROM_DOMAIN` | Dominio validado para envio |

### Implementacion

- `application.yml` referencia siempre variables: `${VAR_NAME}` o `${VAR_NAME:default-no-secreto}`. Nunca valores literales.
- `docker-compose.dev.yml` lee de un `.env` local (gitignored). Plantilla `.env.example` versionada con nombres pero sin valores.
- `docker-compose.yml` (produccion) recibe los secretos del entorno del host.
- Validacion al arranque: si alguna variable critica (JWT, DB, Cloudinary cuando esta activada la subida de fotos, Resend cuando esta activado el envio de correos) falta, la aplicacion falla rapido con mensaje claro.
- Endpoint de firma de Cloudinary (`POST /api/photos/signature`) sigue firmando en backend con el secret; el frontend nunca lo ve.

### Rotacion

Documentar en README un procedimiento simple para rotar cada secreto. Importante para el caso en que un secreto se haya filtrado (como ocurrio en este chat con las credenciales de Cloudinary): rotar en el panel del proveedor, actualizar la variable de entorno, reiniciar el servicio.

## 14. Cambios backend resumidos

### 14.1 Migraciones nuevas

- `V14__medicine_catalog.sql` — tabla `medicine_catalog`.
- `V15__health_medicine_link.sql` — `medicine_id` en `vaccinations` y `treatments`.
- `V16__ranch_geo.sql` — `latitude DECIMAL(9,6)`, `longitude DECIMAL(9,6)` en `ranches` (nullable) si no existen ya.

### 14.2 Modulos nuevos / endpoints nuevos

- `com.digitalcow.health.medicine` (controller, service, repository, entity, dto, mapper).
- `com.digitalcow.animal.purchase` — endpoint `POST /animals/with-purchase` que delega a `animalService.create` mas `expenseService.create` en una transaccion.
- `com.digitalcow.animal.comparison` — endpoint `GET /animals/{id}/comparison` con agregaciones (peso, alimento, gasto, ingreso).
- `com.digitalcow.ranch` — añadir endpoint `PATCH /ranches/{id}/location` (latitud y longitud).
- `com.digitalcow.mail` — reemplazo de implementacion: nuevo `ResendMailClient`, retiro progresivo de `JavaMailSender`.

### 14.3 Sin cambios

- Auth, multi-tenancy, roles, billing, firma de Cloudinary (logica de signed upload), Resilience4j, MapStruct, repositorios existentes.
- Endpoints existentes de animales, vacunas, tratamientos, ventas, ingresos, gastos, planes de alimentacion y similares se siguen llamando desde los wizards.

## 15. i18n

Nuevos namespaces:
- `forms` — campos genericos, errores planos, helper text generico.
- `medicine` — catalogo de medicamentos.
- `wizard` — pasos comunes de wizards, mensajes de confirmacion.
- `voice` — strings que se leen en voz alta (versiones simplificadas si la oracion visual es muy compleja).

Los namespaces existentes (`animals`, `health`, `feeding`, `finance`, `reproduction`, `production`, `reports`, `team`, `ranches`, `auth`, `dashboard`, `common`, `errors`, `alerts`, `catalog`, `reproductionAlerts`) ganan claves `helper`, `example`, `errorPlain` por campo.

Lenguaje: español como base, ingles como secundario. Todo string en español puede llevar acentos (vive en JSON i18n). El JS/TSX no contiene strings hardcodeados.

## 16. Testing y QA

### 16.1 Tests automatizados

- Componentes nuevos (`HelpfulField`, `WizardStep`, `BigPicker`, `AnimalAvatar`, `IconCard`, `BigButton`, `EmptyState`, `SpeakButton`, `BarcodeScanner`, `MedicineSearchInput`, `ComparisonChart`, `RanchMap`, `AnimalCard`) con tests unitarios Vitest mas Testing Library.
- Wizards principales (alta de animal con compra, venta de animal, vacuné, gasté) con tests de flujo completo.
- Endpoint `POST /animals/with-purchase` con test de integracion Testcontainers que verifica la transaccionalidad (rollback si falla la creacion del expense).
- Endpoint `GET /animals/{id}/comparison` con test que valida la forma del payload y la consistencia de las agregaciones.
- Endpoint barcode con test que valida 404 cuando no existe.

### 16.2 QA manual

Checklist de accesibilidad antes de cerrar la fase:
- Navegacion completa por teclado.
- Contraste minimo verificado con axe-core (target AAA en texto principal).
- Lectura por voz funciona en Chrome, Safari, Firefox.
- Camara para escaneo funciona en Android Chrome y iOS Safari.
- PWA instalable y los wizards funcionan offline (excepto submit que requiere red).
- Sin emojis en codigo, comentarios, mensajes ni commits (regla del proyecto).

## 17. Plan de despliegue y migracion del UI existente

Esta fase se ejecuta en sub-iteraciones para no dejar el sistema roto. Orden propuesto (el plan detallado vendra de writing-plans):

1. **Iteracion 6.1** — Nuevo shell (`AppShell`, `BottomNav`, sidebar colapsada), redirects de rutas viejas a nuevas. Sin cambios funcionales todavia. Salida: misma funcionalidad bajo nueva navegacion.
2. **Iteracion 6.2** — Componentes base (`HelpfulField`, `AnimalAvatar`, `BigButton`, `BigPicker`, `WizardStep`, `IconCard`, `EmptyState`, `SpeakButton`).
3. **Iteracion 6.3** — Launcher "Hacer una nota" y wizards de las ocho acciones mas frecuentes. Las pantallas viejas siguen existiendo.
4. **Iteracion 6.4** — Catalogo de medicamentos (backend mas frontend) y escaneo de codigo de barras.
5. **Iteracion 6.5** — Compra y venta integradas, endpoint `with-purchase`.
6. **Iteracion 6.6** — Reescribir las paginas Inicio, Salud, Alimentacion, Dinero como vistas unicas. Pantallas anteriores quedan como "Ver todo".
7. **Iteracion 6.7** — Detalle de animal con foto principal grande, tabs internos y linea de tiempo.
8. **Iteracion 6.8** — `ComparisonChart` y endpoint comparativo.
9. **Iteracion 6.9** — Vistas Tarjetas y Mapa de animales, geolocalizacion de ranchos.
10. **Iteracion 6.10** — Migracion a Resend, configuracion segura de secretos (Cloudinary, JWT, base de datos), retirada del envio SMTP anterior, auditoria de strings para eliminar abreviaciones, pulido final de accesibilidad (voz, contraste, ilustraciones, traducciones nuevas, tests con axe-core).

Cada iteracion es desplegable de manera independiente. La iteracion 6.1 es la unica con cambio visible de navegacion sin cambio funcional.

## 18. Riesgos y mitigaciones

| Riesgo | Mitigacion |
|--------|-----------|
| El cambio de navegacion confunde a usuarios actuales | Mantener todas las rutas viejas con redirect; mostrar tooltip "Esto se movio aqui" la primera vez |
| Web Speech API tiene calidad dispar entre navegadores | Es opcional (off por defecto); el texto sigue visible siempre |
| Camara no disponible en algun dispositivo | Modal cae a input manual con instrucciones |
| Catalogo de medicamentos vacio al inicio = friccion | Sembrar 30-50 medicamentos comunes en latam como seed por defecto al crear cuenta nueva; usuarios existentes pueden importar opcionalmente |
| `with-purchase` y `comparison` aumentan carga del backend | Endpoints simples; comparison usa indices existentes; queries con paginacion donde aplique |
| Recharts no renderiza bien con 4 series y mucha data | Limitar a 4 series simultaneas; downsample a max 60 puntos en frontend si el rango lo amerita |
| Mapas requieren tiles externos | Usar OpenStreetMap (gratis, sin API key); cachear tiles |

## 19. Definicion de hecho

- Las 10 sub-iteraciones completadas.
- Sidebar y bottom-nav muestran solo 5 destinos.
- Toda captura nueva pasa por un wizard del launcher (los formularios viejos siguen como fallback).
- Catalogo de medicamentos funcional con escaneo en al menos un wizard (vacuné).
- ComparisonChart visible en detalle de animal con al menos 2 series funcionales.
- Animal puede crearse desde wizard de compra con gasto atomico.
- Animal puede venderse desde detalle con ingreso atomico.
- Lista de animales tiene 3 modos (lista, tarjetas, mapa).
- Foto del animal aparece en lista, detalle (hero), y como avatar en eventos.
- Tests verdes en backend y frontend; axe-core sin violaciones AA.
- Sin emojis, sin git auto-commit (regla del proyecto).
- Cero abreviaciones en cualquier texto visible al usuario (verificado en barrido manual y por test de lint de strings).
- Resend funcionando como unico proveedor de correo en produccion; SMTP retirado del codigo.
- Inventario de secretos completamente parametrizado por variables de entorno; documentado en README y en `.env.example`.
