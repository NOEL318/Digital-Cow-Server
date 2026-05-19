#!/usr/bin/env bash
# Script de restauracion de un dump generado por backup-db.sh.
# Requiere un argumento con la ruta al archivo .sql.gz a restaurar.
# Detiene el backend antes de restaurar para evitar escrituras concurrentes.
set -euo pipefail

if [[ $# -lt 1 ]]; then
    echo "Uso: $0 <ruta-al-archivo.sql.gz>" >&2
    exit 1
fi

DUMP="$1"
APP_DIR="/opt/digital-cow"
ENV_FILE="$APP_DIR/.env"
COMPOSE_FILE="$APP_DIR/repo/deploy/docker-compose.prod.yml"

if [[ ! -f "$DUMP" ]]; then
    echo "ERROR: No existe $DUMP" >&2
    exit 1
fi

set -a
source "$ENV_FILE"
set +a

echo "==> Deteniendo backend para evitar escrituras"
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" stop backend

echo "==> Restaurando $DUMP"
gunzip -c "$DUMP" | docker exec -i digitalcow-mysql \
    mysql -u root -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"

echo "==> Reiniciando backend"
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" start backend

echo "==> Restauracion completa"
