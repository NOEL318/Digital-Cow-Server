#!/usr/bin/env bash
# Restauracion de un dump generado por backup-db.sh.
# Detiene el backend antes de restaurar para evitar escrituras concurrentes.
# Uso: ./restore-db.sh <ruta-al-archivo.sql.gz>
set -euo pipefail

if [[ $# -lt 1 ]]; then
    echo "Uso: $0 <ruta-al-archivo.sql.gz>" >&2
    exit 1
fi

DUMP="$1"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
ENV_FILE="$REPO_DIR/.env"
COMPOSE_FILE="$REPO_DIR/deploy/docker-compose.prod.yml"

if [[ ! -f "$DUMP" ]]; then
    echo "ERROR: No existe $DUMP" >&2
    exit 1
fi
if [[ ! -f "$ENV_FILE" ]]; then
    echo "ERROR: No existe $ENV_FILE" >&2
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
