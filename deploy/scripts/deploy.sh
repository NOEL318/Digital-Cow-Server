#!/usr/bin/env bash
# Script de despliegue para Digital Cow en VPS.
# Hace pull del repositorio, construye la imagen del backend y levanta los servicios.
# Asume que el repositorio esta clonado en /opt/digital-cow/repo y que el archivo .env
# esta en /opt/digital-cow/.env. Ejecutar con permisos para Docker.
set -euo pipefail

APP_DIR="/opt/digital-cow"
REPO_DIR="$APP_DIR/repo"
ENV_FILE="$APP_DIR/.env"
COMPOSE_FILE="$REPO_DIR/deploy/docker-compose.prod.yml"

if [[ ! -f "$ENV_FILE" ]]; then
    echo "ERROR: No existe $ENV_FILE. Copia deploy/.env.prod.example y completalo." >&2
    exit 1
fi

cd "$REPO_DIR"

echo "==> Trayendo cambios del repositorio"
git fetch --all --prune
git reset --hard origin/main

echo "==> Construyendo imagen del backend"
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" build backend

echo "==> Levantando servicios (recreate si cambia)"
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d --remove-orphans

echo "==> Esperando a que el backend este sano"
for i in $(seq 1 30); do
    status=$(docker inspect --format='{{json .State.Health.Status}}' digitalcow-mysql 2>/dev/null || echo '"starting"')
    echo "  intento $i mysql=$status"
    if [[ "$status" == '"healthy"' ]]; then
        break
    fi
    sleep 5
done

echo "==> Limpiando imagenes antiguas"
docker image prune -f

echo "==> Estado actual de contenedores"
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" ps

echo "==> Despliegue completo. Revisa los logs con:"
echo "    docker compose -f $COMPOSE_FILE --env-file $ENV_FILE logs -f backend"
