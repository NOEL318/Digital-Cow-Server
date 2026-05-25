#!/usr/bin/env bash
# Despliegue idempotente de Digital Cow.
# Funciona desde cualquier ruta donde este clonado el repo: detecta la ubicacion
# del script y resuelve REPO_DIR y .env relativo a ahi.
# El archivo .env se espera en la raiz del repo.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
ENV_FILE="$REPO_DIR/.env"
COMPOSE_FILE="$REPO_DIR/deploy/docker-compose.prod.yml"

if [[ ! -f "$ENV_FILE" ]]; then
    echo "ERROR: No existe $ENV_FILE" >&2
    echo "  Copia deploy/.env.prod.example a $ENV_FILE y completalo." >&2
    exit 1
fi

cd "$REPO_DIR"

echo "==> Trayendo cambios del repositorio"
git fetch --all --prune
# Fast-forward only para no perder cambios locales por error.
git pull --ff-only || echo "  (pull omitido: hay cambios locales o no es fast-forward)"

echo "==> Construyendo imagen del backend"
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" build backend

echo "==> Levantando servicios"
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d --remove-orphans

echo "==> Esperando a que el backend este sano (la base es TiDB Cloud gestionada; no hay MySQL local)"
for i in $(seq 1 30); do
    health=$(curl -fsS http://localhost/actuator/health 2>/dev/null || true)
    echo "  intento $i health=${health:-<sin respuesta>}"
    if echo "$health" | grep -q '"status":"UP"'; then
        echo "  backend sano"
        break
    fi
    sleep 5
done

echo "==> Limpiando imagenes antiguas"
docker image prune -f

echo "==> Estado actual de contenedores"
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" ps

echo "==> Despliegue completo. Logs del backend:"
echo "    docker compose -f $COMPOSE_FILE --env-file $ENV_FILE logs -f backend"
