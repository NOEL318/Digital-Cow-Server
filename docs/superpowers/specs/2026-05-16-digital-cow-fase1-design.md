# Digital Cow — Fase 1: Plataforma SaaS + Catálogo de Ganado

Spec de diseño. Fecha: 2026-05-16. Estado: aprobado para pasar a plan de implementación.

---

## 1. Contexto y alcance

### 1.1 Visión global del producto

Digital Cow es un SaaS para gestión ganadera dirigido a rancheros de LATAM y EE.UU. (bilingüe ES/EN). Cubrirá engorda (beef), producción de leche (dairy) y doble propósito. El producto completo se construye en fases independientes; este documento define únicamente la Fase 1 (MVP plataforma).

Fases planeadas:

1. **Fase 1 (este spec):** Plataforma SaaS multi-tenant + Catálogo de Ganado con fotos.
2. **Fase 2:** Salud y veterinaria (vacunas, enfermedades, tratamientos, plagas).
3. **Fase 3:** Reproducción (celo, inseminaciones, gestación, partos, destete).
4. **Fase 4:** Producción (leche / engorda) y alimentación.
5. **Fase 5:** Economía, reportes avanzados y exportaciones.

### 1.2 Alcance Fase 1

Incluido:

- Registro y login de usuarios.
- Multi-tenancy: una cuenta agrupa una o varias organizaciones ganaderas con sus ranchos.
- Equipos de trabajo: invitar usuarios a la cuenta con un rol asignado.
- Cinco roles: Owner, Admin, Manager, Worker, Viewer.
- Jerarquía: Cuenta → Ranchos → Lotes → Animales.
- CRUD de animales con set completo de identificación (arete interno, oficial, RFID, raza, sexo, fecha de nacimiento, propósito, estado, lote actual, fotos, observaciones).
- Fotos por animal subidas a Cloudinary (incluye captura desde cámara del celular vía PWA).
- Catálogos: razas (semilla precargada por raza común LATAM/EE.UU.) y propósitos (carne, leche, doble propósito).
- Dashboard inicial: totales, distribuciones del hato (raza, sexo, propósito, lote) y altas recientes.
- i18n ES/EN completo (textos UI, validaciones, mensajes de error).
- Listado de animales con búsqueda por arete, filtros (raza, lote, estado, sexo, propósito) y paginación server-side.
- Panel admin global (super-admin de Digital Cow) para marcar cuentas como activas/inactivas (billing manual).
- Logs de auditoría básicos (quién creó/modificó animales y usuarios).

Explícitamente fuera de alcance de Fase 1:

- Eventos de salud, reproducción, pesajes, ordeños, alimentación, ventas, costos.
- Pagos en línea (Stripe u otros).
- Genealogía y campos reproductivos.
- Reportes PDF/Excel (fuera, salvo el dashboard visual).
- Offline-first / cola de sincronización (solo PWA instalable, sin offline).
- Microservicios, Kubernetes.
- Notificaciones push o por email salvo: verificación de email e invitaciones a equipo.
- Computer vision, integraciones IoT, integraciones SENASICA/SISBOV/USDA.

### 1.3 Decisión sobre microservicios, resilience y Kubernetes

Verificación pedida por el usuario.

- **Microservicios: NO.** Justificación: equipo pequeño, dominio compartido (un animal toca salud, reproducción y producción simultáneamente), MySQL único, sin tráfico real todavía. Un monolito modular bien dividido permite extraer servicios cuando el negocio lo demande, sin pagar el costo operativo desde el día uno (deploy independiente, mensajería entre servicios, eventual consistency, tracing distribuido). La estructura por paquetes de feature deja la puerta abierta a una extracción futura.
- **Resilience: SÍ, dentro del monolito.** Resilience4j para retries y circuit breaker en las llamadas a Cloudinary (única dependencia externa de Fase 1). Timeouts explícitos en todos los clientes HTTP. Bulkheads no necesarios todavía.
- **Kubernetes: NO en Fase 1.** Docker Compose en un VPS único cubre el caso de uso inicial. Cuando aparezca tráfico o múltiples clientes que exijan SLA, se migra a un orquestador (k3s o managed Kubernetes). Las imágenes Docker que produciremos son ya compatibles con cualquier orquestador.

---

## 2. Stack tecnológico

### 2.1 Backend

| Componente | Selección | Justificación |
|---|---|---|
| Lenguaje | Java 21 (LTS) | Virtual threads, records, pattern matching. Compatible con todo el ecosistema Spring. |
| Framework | Spring Boot 3.3.x | Estándar de la industria, ecosistema maduro, soporte nativo para JWT, JPA, Validation, Actuator. |
| Build | Maven | Más predecible que Gradle para proyectos Spring estándar; sin DSL custom. |
| Persistencia | Spring Data JPA + Hibernate 6 | Mapeo declarativo, soporte de filtros multi-tenant. |
| Migraciones | Flyway | Versionado de schema en SQL puro, repeatable y reversible. |
| Base de datos | MySQL 8.x | Requisito del usuario. utf8mb4, InnoDB, índices BTREE estándar. |
| Seguridad | Spring Security + jjwt (0.12.x) | JWT firmado HS256 con secreto rotable. |
| Validación | Jakarta Bean Validation (Hibernate Validator) | Anotaciones en DTOs. |
| Mapeo DTO/Entity | MapStruct | Genera código en compile-time, sin reflection en runtime. |
| Boilerplate | Lombok | Reduce getters/setters/constructores. |
| Docs API | springdoc-openapi | Swagger UI automático en /swagger-ui. |
| Resilience | Resilience4j | Retries y circuit breakers para Cloudinary. |
| Observabilidad | Spring Actuator + Logback con encoder JSON (logstash-logback-encoder) | Health, metrics, logs estructurados. |
| Cliente HTTP | java.net.http.HttpClient (JDK) | Sin dependencia adicional para llamar a Cloudinary. |
| Testing | JUnit 5, AssertJ, Mockito, Testcontainers (MySQL real) | Tests unitarios + integración. |

