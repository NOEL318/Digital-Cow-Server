#!/usr/bin/env bash
# Backup diario de la base de datos. Genera dump comprimido en <repo>/backups
# con marca de fecha y conserva los ultimos 14 dias.
# Programar via cron: 0 3 * * * /ruta/al/repo/deploy/scripts/backup-db.sh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
ENV_FILE="$REPO_DIR/.env"
BACKUP_DIR="$REPO_DIR/backups"
TIMESTAMP="$(date +%F-%H%M)"
OUT="$BACKUP_DIR/digitalcow-$TIMESTAMP.sql.gz"

if [[ ! -f "$ENV_FILE" ]]; then
    echo "ERROR: No existe $ENV_FILE" >&2
    exit 1
fi

set -a
source "$ENV_FILE"
set +a

mkdir -p "$BACKUP_DIR"

echo "==> Creando dump en $OUT"
docker exec digitalcow-mysql \
    mysqldump \
        --single-transaction \
        --quick \
        --routines \
        --triggers \
        -u root \
        -p"$MYSQL_ROOT_PASSWORD" \
        "$MYSQL_DATABASE" \
    | gzip > "$OUT"

echo "==> Borrando backups con mas de 14 dias"
find "$BACKUP_DIR" -name "digitalcow-*.sql.gz" -mtime +14 -delete

echo "==> Backup completo. Tamano: $(du -h "$OUT" | cut -f1)"
