#!/usr/bin/env bash
# Script de backup diario de la base de datos.
# Genera un dump comprimido en /opt/digital-cow/backups con marca de fecha.
# Conserva los ultimos 14 dias y elimina los mas antiguos.
# Programar via cron con: 0 3 * * * /opt/digital-cow/repo/deploy/scripts/backup-db.sh
set -euo pipefail

APP_DIR="/opt/digital-cow"
ENV_FILE="$APP_DIR/.env"
BACKUP_DIR="$APP_DIR/backups"
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