### 2.2 Frontend

| Componente | Selección | Justificación |
|---|---|---|
| Framework | React 18 + TypeScript | Requisito del usuario. Tipos en todo. |
| Bundler | Vite 5 | Requisito del usuario. |
| UI base | shadcn/ui sobre Radix + Tailwind CSS | Requisito del usuario. Componentes copiados al repo, customizables. |
| Iconos | lucide-react | Compatible con shadcn, sin emojis. |
| Ruteo | React Router v6 | Estándar para SPA. |
| Estado servidor | TanStack Query v5 | Cache, refetch, optimistic updates, manejo de estado de carga/error. |
| Estado UI | React Context + useReducer cuando aplique | Sin Redux; estado mínimo y localizado. |
| Formularios | react-hook-form + zod | Validación tipada compartida cliente/servidor (schemas en zod). |
| i18n | react-i18next | Carga lazy de namespaces, fallback a EN. |
| Gráficas | Recharts | Composable, suficiente para dashboards de Fase 1 (donut, barras, línea). |
| HTTP | axios + interceptors | Para anexar JWT y manejar refresh. |
| PWA | vite-plugin-pwa (Workbox) | Manifest e installability sin offline complejo. |
| Testing | Vitest + Testing Library | Unit + componentes. Playwright opcional para E2E (no incluido en Fase 1). |

### 2.3 Infraestructura externa

- **Cloudinary** para almacenamiento, transformación y CDN de imágenes de animales.
- **MySQL 8** como única base de datos.
- **VPS único** (cualquier proveedor: Hetzner, DigitalOcean, Linode) con Docker y reverse proxy (Caddy con HTTPS automático).

---

## 3. Estructura de repositorio

Monorepo simple:

```
Digital-Cow/
  backend/
    src/main/java/com/digitalcow/
      DigitalCowApplication.java
      common/             clases compartidas: errors, pagination, audit, security utils
      tenancy/            filtro multi-tenant, TenantContext, interceptors
      auth/               login, registro, JWT, refresh, password reset
      account/            cuentas, planes, billing manual (super-admin)
      org/                organizaciones dentro de una cuenta, ranchos, lotes
      user/               usuarios, invitaciones, roles
      catalog/            razas, propósitos (seed data)
      animal/             entidad Animal y endpoints CRUD
      photo/              servicio de fotos, firmado de uploads a Cloudinary
      dashboard/          endpoints agregados para dashboard
      audit/              tabla de auditoría y aspect AOP
      config/             beans de configuración (Security, OpenAPI, CORS, etc.)
    src/main/resources/
      application.yml
      application-dev.yml
      application-prod.yml
      db/migration/       scripts Flyway V1__init.sql, V2__seed_breeds.sql, etc.
      i18n/messages.properties, messages_es.properties
    src/test/java/...     tests unitarios y de integración
    Dockerfile
    pom.xml
  frontend/
    src/
      app/                providers, router, layout shells
      pages/              páginas top-level (Login, Register, Dashboard, AnimalsList, AnimalDetail, Settings, etc.)
      features/           lógica por feature (animals/, auth/, dashboard/, org/, team/)
        animals/
          api/            llamadas a backend (TanStack Query hooks)
          components/     AnimalForm, AnimalTable, PhotoUploader, etc.
          schemas/        zod schemas
          types.ts
      components/         UI compartido (shadcn/ui copiado aquí)
      lib/                utilities (axios instance, formatters, i18n init)
      hooks/              hooks transversales (useAuth, useTenant)
      locales/
        en/common.json, en/animals.json, ...
        es/common.json, es/animals.json, ...
    public/
      manifest.webmanifest
      icons/
    index.html
    vite.config.ts
    tailwind.config.ts
    tsconfig.json
    package.json
    Dockerfile           build estático + nginx para servir
  docker-compose.yml     producción local: mysql + backend + frontend (nginx)
  docker-compose.dev.yml overrides: hot reload, montar volúmenes
  .env.example
  docs/
    superpowers/specs/2026-05-16-digital-cow-fase1-design.md   (este archivo)
  README.md
```

Razón del monorepo: equipo pequeño, deploy conjunto, refactors cross-cutting más fáciles. Si el repo crece, se puede partir más adelante.

---

## 4. Arquitectura backend (monolito modular)

### 4.1 Capas por paquete de feature

Cada paquete de feature (animal, user, org, etc.) sigue la misma estructura interna:

```
animal/
  AnimalController.java         capa web: REST endpoints, validation de DTOs
  AnimalService.java            capa negocio: orquestación, autorización, transacciones
  AnimalRepository.java         capa persistencia: Spring Data JPA
  Animal.java                   entity JPA
  dto/                          AnimalCreateDto, AnimalUpdateDto, AnimalResponseDto, AnimalListItemDto
  mapper/AnimalMapper.java      MapStruct
  spec/AnimalSpecifications.java específicas JPA para filtros dinámicos
```

