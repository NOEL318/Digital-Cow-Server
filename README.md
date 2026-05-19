# Digital Cow

Plataforma SaaS multi-tenant para gestion ganadera. Fase 1: registro, equipos, ranchos, lotes, catalogo de animales con fotos, dashboard, bilingue espanol / ingles, PWA. Fase 6 en curso: simplificacion de la UI, accesibilidad para baja alfabetizacion, catalogo de medicamentos con escaneo de codigos de barras, graficas comparativas por animal, fotos visibles en todas las vistas, compra y venta integradas, envio de correos con Resend.

## Stack
- Backend: Java 21 + Spring Boot 3.3 + MySQL 8 + Flyway + Hibernate
- Frontend: React 18 + Vite + shadcn/ui + TanStack Query + Recharts + zxing (escaneo de codigos de barras)
- Infra: Docker Compose para MySQL + backend + Adminer. Vercel para el frontend. Cloudinary para fotos, Resend para correo.

## Estructura

- `backend/` API REST monolitica modular.
- `frontend/` SPA + PWA (deploy en Vercel).
- `docker-compose.yml` orquestacion local de MySQL + backend + Adminer (sin frontend).
- `frontend/vercel.json` configuracion de despliegue en Vercel.
- `docs/superpowers/specs/` y `docs/superpowers/plans/` documentacion.

## Variables de entorno

Copiar `.env.example` a `.env` y completar. **Nunca subir el .env al repositorio**; ya esta cubierto por `.gitignore`. Tampoco pegar secretos en chats, capturas o documentos compartidos: si se filtra un secreto, rotarlo en el panel del proveedor y actualizar la variable.

### Inventario de variables

| Variable | Sensible | Descripcion |
|----------|----------|-------------|
| `MYSQL_ROOT_PASSWORD` | si | Password de root de MySQL. |
| `MYSQL_USER`, `MYSQL_PASSWORD`, `MYSQL_DATABASE`, `MYSQL_HOST`, `MYSQL_PORT` | si (password) | Credenciales y endpoint de la base de datos. |
| `JWT_SECRET` | si | Cadena aleatoria >= 256 bits. Generar con `openssl rand -base64 48`. |
| `CORS_ALLOWED_ORIGINS` | no | Lista de origenes permitidos, separados por coma. |
| `SUPERADMIN_EMAIL` | no | Email del super-admin que se crea al primer arranque. |
| `CLOUDINARY_CLOUD_NAME` | no | Nombre publico de la cuenta Cloudinary. |
| `CLOUDINARY_API_KEY` | no | Clave publica de Cloudinary. |
| `CLOUDINARY_API_SECRET` | si | Secreto de Cloudinary. NUNCA viaja al cliente. |
| `MAIL_PROVIDER` | no | `dev` (default), `smtp` o `resend`. |
| `MAIL_FROM` | no | Remitente con formato `"Nombre <email@dominio>"`. |
| `RESEND_API_KEY` | si | Clave de Resend. Necesaria solo si `MAIL_PROVIDER=resend`. |
| `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASSWORD` | si (password) | Solo si `MAIL_PROVIDER=smtp`. |
| `VITE_API_URL` | no | Endpoint del backend que el frontend usa. En dev local se omite (Vite usa proxy a `localhost:8080`); en Vercel se configura como Environment Variable en el panel. |

### Rotacion de secretos

Si un secreto se filtra (logs publicos, chat, repositorio):
1. **Rotarlo de inmediato** en el panel del proveedor (Cloudinary, Resend, base de datos).
2. Actualizar el valor en `.env` (local) o en los secretos del host (produccion).
3. Reiniciar el servicio: `docker compose restart backend`.

## Correo saliente

Tres proveedores intercambiables segun `MAIL_PROVIDER`:

