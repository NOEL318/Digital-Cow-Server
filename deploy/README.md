# Despliegue de Digital Cow en VPS privada

Esta carpeta contiene todo lo necesario para desplegar el backend de Digital Cow en una VPS Debian o Ubuntu. El frontend continua sirviendose desde Vercel; este despliegue solo cubre la API publica.

## Que se despliega

- MySQL 8 en contenedor con volumen persistente y red interna (no expone puertos al exterior).
- Backend Java 21 + Spring Boot 3.3 en contenedor, leyendo configuracion de un archivo .env externo.
- Caddy como proxy inverso con TLS automatico via Let's Encrypt.

## Requisitos

- VPS con Debian 12 o Ubuntu 22.04 con acceso root.
- Dominio publico apuntando a la IP del VPS con un registro A. Ejemplo: api.tudominio.com.
- Acceso saliente a Internet para descargar imagenes y emitir certificados.

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

Las variables criticas son `DOMAIN`, `JWT_SECRET`, los passwords de MySQL y las claves de Cloudinary y Resend. Generar el `JWT_SECRET` con `openssl rand -base64 48`.

### 4. Apuntar el DNS

Crear un registro A en el panel de tu proveedor de DNS que apunte `DOMAIN` a la IP del VPS. Verificar con `dig +short api.tudominio.com` antes de continuar.

### 5. Levantar los servicios

```
bash /opt/digital-cow/repo/deploy/scripts/deploy.sh
```

El script reconstruye la imagen del backend, levanta los tres contenedores y muestra el estado. Caddy obtendra el certificado TLS automaticamente la primera vez. Los logs se ven con:

```
docker compose -f /opt/digital-cow/repo/deploy/docker-compose.prod.yml --env-file /opt/digital-cow/.env logs -f
```

### 6. Verificacion

Probar la API publica:

```
curl -i https://api.tudominio.com/actuator/health
```

Debe devolver `200 OK` con un cuerpo JSON `{"status":"UP"}`.

### 7. Crear el superadmin

Al primer arranque el backend crea un superadmin y registra una linea en el log con la password aleatoria:

```
docker logs digitalcow-backend 2>&1 | grep "SUPERADMIN CREATED"
```

Iniciar sesion en `https://digital-cow.vercel.app/admin/login` con esas credenciales y cambiar la password en Ajustes.

### 8. Conectar el frontend

Configurar en el panel de Vercel la variable de entorno `VITE_API_URL=https://api.tudominio.com/api/v1` y redeployar. Asegurarse que `CORS_ALLOWED_ORIGINS` en `/opt/digital-cow/.env` incluye el dominio de Vercel.

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

- **Caddy no obtiene certificado:** verificar que el DNS apunte al VPS y que los puertos 80 y 443 esten abiertos. Revisar `docker logs digitalcow-caddy`.
- **Backend no arranca:** revisar `docker logs digitalcow-backend`. Casi siempre es una variable faltante en `.env` o MySQL no esta sano.
- **Frontend recibe CORS error:** anadir el origen exacto en `CORS_ALLOWED_ORIGINS` y reiniciar el backend.
- **Imagen no se actualiza:** ejecutar `docker compose ... build --no-cache backend` y luego `up -d`.