Reglas de dependencia:

- Los controllers no llaman a otros controllers; llaman a su propio service.
- Los services pueden depender de services de otros features pero solo de su API pública.
- Las entities y repositories son privadas al feature; nunca se exponen fuera del paquete.
- DTOs son la moneda de comunicación con el exterior; no se serializa una entity JPA en una respuesta HTTP.

### 4.2 Multi-tenancy

**Estrategia:** discriminator column. Toda tabla con datos de cliente lleva una columna `account_id BIGINT NOT NULL` indexada.

**Implementación:**

1. `TenantContext` es un `ThreadLocal<Long>` (compatible con virtual threads) que guarda el `account_id` del request actual.
2. Un `OncePerRequestFilter` (`TenancyFilter`) corre después de Spring Security: lee el JWT, extrae el claim `accountId` y lo guarda en `TenantContext`. Al final del request lo limpia.
3. Hibernate filter declarado en cada entity multi-tenant:

   ```java
   @FilterDef(name = "accountFilter", parameters = @ParamDef(name = "accountId", type = Long.class))
   @Filter(name = "accountFilter", condition = "account_id = :accountId")
   ```

   Un `EntityManager` post-construct activa el filtro con el `accountId` del `TenantContext` para cada sesión.
4. En todas las inserciones, un `EntityListener` (`@PrePersist`) inyecta el `accountId` del `TenantContext` automáticamente, evitando que el desarrollador olvide setearlo.
5. Endpoints de Auth (login, registro, refresh) y endpoints super-admin están exentos del filtro mediante un `@SkipTenancy` custom annotation, validado en el controller.

**Garantía:** ningún query JPA puede leer datos de otra cuenta porque el filtro se aplica en la sesión. Tests de integración validarán esta garantía con datos de dos cuentas distintas y verificando que un usuario de la cuenta A no vea registros de la cuenta B.

### 4.3 Autenticación y autorización

**Flujo de login:**

1. `POST /api/auth/login` con `{email, password}`.
2. Backend valida con BCrypt, genera:
   - `access_token` JWT HS256, expiración 15 min, claims: `sub` (userId), `accountId`, `roles`, `email`.
   - `refresh_token` opaco (UUID v4) guardado en tabla `refresh_token` con expiración 30 días, hash en columna.
3. Frontend guarda ambos en `localStorage` (acceso) y los manda en `Authorization: Bearer <token>`.
4. `POST /api/auth/refresh` con `{refresh_token}`: valida, rota refresh (uno solo válido a la vez), devuelve nuevo access.
5. `POST /api/auth/logout`: invalida el refresh token (lo borra de la tabla).

**Autorización por rol:**

- Spring Security con `@PreAuthorize("hasAnyRole('OWNER','ADMIN')")` en métodos de service.
- Matriz de permisos (resumen):

| Acción | OWNER | ADMIN | MANAGER | WORKER | VIEWER |
|---|---|---|---|---|---|
| Ver dashboard, listar animales | sí | sí | sí | sí | sí |
| Crear/editar animal, subir foto | sí | sí | sí | sí | no |
| Borrar animal | sí | sí | sí | no | no |
| Crear/editar ranchos y lotes | sí | sí | sí | no | no |
| Invitar usuarios, asignar roles | sí | sí | no | no | no |
| Cambiar nombre/marca de cuenta | sí | sí | no | no | no |
| Eliminar cuenta, transferir owner | sí | no | no | no | no |

- Workers y managers pueden estar restringidos opcionalmente a un subconjunto de ranchos (campo `user_ranch_access`). En Fase 1 se modela la tabla pero por defecto un Manager/Worker tiene acceso a todos los ranchos de la cuenta. La UI de restricción por rancho es opcional para Fase 1 y aceptable si se difiere.

**Verificación de email:**

- Al registrar, se genera un token de verificación (UUID, 24h) y se envía al correo del usuario. Se requiere verificación para acceder a la app. En desarrollo el token se loguea (no se manda email) si no hay SMTP configurado.

### 4.4 Modelo de datos (MySQL)

Notación: PK = primary key, FK = foreign key, UQ = unique, IX = index. Todas las tablas con datos de cliente llevan `account_id`.

#### Tablas core

**account**
- `id` PK BIGINT AUTO_INCREMENT
- `name` VARCHAR(120) NOT NULL
- `slug` VARCHAR(60) NOT NULL UQ
- `status` ENUM('ACTIVE','INACTIVE','SUSPENDED') NOT NULL DEFAULT 'ACTIVE'
- `plan` ENUM('FREE','PRO') NOT NULL DEFAULT 'FREE'
- `default_locale` ENUM('es','en') NOT NULL DEFAULT 'es'
- `created_at`, `updated_at` TIMESTAMP

**app_user** (`user` es palabra reservada en MySQL en algunas configs)
- `id` PK BIGINT
- `account_id` FK -> account.id (un usuario pertenece a una sola cuenta en Fase 1)
- `email` VARCHAR(180) NOT NULL UQ (global UQ)
- `password_hash` VARCHAR(120) NOT NULL (BCrypt)
- `full_name` VARCHAR(160) NOT NULL
- `role` ENUM('OWNER','ADMIN','MANAGER','WORKER','VIEWER') NOT NULL
- `locale` ENUM('es','en') NULL (override del default de la cuenta)
- `email_verified_at` TIMESTAMP NULL
- `status` ENUM('ACTIVE','INVITED','DISABLED') NOT NULL DEFAULT 'INVITED'
- `created_at`, `updated_at`
- IX(account_id), IX(email)

