# Digital Cow Backend

API REST monolitica modular para la plataforma de gestion ganadera Digital Cow. El backend expone los datos y la logica de negocio que consume el frontend desplegado en Vercel, y orquesta integraciones con MySQL, Cloudinary y Resend.

## Tabla de contenidos

1. Arquitectura general
2. Stack tecnico
3. Estructura del codigo
4. Modelo de datos y migraciones
5. Autenticacion y multi tenant
6. Manejo de errores
7. Cache y rate limit
8. Integraciones externas
9. Configuracion por entornos
10. Como correrlo en local
11. Como ejecutar tests
12. Despliegue en VPS
13. Endpoints publicos relevantes
14. Solucion de problemas

---

## 1. Arquitectura general

El backend es un monolito modular escrito en Java 21 sobre Spring Boot 3.3. Cada modulo de negocio vive en su propio paquete dentro de `com.digitalcow.<modulo>` y contiene su controlador HTTP, su servicio, su repositorio, sus entidades JPA, sus DTOs y sus mappers MapStruct.

Las dependencias entre modulos son siempre hacia el modulo `common`, que aporta utilidades de errores, auditoria, tenancy, logging y web. No hay dependencias ciclicas entre modulos de negocio.

```
+------------+      +------------------+      +-------------+
|  Frontend  |  ->  |   Spring Boot    |  ->  |   MySQL 8   |
|  (Vercel)  |  HTTP|   (este modulo)  |  JPA |  + Flyway   |
+------------+      +------------------+      +-------------+
                          |       |
                          v       v
                    Cloudinary  Resend
```

## 2. Stack tecnico

| Componente | Version | Uso |
|------------|---------|------|
| Java | 21 | Lenguaje base |
| Spring Boot | 3.3.4 | Framework web, DI, seguridad, datos, cache, mail |
| MySQL | 8.0 | Base de datos relacional |
| Flyway | 10.x | Migraciones de esquema |
| Hibernate | 6.x | ORM y persistencia |
| MapStruct | 1.6.2 | Mappers entidad/DTO en tiempo de compilacion |
| Lombok | 1.18.34 | Reduccion de boilerplate |
| jjwt | 0.12.6 | Emision y verificacion de tokens JWT |
| Resilience4j | 2.2.0 | Circuit breaker y retry para servicios externos |
| Bucket4j | 8.14 | Rate limiting por usuario y endpoint |
| Springdoc | 2.6 | Generacion automatica de OpenAPI y Swagger UI |
| Caffeine | runtime | Cache en memoria para dashboard |
| Testcontainers | 1.20 | Tests de integracion con MySQL real |
| Logstash encoder | 8.0 | Logs estructurados JSON en produccion |

## 3. Estructura del codigo

Carpeta principal: `src/main/java/com/digitalcow/`.

```
DigitalCowApplication.java     Entry point Spring Boot.
config/                        Beans globales: seguridad, OpenAPI, cache, password encoder.
common/                        Utilidades transversales:
  audit/                       Auditoria de filas creadas y modificadas.
  error/                       GlobalExceptionHandler, BusinessException, codigos.
  jpa/                         Helpers JPA (paginacion, soft delete, base entities).
  logging/                     Filtros para traceId en MDC y logs estructurados.
  tenancy/                     TenantContext y filtro de aplicacion del tenant.
  web/                         Filtros HTTP y configuracion de CORS reutilizable.
auth/                          Registro, login, refresh, recuperacion de password, JWT.
admin/                         Endpoints administrativos para superadmin.
account/                       Cuenta del usuario autenticado y preferencias.
user/                          Entidad User y operaciones de gestion.
team/                          Equipos, invitaciones y roles.
ranch/                         Ranchos y lotes con condiciones diarias.
animal/                        Animales, fotos, comparacion, badges, compras.
breed/                         Catalogo de razas.
catalog/                       Sub modulos vacuna, enfermedad, medicamento, plaga.
health/                        Diagnosticos, tratamientos, vacunaciones, planes sanitarios, visitas vet, alertas.
feeding/                       Items de alimento, planes, registros y costo por unidad.
production/                    Ordenes, muestras, pesajes, tanque, sacrificio, KPIs.
reproduction/                  Celos, servicios, prenez, abortos, partos, destete, semen, toros.
finance/                       Ingresos, gastos, ventas, P&L, ROI por animal, categorias.
photo/                         Generacion de firma para subida directa a Cloudinary.
mail/                          Cliente de correo dev, smtp y resend.
dashboard/                     KPIs agregados del inicio.
report/                        Reportes consolidados.
agenda/                        Calendario de actividades.
alerts/                        Alertas operativas.
audit/                         Registro de operaciones administrativas.
tenancy/                       TenancyFilter que extrae el tenant del JWT.
```

