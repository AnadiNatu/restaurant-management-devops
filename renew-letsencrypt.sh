#!/usr/bin/env bash
set -Eeuo pipefail

# Renew certificates using standalone HTTP-01 validation. Port 80 must be reachable.
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"
ENV_FILE="${ENV_FILE:-.env.prod}"
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.prod.yml}"

[[ -f "$ENV_FILE" ]] || { echo "ERROR: Missing $ENV_FILE." >&2; exit 1; }
command -v docker >/dev/null 2>&1 || { echo "ERROR: Docker is required." >&2; exit 1; }

echo "Stopping frontend briefly to release port 80..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" stop frontend || true

cleanup() {
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" start frontend >/dev/null 2>&1 || true
}
trap cleanup EXIT

docker run --rm \
  -p 80:80 \
  -v /etc/letsencrypt:/etc/letsencrypt \
  certbot/certbot:latest renew \
  --standalone \
  --non-interactive

echo "Certbot renewal check completed. Restarting frontend to reload certificates..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d frontend