**user_invitation**
- `id` PK, `account_id` FK, `email`, `role`, `token` VARCHAR(64) UQ, `expires_at`, `accepted_at`, `created_by_user_id` FK
- IX(account_id), UQ(token)

**refresh_token**
- `id` PK, `user_id` FK, `token_hash` CHAR(64) NOT NULL UQ, `expires_at`, `created_at`, `revoked_at`
- IX(user_id), UQ(token_hash)

**email_verification**
- `id` PK, `user_id` FK, `token` VARCHAR(64) UQ, `expires_at`, `used_at`

**password_reset**
- `id` PK, `user_id` FK, `token` VARCHAR(64) UQ, `expires_at`, `used_at`

#### Jerarquía organizacional

**organization** (opcional dentro de una cuenta; Fase 1 puede tener una sola organización por cuenta para simplificar)
- `id` PK, `account_id` FK, `name`, `created_at`, `updated_at`
- IX(account_id)
- Nota: para simplificar Fase 1 omitimos esta tabla y modelamos Account → Ranch directamente. Se agrega en una migración posterior si un cliente lo pide.

**ranch** (rancho / establecimiento)
- `id` PK, `account_id` FK, `name` VARCHAR(120), `location` VARCHAR(200) NULL (texto libre), `latitude` DECIMAL(9,6) NULL, `longitude` DECIMAL(9,6) NULL, `area_hectares` DECIMAL(10,2) NULL, `notes` TEXT NULL
- `created_at`, `updated_at`
- IX(account_id)

**lot** (lote / potrero)
- `id` PK, `account_id` FK, `ranch_id` FK, `name` VARCHAR(120), `area_hectares` DECIMAL(10,2) NULL, `notes` TEXT NULL
- `created_at`, `updated_at`
- IX(account_id, ranch_id)

#### Catálogos (seed)

**breed**
- `id` PK, `code` VARCHAR(40) UQ, `name_es` VARCHAR(120), `name_en` VARCHAR(120), `species` ENUM('BOVINE'), `category` ENUM('DAIRY','BEEF','DUAL'), `bos` ENUM('TAURUS','INDICUS','CROSS')
- Seed: Holstein, Jersey, Pardo Suizo, Gyr, Girolando, Angus, Hereford, Charolais, Brahman, Brangus, Beefmaster, Simmental, Limousin, Nelore, Senepol, Santa Gertrudis, Simbrah.

**purpose** (propósito)
- Enum embebido en el modelo: BEEF, DAIRY, DUAL. No es tabla porque no cambia.

#### Animales

**animal**
- `id` PK BIGINT
- `account_id` FK NOT NULL
- `ranch_id` FK NOT NULL
- `lot_id` FK NULL (puede no tener lote asignado)
- `internal_tag` VARCHAR(40) NOT NULL (arete interno, único por cuenta)
- `official_tag` VARCHAR(60) NULL (SINIIGA, SISBOV, USDA 840, etc.)
- `rfid` VARCHAR(40) NULL
- `name` VARCHAR(80) NULL
- `sex` ENUM('FEMALE','MALE') NOT NULL
- `birth_date` DATE NULL
- `birth_date_estimated` BOOLEAN NOT NULL DEFAULT FALSE
- `breed_id` FK NOT NULL
- `purpose` ENUM('BEEF','DAIRY','DUAL') NOT NULL
- `status` ENUM('ACTIVE','SOLD','DEAD','MISSING','TRANSFERRED') NOT NULL DEFAULT 'ACTIVE'
- `cover_photo_id` FK -> animal_photo.id NULL (foto destacada)
- `notes` TEXT NULL
- `created_by_user_id` FK
- `created_at`, `updated_at`
- UQ(account_id, internal_tag)
- UQ(account_id, official_tag) WHERE official_tag IS NOT NULL (implementado con índice único compuesto y MySQL acepta múltiples NULLs)
- IX(account_id, ranch_id, lot_id, status)
- IX(account_id, breed_id)