Carpeta `src/main/resources/`:

```
application.yml          Base, lee variables del entorno.
application-dev.yml      Overrides en desarrollo.
application-prod.yml     Overrides en produccion.
logback-spring.xml       Logging con Logstash en prod, consola en dev.
db/migration/V*.sql      Migraciones Flyway en orden numerico.
```

## 4. Modelo de datos y migraciones

El esquema se construye via Flyway desde `db/migration`. Las migraciones se aplican en orden numerico al arrancar la aplicacion. `ddl-auto=validate` garantiza que las entidades y el esquema concuerdan.

Migraciones principales:

| Archivo | Que crea |
|---------|----------|
| V1__core.sql | Tablas tenant, user, role, refresh_token, password_reset, email_verification |
| V2__ranch_lot.sql | Ranchos y lotes con su jerarquia |
| V3__breed.sql | Catalogo de razas |
| V4__animal.sql | Tabla principal de animales |
| V5__animal_photo.sql | Fotos asociadas a un animal |
| V6__health_catalog.sql | Vacunas, enfermedades, medicamentos y plagas |
| V7__health_events.sql | Diagnosticos, tratamientos, vacunaciones, visitas |
| V8__health_plan.sql | Planes sanitarios |
| V9__animal_genealogy.sql | Padre y madre por animal |
| V10__reproduction.sql | Eventos reproductivos |
| V11__production.sql | Ordenes, muestras, pesajes, tanque |
| V12__feeding.sql | Items, planes y registros de alimento |
| V13__finance.sql | Ingresos, gastos, ventas, categorias |
| V14__medication_barcode.sql | Codigo de barras en medicamentos |
| V17__lot_conditions.sql | Condiciones diarias de un lote |
| V18__lot_polygon.sql | Poligono geografico del lote |
| V19__lot_condition_severity_smallint.sql | Cambio de tipo en severidad |
| V20__medication_expiry.sql | Fecha de vencimiento de medicamentos |
| V21__feeding_per_animal.sql | Registros de alimento a nivel animal |

Toda tabla incluye columnas tenant_id, created_at, updated_at, created_by, updated_by. La columna tenant_id se setea desde el filtro de tenancy y se valida en cada query.

## 5. Autenticacion y multi tenant

### Flujo de autenticacion

1. El cliente envia `POST /api/v1/auth/register` o `POST /api/v1/auth/login` con email y password.
2. El backend devuelve un access token de 720 minutos y un refresh token de 365 dias.
3. El cliente incluye `Authorization: Bearer <access>` en todas las llamadas autenticadas.
4. Si el access vence se renueva con `POST /api/v1/auth/refresh`.
5. El cierre de sesion revoca el refresh con `POST /api/v1/auth/logout`.

### Filtros HTTP

`JwtAuthenticationFilter` extrae el bearer, valida la firma y carga el usuario en el `SecurityContext`.

`TenancyFilter` corre despues y lee el claim `tenantId` del JWT para setear el `TenantContext` con scope de request. Todas las queries JPA aplican el tenant via specifications o repositorios filtrados.

### Roles

Las anotaciones `@PreAuthorize` controlan el acceso por rol. Roles principales: OWNER, MANAGER, WORKER, SUPERADMIN.

## 6. Manejo de errores

`GlobalExceptionHandler` traduce excepciones a respuestas `ApiError` consistentes con codigo, mensaje, clave i18n, detalles y traceId. Errores manejados explicitamente:

- `BusinessException`: errores de dominio con codigo y status propio.
- `MethodArgumentNotValidException`: validacion Bean Validation, status 400.
- `EntityNotFoundException`: status 404.
- `AccessDeniedException`: status 403.
- `AuthenticationException`: status 401.
- `DataIntegrityViolationException`: status 409 con codigos especificos para constraints conocidos.
- Excepcion no esperada: status 500 con log completo del stack.

Los stack traces nunca viajan al cliente. El traceId va en la respuesta y en el header `X-Trace-Id`.

## 7. Cache y rate limit

El cache Caffeine cachea los KPIs del dashboard por tenant y minuto. Se invalida en mutaciones relevantes.

