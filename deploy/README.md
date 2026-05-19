# Despliegue de Digital Cow en VPS privada

Esta carpeta contiene todo lo necesario para desplegar el backend de Digital Cow en una VPS Debian o Ubuntu. El frontend continua sirviendose desde Vercel; este despliegue solo cubre la API publica.

## Que se despliega

- MySQL 8 en contenedor con volumen persistente y red interna (no expone puertos al exterior).
- Backend Java 21 + Spring Boot 3.3 en contenedor, leyendo configuracion de un archivo .env externo.
- Nginx como proxy inverso en HTTP (puerto 80), expuesto por la IP publica del VPS.

## Requisitos

- VPS con Debian 12 o Ubuntu 22.04 con acceso root.
- IP publica accesible desde Internet en el puerto 80.
- Acceso saliente a Internet para descargar imagenes.

## Pasos de instalacion

### 1. Conexion inicial al VPS

Acceder por SSH como root o como usuario con sudo. Crear un usuario no root para administrar el despliegue es una practica recomendada pero no obligatoria.

### 2. Instalar dependencias

Clonar el repositorio donde prefieras (ejemplo: `~/Digital-Cow-Server`) y ejecutar el instalador:

```
git clone https://github.com/tu-usuario/digital-cow.git ~/Digital-Cow-Server
cd ~/Digital-Cow-Server
bash deploy/scripts/install-vps.sh
```

El script instala Docker, Docker Compose y configura el firewall UFW.

### 3. Configurar variables de entorno

Copiar la plantilla a la raiz del repo y completar los valores reales. Los scripts esperan `.env` justo ahi.

```
cp deploy/.env.prod.example .env
chmod 600 .env
nano .env
```

Las variables criticas son `JWT_SECRET`, los passwords de MySQL y las claves de Cloudinary y Resend. Generar el `JWT_SECRET` con `openssl rand -base64 48`.

### 4. Levantar los servicios

Desde la raiz del repo:

```
bash deploy/scripts/deploy.sh
```

El script reconstruye la imagen del backend, levanta los tres contenedores y muestra el estado. Los logs se ven con:

```
docker compose -f deploy/docker-compose.prod.yml --env-file .env logs -f
```

### 5. Verificacion

Probar la API publica reemplazando `<IP_DEL_VPS>` por la IP de la VPS (ej. 74.208.133.36):

```
curl -i http://<IP_DEL_VPS>/actuator/health
```

Debe devolver `200 OK` con un cuerpo JSON `{"status":"UP"}`.

### 6. Crear el superadmin

Al primer arranque el backend crea un superadmin y registra una linea en el log con la password aleatoria:

```
docker logs digitalcow-backend 2>&1 | grep "SUPERADMIN CREATED"
```

Iniciar sesion en `https://digital-cow.vercel.app/admin/login` con esas credenciales y cambiar la password en Ajustes.

### 7. Conectar el frontend

Configurar en el panel de Vercel la variable de entorno `VITE_API_URL=http://<IP_DEL_VPS>/api/v1` y redeployar. Asegurarse que `CORS_ALLOWED_ORIGINS` en el `.env` del repo incluye el dominio de Vercel.

> Aviso: si el frontend de Vercel se sirve por HTTPS, el navegador bloqueara las llamadas HTTP al VPS como "mixed content". Para resolverlo hay que sumar un dominio + TLS al VPS (por ejemplo certbot + bloque server en `:443` dentro de `nginx.conf`).

## Operaciones del dia a dia

### Actualizar la app

Desde la raiz del repo:

```
bash deploy/scripts/deploy.sh
```

El script hace `git pull --ff-only`, reconstruye y reinicia. Es idempotente.

### Reiniciar el backend sin redeployar

```
docker compose -f deploy/docker-compose.prod.yml --env-file .env restart backend
```

### Backups diarios

Programar un cron en el host (reemplaza `<REPO>` por la ruta absoluta del repo):

```
crontab -e
0 3 * * * <REPO>/deploy/scripts/backup-db.sh >> /var/log/digitalcow-backup.log 2>&1
```

El script guarda dumps comprimidos en `<REPO>/backups` y mantiene los ultimos 14 dias.

### Restaurar un backup

```
bash deploy/scripts/restore-db.sh backups/digitalcow-2026-05-18-0300.sql.gz
```

### Arranque automatico al reiniciar

La unidad systemd `deploy/systemd/digitalcow.service` usa rutas absolutas. Editar las rutas `WorkingDirectory`, `EnvironmentFile` y los `ExecStart`/`ExecStop` para que apunten al repo y `.env` reales, luego:

```
sudo cp deploy/systemd/digitalcow.service /etc/systemd/system/digitalcow.service
sudo systemctl daemon-reload
sudo systemctl enable --now digitalcow
```

## Rotacion de secretos

Si un secreto se filtra (Cloudinary, Resend, JWT, MySQL):

1. Rotar el valor en el panel del proveedor o regenerar con `openssl rand -base64 48` para `JWT_SECRET`.
2. Editar el `.env` del repo.
3. Reiniciar con `docker compose -f deploy/docker-compose.prod.yml --env-file .env restart backend`.

Si rotas el `JWT_SECRET` todos los tokens emitidos previamente quedan invalidos y los usuarios deberan iniciar sesion nuevamente.

## Solucion de problemas

- **Nginx no arranca:** revisar `docker logs digitalcow-nginx`. Casi siempre es un error de sintaxis en `nginx/nginx.conf`. Validar localmente con `docker run --rm -v $PWD/deploy/nginx/nginx.conf:/etc/nginx/nginx.conf:ro nginx:1.27-alpine nginx -t`.
- **Backend no arranca:** revisar `docker logs digitalcow-backend`. Casi siempre es una variable faltante en `.env` o MySQL no esta sano.
- **Frontend recibe CORS error:** anadir el origen exacto en `CORS_ALLOWED_ORIGINS` y reiniciar el backend.
- **Mixed content desde Vercel:** el frontend en HTTPS no puede llamar HTTP. Agregar dominio + certbot al VPS o usar Vercel solo en HTTP (no recomendado).
- **Imagen no se actualiza:** ejecutar `docker compose ... build --no-cache backend` y luego `up -d`.