- `dev` (default): loguea cada email en consola. No envia nada. Ideal para desarrollo.
- `resend`: envia via [Resend](https://resend.com) usando la REST API. Requiere `RESEND_API_KEY` y `MAIL_FROM` con un dominio verificado en el panel de Resend (SPF, DKIM, DMARC). Recomendado en produccion.
- `smtp`: envia via SMTP estandar (fallback de transicion). Requiere las variables `SMTP_*`.

## Cloudinary

Las fotos se suben directamente desde el navegador con una firma generada por el backend en `POST /api/v1/photos/signature`. El backend usa el secret de Cloudinary (`CLOUDINARY_API_SECRET`); el cliente nunca lo recibe.

## Quick start (local)

```
cp .env.example .env
# Backend + MySQL + Adminer en Docker
docker compose up -d --build
# Frontend en dev local (proxy automatico a backend localhost:8080)
cd frontend && npm install && npm run dev
```

- Frontend (Vite dev): http://localhost:5173
- Backend: http://localhost:8080
- Adminer: http://localhost:8081
- Swagger: http://localhost:8080/swagger-ui.html

## Despliegue del frontend en Vercel

El frontend se sirve desde Vercel, no desde Docker. Pasos:

1. **Conectar el repositorio** en https://vercel.com → New Project → Import Git Repository.
2. En la pantalla de configuracion del proyecto:
   - **Root directory**: `frontend`
   - **Framework preset**: Vite (se detecta solo gracias a `frontend/vercel.json`).
   - **Build command**: `npm run build`
   - **Output directory**: `dist`
3. En **Environment Variables** anadir:
   - `VITE_API_URL` = `https://api.tu-dominio.com/api/v1` (la URL publica del backend).
4. **Deploy**. Cada push a `main` despliega produccion; cada PR despliega preview.

### CORS en el backend
Una vez que tengas la URL de Vercel (por ejemplo `https://digital-cow.vercel.app`), agrega ese origen a `CORS_ALLOWED_ORIGINS` en `.env` del backend (separado por coma de los origenes de dev / previews) y reinicia:

```
CORS_ALLOWED_ORIGINS=http://localhost:5173,https://digital-cow.vercel.app
docker compose restart backend
```

### Backend publico
El backend debe ser accesible desde internet para que Vercel lo pueda llamar. Opciones:
- VPS con Nginx haciendo reverse proxy al puerto 8080 (ver `deploy/`).
- Tunel temporal (Cloudflare Tunnel, ngrok) para pruebas rapidas.

## Bootstrap super-admin

Al primer arranque, los logs del backend incluyen una linea:

```
SUPERADMIN CREATED - Email: admin@digitalcow.local Password: <random> ROTATE AT FIRST LOGIN
```

Iniciar sesion en `/admin/login` y cambiar la password en `/ajustes/perfil`.

## Primer registro

1. Abrir `/register` y crear cuenta con un email valido.
2. En dev, el token de verificacion se loguea (buscar `--- DEV EMAIL ---` en logs).
3. Visitar `/verify-email?token=<el-token>`.

## Backups MySQL

Cron en host:
```
docker exec -t digital-cow_mysql_1 mysqldump -u root -p"$MYSQL_ROOT_PASSWORD" digitalcow | gzip > backups/dc-$(date +%F).sql.gz
```

Restore:
```
gunzip -c backups/dc-YYYY-MM-DD.sql.gz | docker exec -i digital-cow_mysql_1 mysql -u root -p"$MYSQL_ROOT_PASSWORD" digitalcow
```

## Tests

Backend:
```
cd backend && JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn verify
```

Frontend:
```
cd frontend && npm test
```

## Navegacion Fase 6

La UI tiene cinco destinos principales:

- **Inicio**: vista del dia con KPIs y alertas.
- **Animales**: catalogo en lista o tarjetas, con fotos y filtros.
- **Hacer una nota**: launcher de captura rapida (vacunar, pesar, vender, gastar y similares).
- **Panel**: sub-paginas Salud, Alimentacion, Dinero, Reproduccion, Produccion.
- **Ajustes**: perfil, cuenta, ranchos, equipo, categorias de dinero, catalogo de medicamentos, idioma y tema.

Todas las rutas anteriores (`/dashboard`, `/animals`, `/health`, etc.) redirigen automaticamente a las nuevas para no romper bookmarks.

## Documentacion

- Spec Fase 6: `docs/superpowers/specs/2026-05-17-digital-cow-fase6-design.md`
- Plan Fase 6: `docs/superpowers/plans/2026-05-17-digital-cow-fase6-plan.md`
- Specs y planes anteriores: ver `docs/superpowers/`.