Bucket4j limita las llamadas a auth y mutaciones de animales para evitar abuso. Los limites se configuran por endpoint en codigo.

## 8. Integraciones externas

### Cloudinary

El cliente sube fotos directamente al CDN con una firma generada por `POST /api/v1/photos/signature`. El backend nunca recibe el binario, solo el `publicId` y la URL para guardar en la entidad animal. Resilience4j envuelve las llamadas al API REST de Cloudinary con circuit breaker y retry.

### Resend

El proveedor de correo se selecciona via `MAIL_PROVIDER`. Los tres implementadores (`DevMailer`, `SmtpMailer`, `ResendMailer`) implementan la misma interfaz. Recomendado en produccion: Resend con dominio verificado.

## 9. Configuracion por entornos

Variables que se leen del entorno con default razonable cuando aplica:

| Variable | Default | Uso |
|----------|---------|------|
| SPRING_PROFILES_ACTIVE | dev | Selecciona application-dev.yml o application-prod.yml |
| MYSQL_HOST | localhost | Host de MySQL |
| MYSQL_PORT | 3306 | Puerto |
| MYSQL_DATABASE | digitalcow | Schema |
| MYSQL_USER | digitalcow | Usuario |
| MYSQL_PASSWORD | changeme | Password |
| JWT_SECRET | obligatorio | Cadena aleatoria >= 256 bits |
| CORS_ALLOWED_ORIGINS | http://localhost:5173 | Lista separada por comas |
| CLOUDINARY_CLOUD_NAME | vacio | Nombre publico |
| CLOUDINARY_API_KEY | vacio | Clave publica |
| CLOUDINARY_API_SECRET | vacio | Secreto privado |
| MAIL_PROVIDER | dev | dev, smtp o resend |
| MAIL_FROM | Digital Cow <no-reply@digitalcow.local> | Remitente |
| RESEND_API_KEY | vacio | Solo si provider=resend |
| SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASSWORD | vacio | Solo si provider=smtp |
| SUPERADMIN_EMAIL | admin@digitalcow.local | Email del superadmin inicial |

## 10. Como correrlo en local

Opcion A, todo en Docker:

```
cd ..
cp .env.example .env
docker compose up -d --build
```

El backend queda en http://localhost:8080, Adminer en http://localhost:8081 y Swagger en http://localhost:8080/swagger-ui.html.

Opcion B, backend desde el IDE y MySQL en Docker:

```
cd ..
docker compose -f docker-compose.dev.yml up -d
cd backend
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./mvnw spring-boot:run
```

## 11. Como ejecutar tests

```
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./mvnw verify
```

Los tests de integracion levantan MySQL con Testcontainers. Requiere Docker activo en el host.

## 12. Despliegue en VPS

El directorio `../deploy/` contiene `docker-compose.prod.yml`, scripts de instalacion y backup y un Caddyfile listo para TLS automatico. Ver `../deploy/README.md` para el procedimiento paso a paso.

## 13. Endpoints publicos relevantes

| Metodo | Path | Descripcion |
|--------|------|-------------|
| POST | /api/v1/auth/register | Registro de usuario |
| POST | /api/v1/auth/login | Login |
| POST | /api/v1/auth/refresh | Renovar access token |
| POST | /api/v1/auth/logout | Revocar refresh |
| POST | /api/v1/auth/forgot-password | Iniciar reset |
| POST | /api/v1/auth/reset-password | Confirmar reset |
| GET | /api/v1/breeds | Listado publico de razas |
| GET | /actuator/health | Salud del backend |
| GET | /swagger-ui.html | Documentacion interactiva |

Todos los demas endpoints requieren JWT.

## 14. Solucion de problemas

- **Flyway falla al arrancar:** revisar el log; suele ser una migracion fuera de orden o un esquema preexistente. Si es base de datos vacia probar `FLYWAY_BASELINE_ON_MIGRATE=true` en variables; nunca en produccion sin entender el impacto.
- **JWT expira muy pronto:** el TTL del access es 720 minutos; si los logs muestran refresh fallido revisar reloj del servidor.
- **CORS error en navegador:** anadir el origen exacto en `CORS_ALLOWED_ORIGINS` y reiniciar.
- **Subida de fotos falla:** verificar las tres variables de Cloudinary y revisar el panel para limites de uso.
- **Email no llega:** revisar `MAIL_PROVIDER`, en dev solo se loguean; en resend el dominio debe estar verificado con SPF, DKIM y DMARC.
