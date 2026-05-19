#!/usr/bin/env bash
# Script de instalacion inicial en VPS Debian o Ubuntu.
# Instala Docker, Docker Compose, firewall y crea el directorio de la app.
# Ejecutar como root o con sudo.
set -euo pipefail

APP_DIR="/opt/digital-cow"

echo "==> Actualizando paquetes del sistema"
apt-get update
apt-get upgrade -y

echo "==> Instalando dependencias basicas"
apt-get install -y ca-certificates curl gnupg ufw fail2ban git

echo "==> Instalando Docker Engine"
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc || \
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc

if grep -qi ubuntu /etc/os-release; then
    DOCKER_REPO="https://download.docker.com/linux/ubuntu"
    DOCKER_CODENAME="$(. /etc/os-release && echo "$VERSION_CODENAME")"
else
    DOCKER_REPO="https://download.docker.com/linux/debian"
    DOCKER_CODENAME="$(. /etc/os-release && echo "$VERSION_CODENAME")"
fi

echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] $DOCKER_REPO $DOCKER_CODENAME stable" \
    > /etc/apt/sources.list.d/docker.list

apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

echo "==> Configurando firewall UFW"
ufw default deny incoming
ufw default allow outgoing
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw allow 443/udp
ufw --force enable

echo "==> Creando directorio de la app en $APP_DIR"
mkdir -p "$APP_DIR" "$APP_DIR/backups"
chmod 700 "$APP_DIR/backups"

echo "==> Habilitando Docker para que arranque al iniciar el sistema"
systemctl enable docker
systemctl start docker

echo "==> Listo. Pasos siguientes:"
echo "  1. Clonar el repositorio en $APP_DIR/repo"
echo "  2. Copiar deploy/.env.prod.example a $APP_DIR/.env y completar"
echo "  3. Ajustar DNS del dominio para que apunte a la IP del VPS"
echo "  4. Ejecutar deploy/scripts/deploy.sh"