**animal_photo**
- `id` PK
- `account_id` FK NOT NULL
- `animal_id` FK NOT NULL
- `cloudinary_public_id` VARCHAR(200) NOT NULL
- `cloudinary_url` VARCHAR(500) NOT NULL
- `width` INT, `height` INT, `bytes` INT
- `taken_at` TIMESTAMP NULL (fecha en que se tomó la foto, no de upload)
- `uploaded_by_user_id` FK
- `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
- IX(account_id, animal_id, created_at)

#### Auditoría

**audit_log**
- `id` PK, `account_id` FK NULL (NULL para acciones super-admin), `user_id` FK NULL, `entity_type` VARCHAR(60), `entity_id` BIGINT, `action` ENUM('CREATE','UPDATE','DELETE','LOGIN','INVITE'), `payload_json` JSON NULL, `ip` VARCHAR(45), `user_agent` VARCHAR(250), `created_at`
- IX(account_id, created_at), IX(entity_type, entity_id)

#### Convenciones SQL

- Todas las tablas con datos de cliente llevan `account_id`.
- Todas las tablas llevan `created_at` y `updated_at`.
- `ON DELETE RESTRICT` por defecto en FKs (nada se borra silenciosamente).
- Animal nunca se borra físicamente: se marca `status='DEAD'` o `status='SOLD'`. Solo se permite borrado físico si nunca se editó (creado por error).

### 4.5 API REST

Versionado por prefijo `/api/v1`. JSON. Códigos HTTP estándar.

**Errores:** todas las respuestas de error siguen el shape:

```json
{
  "error": {
    "code": "ANIMAL_TAG_DUPLICATE",
    "message": "El arete interno ya existe en esta cuenta.",
    "messageKey": "errors.animal.tag.duplicate",
    "details": { "field": "internalTag" }
  },
  "traceId": "..."
}
```

Códigos de error custom enumerados en `ErrorCode` (enum Java), mapeados a `messageKey` para que el frontend traduzca. Validaciones de campo devuelven `400` con `details.fieldErrors: [{field, code}]`.

**Endpoints Fase 1:**

Auth:
- `POST /api/v1/auth/register` (registro de Owner; crea Account + User; envía email de verificación)
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `POST /api/v1/auth/verify-email` (token)
- `POST /api/v1/auth/request-password-reset`
- `POST /api/v1/auth/reset-password` (token + nueva password)

Cuenta y perfil:
- `GET /api/v1/me` (datos del usuario actual y de su cuenta)
- `PATCH /api/v1/me` (nombre, locale)
- `PATCH /api/v1/me/password`
- `GET /api/v1/account` (datos cuenta)
- `PATCH /api/v1/account` (nombre, locale default)

Equipo:
- `GET /api/v1/team` (lista usuarios de la cuenta)
- `POST /api/v1/team/invitations` (admin invita por email + rol)
- `DELETE /api/v1/team/invitations/{id}`
- `POST /api/v1/team/invitations/{token}/accept` (no requiere auth previa)
- `PATCH /api/v1/team/users/{id}` (cambiar rol, deshabilitar)

Ranchos y lotes:
- `GET /api/v1/ranches`
- `POST /api/v1/ranches`
- `PATCH /api/v1/ranches/{id}`
- `DELETE /api/v1/ranches/{id}` (sólo si no tiene animales activos)
- `GET /api/v1/ranches/{id}/lots`
- `POST /api/v1/ranches/{id}/lots`
- `PATCH /api/v1/lots/{id}`
- `DELETE /api/v1/lots/{id}` (sólo si no tiene animales)

Catálogos:
- `GET /api/v1/breeds` (lista todas las razas; cacheable)

Animales:
- `GET /api/v1/animals` (paginado, filtros: `?page=0&size=20&search=&ranchId=&lotId=&breedId=&sex=&purpose=&status=`)
- `POST /api/v1/animals`
- `GET /api/v1/animals/{id}`
- `PATCH /api/v1/animals/{id}`
- `DELETE /api/v1/animals/{id}` (sólo si nunca se editó; en caso contrario error explícito)
- `GET /api/v1/animals/{id}/photos`
- `POST /api/v1/animals/{id}/photos/sign-upload` (devuelve firma de Cloudinary; ver §4.6)
- `POST /api/v1/animals/{id}/photos/confirm` (frontend confirma tras upload exitoso con `public_id` y metadata)
- `DELETE /api/v1/animals/{id}/photos/{photoId}`
- `PATCH /api/v1/animals/{id}/cover-photo/{photoId}` (marcar foto principal)

Dashboard:
- `GET /api/v1/dashboard/summary` (totales y agregados; ver §4.7)

Super-admin (rol global, no por cuenta):
- `POST /api/v1/admin/login`
- `GET /api/v1/admin/accounts`
- `PATCH /api/v1/admin/accounts/{id}` (cambiar status, plan)

### 4.6 Subida de fotos con Cloudinary

Flujo signed upload (no exponemos secret de Cloudinary al frontend):

1. Frontend solicita firma: `POST /api/v1/animals/{id}/photos/sign-upload` con `{tags: ["animal-{id}"]}`.
2. Backend valida que el usuario tiene permiso de escritura sobre el animal, genera la firma con `cloud_name`, `api_key`, `timestamp`, `folder=accounts/{accountId}/animals/{animalId}`, `tags`. Devuelve:

   ```json
   {
     "cloudName": "...",
     "apiKey": "...",
     "timestamp": 1715900000,
     "folder": "accounts/123/animals/456",
     "tags": "animal-456",
     "signature": "..."
   }
   ```

3. Frontend sube directo a `https://api.cloudinary.com/v1_1/{cloudName}/image/upload` con FormData.
4. Frontend confirma al backend: `POST /api/v1/animals/{id}/photos/confirm` con `{publicId, url, width, height, bytes, takenAt}`. Backend valida que `publicId` empiece con `accounts/{accountId}/animals/{id}/` (defensa contra usuario malicioso que invente public_ids ajenos) y crea el registro `animal_photo`.

**Resilience:** la llamada de firma es local (no llama a Cloudinary). La tabla `animal_photo` se crea sólo tras confirmación, así que un upload abortado a Cloudinary no deja registro huérfano. Si Cloudinary cae, el frontend muestra error claro al usuario y permite reintentar. No hay reintentos automáticos cliente porque el usuario ya está mirando la pantalla.

**Limpieza:** los uploads sin confirmación se vuelven "huérfanos" en Cloudinary. Un job programado (cron Spring `@Scheduled`, semanal) lista uploads en la carpeta de cada cuenta y borra los que no tengan registro en `animal_photo`. Fuera de Fase 1; documentado como deuda conocida.

### 4.7 Dashboard

Endpoint único `GET /api/v1/dashboard/summary` que devuelve:

```json
{
  "totals": {
    "totalAnimals": 0,
    "activeAnimals": 0,
    "soldThisYear": 0,
    "deadThisYear": 0,
    "ranches": 0,
    "lots": 0
  },
  "byRanch": [{"ranchId": 1, "ranchName": "La Esperanza", "count": 250}],
  "byBreed": [{"breedId": 1, "breedCode": "HOLSTEIN", "count": 80}],
  "bySex": {"FEMALE": 240, "MALE": 60},
  "byPurpose": {"BEEF": 100, "DAIRY": 180, "DUAL": 20},
  "recentAdditions": {
    "labels": ["2026-04-16", "2026-04-17", "..."],
    "counts": [3, 1, 0, 5, ...]
  }
}
```

Calculado con queries agregadas (`COUNT(*) GROUP BY`). Cacheado en memoria 60 segundos por cuenta (Caffeine). La caché se invalida en cualquier `POST /animals` o `PATCH /animals/{id}` que cambie `status`, `breed_id`, `ranch_id`, `purpose`, `sex`.

### 4.8 Manejo de errores

- `RestControllerAdvice` global que mapea:
  - `MethodArgumentNotValidException` → 400 con `details.fieldErrors`
  - `EntityNotFoundException` → 404 con código apropiado
  - `AccessDeniedException` → 403
  - `AuthenticationException` → 401
  - `DataIntegrityViolationException` con UQ violado → 409 con código específico (`ANIMAL_TAG_DUPLICATE`, etc.)
  - `BusinessException` (excepción de dominio custom con `ErrorCode`) → 400/409/422 según corresponda
  - Cualquier otra → 500 con `INTERNAL_ERROR` y log a nivel ERROR con stack trace y traceId
- `traceId` generado por filter al inicio del request, anexado a MDC para que aparezca en todos los logs del request, devuelto en headers (`X-Trace-Id`) y en cuerpo de errores.

### 4.9 Observabilidad y logging

- Logback con encoder JSON: cada línea de log es un objeto con `timestamp`, `level`, `logger`, `message`, `traceId`, `userId`, `accountId`.
- Endpoints Actuator habilitados: `/actuator/health`, `/actuator/info`, `/actuator/metrics`, `/actuator/prometheus` (este último opcional).
- Health checks: liveness (proceso vivo), readiness (DB + Cloudinary alcanzables, este último con timeout corto). Si Cloudinary cae, app sigue readiness OK pero feature de fotos devuelve error claro.
- Métricas custom: `animals.created.count`, `photos.uploaded.count`, `auth.login.success/failed`, etc.

### 4.10 Resilience4j

Decoración de cliente Cloudinary:
- Timeout: 5 segundos por request.
- Retry: 3 intentos, backoff exponencial 200ms, 400ms, 800ms, sólo para 5xx y timeouts.
- Circuit breaker: si 50% de últimas 20 llamadas fallaron, abre 30s.
- Cuando circuit breaker está abierto, el endpoint de firma devuelve 503 con `PHOTO_SERVICE_UNAVAILABLE`. El frontend muestra "Servicio de fotos no disponible, intenta más tarde".

### 4.11 Seguridad

- BCrypt cost 12 para passwords.
- JWT firmado HS256 con secreto leído de variable de entorno `JWT_SECRET` (mínimo 256 bits). Rotación de secreto invalida todas las sesiones.
- CORS configurado por allowlist de orígenes (env var `CORS_ALLOWED_ORIGINS`). En dev: `http://localhost:5173`.
- Headers de seguridad: `Strict-Transport-Security`, `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Referrer-Policy: strict-origin-when-cross-origin`.
- CSRF deshabilitado para endpoints stateless con JWT, habilitado si en el futuro se usan cookies de sesión.
- Rate limiting básico (Bucket4j): 10 req/seg por IP en `/auth/*`. Fuera de esto, sin rate limit en Fase 1.
- Validación estricta de inputs (Bean Validation) en todos los DTOs.
- Auditoría: cada modificación de animal/usuario crea registro en `audit_log`.
- Secretos (JWT_SECRET, CLOUDINARY_API_SECRET, MYSQL_PASSWORD, SMTP_PASSWORD) leídos sólo de env vars, nunca commiteados.
- Email del super-admin en una env var `SUPERADMIN_EMAIL`; al arrancar, si no existe usuario super-admin, se crea con password generada y logueada (rotar al primer login).

### 4.12 Testing backend

- **Unit tests:** services con mocks de repositorios, validan reglas de negocio.
- **Integration tests:** Testcontainers MySQL real, prueban repositories, controllers (MockMvc) y filtro multi-tenant. Suite específica que crea dos cuentas y verifica aislamiento.
- **Cobertura mínima sugerida:** 70% líneas en código de negocio (`/service`, `/mapper`).
- Tests corren en CI antes de cualquier deploy.

---

## 5. Arquitectura frontend

### 5.1 Estructura por feature

Dentro de `src/features/<feature>/` se agrupan: `api/` (hooks TanStack Query), `components/`, `schemas/` (zod), `types.ts`, `utils.ts`. Las páginas (`src/pages/`) componen features pero no contienen lógica de negocio.

Convención: una página nunca llama a `axios` directamente; siempre consume hooks de TanStack Query expuestos por la feature.

### 5.2 Cliente HTTP

