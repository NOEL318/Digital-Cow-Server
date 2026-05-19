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

Copiar este repositorio o solo el directorio `deploy/` a la VPS y ejecutar el instalador:

```
bash deploy/scripts/install-vps.sh
```

El script instala Docker, Docker Compose, configura el firewall UFW y crea `/opt/digital-cow`. Despues clonar el repositorio en esa ruta:

```
git clone https://github.com/tu-usuario/digital-cow.git /opt/digital-cow/repo
```

### 3. Configurar variables de entorno

Copiar la plantilla y completar los valores reales:

```
cp /opt/digital-cow/repo/deploy/.env.prod.example /opt/digital-cow/.env
chmod 600 /opt/digital-cow/.env
nano /opt/digital-cow/.env
```

Las variables criticas son `JWT_SECRET`, los passwords de MySQL y las claves de Cloudinary y Resend. Generar el `JWT_SECRET` con `openssl rand -base64 48`.

### 4. Levantar los servicios

```
bash /opt/digital-cow/repo/deploy/scripts/deploy.sh
```

El script reconstruye la imagen del backend, levanta los tres contenedores y muestra el estado. Los logs se ven con:

```
docker compose -f /opt/digital-cow/repo/deploy/docker-compose.prod.yml --env-file /opt/digital-cow/.env logs -f
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

Configurar en el panel de Vercel la variable de entorno `VITE_API_URL=http://<IP_DEL_VPS>/api/v1` y redeployar. Asegurarse que `CORS_ALLOWED_ORIGINS` en `/opt/digital-cow/.env` incluye el dominio de Vercel.

> Aviso: si el frontend de Vercel se sirve por HTTPS, el navegador bloqueara las llamadas HTTP al VPS como "mixed content". Para resolverlo hay que sumar un dominio + TLS al VPS (por ejemplo certbot + bloque server en `:443` dentro de `nginx.conf`).

## Operaciones del dia a dia

### Actualizar la app

```
bash /opt/digital-cow/repo/deploy/scripts/deploy.sh
```

El script hace `git pull`, reconstruye y reinicia. Es idempotente.

### Reiniciar el backend sin redeployar

```
docker compose -f /opt/digital-cow/repo/deploy/docker-compose.prod.yml --env-file /opt/digital-cow/.env restart backend
```

### Backups diarios

Programar un cron en el host:

```
crontab -e
0 3 * * * /opt/digital-cow/repo/deploy/scripts/backup-db.sh >> /var/log/digitalcow-backup.log 2>&1
```

El script guarda dumps comprimidos en `/opt/digital-cow/backups` y mantiene los ultimos 14 dias.

### Restaurar un backup

```
bash /opt/digital-cow/repo/deploy/scripts/restore-db.sh /opt/digital-cow/backups/digitalcow-2026-05-18-0300.sql.gz
```

### Arranque automatico al reiniciar

Instalar la unidad systemd:

```
sudo cp /opt/digital-cow/repo/deploy/systemd/digitalcow.service /etc/systemd/system/digitalcow.service
sudo systemctl daemon-reload
sudo systemctl enable --now digitalcow
```

## Rotacion de secretos

Si un secreto se filtra (Cloudinary, Resend, JWT, MySQL):

1. Rotar el valor en el panel del proveedor o regenerar con `openssl rand -base64 48` para `JWT_SECRET`.
2. Editar `/opt/digital-cow/.env`.
3. Reiniciar con `docker compose -f deploy/docker-compose.prod.yml --env-file /opt/digital-cow/.env restart backend`.

Si rotas el `JWT_SECRET` todos los tokens emitidos previamente quedan invalidos y los usuarios deberan iniciar sesion nuevamente.

## Solucion de problemas

- **Nginx no arranca:** revisar `docker logs digitalcow-nginx`. Casi siempre es un error de sintaxis en `nginx/nginx.conf`. Validar localmente con `docker run --rm -v $PWD/deploy/nginx/nginx.conf:/etc/nginx/nginx.conf:ro nginx:1.27-alpine nginx -t`.
- **Backend no arranca:** revisar `docker logs digitalcow-backend`. Casi siempre es una variable faltante en `.env` o MySQL no esta sano.
- **Frontend recibe CORS error:** anadir el origen exacto en `CORS_ALLOWED_ORIGINS` y reiniciar el backend.
- **Mixed content desde Vercel:** el frontend en HTTPS no puede llamar HTTP. Agregar dominio + certbot al VPS o usar Vercel solo en HTTP (no recomendado).
- **Imagen no se actualiza:** ejecutar `docker compose ... build --no-cache backend` y luego `up -d`.