Instancia única de `axios` en `src/lib/http.ts` con:
- baseURL desde `import.meta.env.VITE_API_URL`.
- Interceptor request: anexa `Authorization: Bearer <access_token>` desde el store de auth.
- Interceptor response: si recibe 401 y hay refresh token, llama `/auth/refresh` una vez, reintenta el request original. Si refresh también falla, limpia sesión y redirige a `/login`.

### 5.3 Estado de autenticación

- `AuthContext` con `user`, `account`, `accessToken`, `refreshToken`, `login()`, `logout()`, `register()`.
- Tokens persistidos en `localStorage`. Al cargar la app, si hay token y no está expirado, hidrata el contexto y llama `GET /me` para validar.
- `<ProtectedRoute>` componente que envuelve rutas privadas, redirige a `/login` si no hay sesión.
- `<RoleGate roles={['OWNER','ADMIN']}>` componente para esconder UI según rol.

### 5.4 i18n

- `react-i18next` con backend `i18next-http-backend` (carga lazy de JSON desde `/locales/{lng}/{ns}.json`).
- Namespaces: `common`, `auth`, `animals`, `dashboard`, `team`, `ranches`, `errors`.
- Selector de idioma en el navbar y persiste en `localStorage` + `app_user.locale`.
- Defaults: navegador del usuario; fallback `es`. Los errores del backend incluyen `messageKey`, el frontend lo traduce contra `errors` namespace; si no hay traducción, usa `message` literal.

### 5.5 Rutas

```
/                     redirige a /dashboard si autenticado, /login si no
/login
/register
/verify-email?token=
/forgot-password
/reset-password?token=
/accept-invitation?token=
/dashboard
/animals
/animals/new
/animals/:id
/animals/:id/edit
/ranches
/ranches/:id
/team
/settings/account
/settings/profile
/admin (super-admin)
```

### 5.6 Listado de animales

- Tabla shadcn `<DataTable>` con columnas: arete interno, arete oficial, nombre, raza, sexo, estado, lote, foto miniatura.
- Filtros arriba de la tabla: search (debounced 300ms), select de raza, lote, estado, sexo, propósito.
- Paginación server-side con TanStack Query (`useQuery` con `keepPreviousData`).
- Bulk actions diferidas a fases futuras.
- Click en fila → `/animals/:id`.

### 5.7 Detalle de animal

- Header: foto cover, arete interno grande, nombre, badges (raza, sexo, estado, lote).
- Tabs: "Información" (datos básicos), "Fotos" (galería con upload, ver §5.8).
- Botones: Editar, Cambiar foto principal, Cambiar lote, Cambiar estado.
- Tabs futuras (deshabilitadas, ocultas en Fase 1): Salud, Reproducción, Producción, Economía.

### 5.8 Subida de fotos

- Componente `<PhotoUploader>` con dos modos:
  1. Drag&drop o click para seleccionar archivo (desktop).
  2. Botón "Tomar foto" usa `<input type="file" accept="image/*" capture="environment">` (mobile abre cámara directamente).
- Flujo: pide firma → sube a Cloudinary (con barra de progreso) → confirma con backend. Errores en cualquier paso muestran toast con código i18n.
- Compresión cliente con `browser-image-compression` antes de subir (max 1600px, quality 0.8) para reducir consumo Cloudinary y tiempo en móviles con conexión lenta.

### 5.9 Dashboard

- Grid responsive (1 columna móvil, 2 tablet, 3-4 desktop) con cards:
  - "Total animales activos" (número grande).
  - "Vendidos este año", "Bajas este año".
  - "Ranchos / Lotes".
- Gráficas Recharts:
  - Donut: distribución por raza.
  - Donut: distribución por propósito (carne/leche/doble).
  - Barras horizontales: animales por rancho.
  - Línea: altas en los últimos 30 días.
- Loading skeletons (shadcn) durante la carga.

### 5.10 PWA

- `vite-plugin-pwa` con `registerType: 'autoUpdate'`.
- `manifest.webmanifest`: nombre Digital Cow, theme color, iconos 192/512, `display: standalone`.
- Service worker: precache de shell estático, runtime cache `staleWhileRevalidate` para `GET /api/v1/breeds`.
- Sin offline-first para mutaciones (fuera de Fase 1).

### 5.11 Diseño visual y accesibilidad

- shadcn/ui (estética minimal, dark mode opcional con toggle en navbar).
- Mobile-first responsive con breakpoints Tailwind por defecto.
- Sin emojis. Iconos lucide-react.
- Contraste WCAG AA mínimo, focus visible, labels en todos los inputs, `aria-label` en botones-icono.

### 5.12 Testing frontend

- Vitest + Testing Library para componentes críticos: `AnimalForm`, `PhotoUploader`, `LoginForm`, `ProtectedRoute`, `RoleGate`.
- Mocks de TanStack Query con `MSW` (Mock Service Worker) si la complejidad lo amerita.
- Cobertura objetivo: no obligatoria en Fase 1, pero todos los formularios deben tener al menos un test de submit válido.

---

## 6. Despliegue

### 6.1 Imágenes Docker

- **backend/Dockerfile:** multi-stage. Stage 1 Maven build, stage 2 Eclipse Temurin JRE 21 con el `.jar`. Variables: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`, `CORS_ALLOWED_ORIGINS`, `SMTP_HOST`, `SMTP_USER`, `SMTP_PASSWORD`, `SUPERADMIN_EMAIL`.
- **frontend/Dockerfile:** multi-stage. Stage 1 Node 20 npm build, stage 2 nginx servir `/dist`. Variable build-time: `VITE_API_URL`.

### 6.2 docker-compose.yml (prod-ready local o VPS único)

Servicios:
- `mysql`: imagen oficial 8.x, volumen persistente, healthcheck.
- `backend`: depende de mysql healthy, expone 8080.
- `frontend`: nginx, expone 80.
- `caddy` (en VPS de prod): reverse proxy con HTTPS automático, rutea `digitalcow.app` → frontend, `api.digitalcow.app` → backend.

### 6.3 docker-compose.dev.yml

Override que monta el código como volumen y usa imágenes con hot reload (spring-boot-devtools, vite dev server). MySQL conserva volumen pero con DB de pruebas.

### 6.4 Migraciones

- Flyway corre al arrancar el backend; si una migración falla, el contenedor no arranca. Esto previene estados parciales.
- Migraciones idempotentes para seed de razas (`INSERT ... ON DUPLICATE KEY UPDATE`).

### 6.5 Backups

- Cron en host (fuera del backend): `mysqldump` nightly, encriptado y enviado a un bucket S3-compatible. Documentado en README; script de ejemplo provisto.
- Documentar restauración en README.

---

## 7. Convenciones de código (requisitos del usuario)

Aplicables a backend y frontend:

- **Sin emojis** en código, comentarios, mensajes UI, ni en commits (si los hubiera; el usuario pidió que no se hagan commits automáticos).
- **Sin arte ASCII** en comentarios; sólo texto.
- **Todo comentado y documentado:**
  - Java: Javadoc en clases públicas y métodos públicos. Comentarios inline donde la lógica no sea obvia.
  - TypeScript/React: TSDoc en funciones y componentes exportados. Comentarios sólo donde el "por qué" no se infiere del nombre o el tipo.
- **Baja complejidad:**
  - Funciones < 40 líneas como objetivo.
  - Sin abstracciones especulativas (sin interfaces con una sola implementación a menos que haya razón clara).
  - Sin lógica condicional anidada > 3 niveles.
  - Cyclomatic complexity bajo (recomendado ≤ 10 por método; herramienta opcional: PMD/Checkstyle en backend).
- **Convenciones de naming:**
  - Java: `PascalCase` clases, `camelCase` métodos/variables, `SCREAMING_SNAKE` constantes. Paquetes en minúsculas.
  - TS: `PascalCase` componentes y tipos, `camelCase` funciones/variables, `kebab-case` archivos no-componente.
- **Linting/formato:**
  - Backend: spotless con google-java-format (opcional).
  - Frontend: ESLint + Prettier.

---

## 8. Riesgos y mitigaciones

| Riesgo | Mitigación |
|---|---|
| Fuga de datos entre cuentas (multi-tenant) | Filtro Hibernate forzado a nivel de sesión + tests de integración explícitos de aislamiento. Revisión cruzada antes de merge. |
| Costos inesperados en Cloudinary | Compresión cliente antes de upload + límite de N fotos por animal (configurable, default 20) + job de limpieza de huérfanos. |
| Pérdida de tokens JWT con secret comprometido | Secreto en env var, rotación documentada (rota → todos hacen relogin). Refresh tokens revocables individualmente. |
| Migraciones rotas en prod | Flyway en CI también, contra schema de prueba; rollback documentado vía migración compensatoria. |
| Backups no probados | Documentar y ejecutar restauración mensual a entorno de staging. |
| i18n incompleta | Linter (i18next-parser) detecta keys sin traducir; checked en CI. |
| Subida directa a Cloudinary expone API key | Solo `api_key` es pública; el `api_secret` jamás sale del backend. Firma server-side garantiza que el cliente no puede inventar uploads. |

---

## 9. Definición de "Fase 1 terminada"

La Fase 1 se considera terminada cuando:

1. Un usuario puede registrarse, verificar email, iniciar sesión, cerrar sesión.
2. Puede crear su cuenta con un rancho y un lote.
3. Puede invitar a un compañero con cualquiera de los 5 roles, y éste puede aceptar la invitación.
4. Puede crear, editar y dar de baja animales con todos los campos del set completo.
5. Puede subir fotos (desde desktop y desde cámara mobile) y marcar una como principal.
6. El dashboard muestra los totales y las 4 gráficas con datos reales del hato.
7. Toda la UI funciona en español e inglés con cambio dinámico.
8. Funciona como PWA instalable en iOS y Android.
9. Multi-tenancy validado: dos cuentas creadas en el mismo entorno no ven datos del otro.
10. Super-admin puede listar y activar/desactivar cuentas.
11. CI pasa: build backend, build frontend, tests backend, tests frontend, lint, type-check.
12. `docker-compose up` levanta el sistema completo desde cero en una máquina con Docker.

---

## 10. Trabajo posterior (no incluido en Fase 1)

Para mantener foco y evitar scope creep, lo siguiente se difiere a fases posteriores documentadas en §1.1:

- Eventos: vacunas, enfermedades, tratamientos, pesajes, ordeños, alimentación, partos, inseminaciones.
- Reportes PDF/Excel.
- Pagos Stripe.
- Genealogía, KPIs reproductivos y productivos.
- Notificaciones push, alertas sanitarias.
- Computer vision para BCS o identificación facial.
- Integraciones SENASICA/SISBOV/USDA.
- Offline-first con cola de sincronización.
- Migración a microservicios o Kubernetes.

Cada una se brainstormea como su propia Fase con su propio spec.
