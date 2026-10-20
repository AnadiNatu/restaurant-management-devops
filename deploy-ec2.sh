#!/usr/bin/env bash
set -Eeuo pipefail

# Run this on the EC2 instance from the directory containing docker-compose.prod.yml
# and .env.prod. It pulls published images; it does not build images on the server.
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

ENV_FILE="${ENV_FILE:-.env.prod}"
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.prod.yml}"

command -v docker >/dev/null 2>&1 || { echo "ERROR: Docker is not installed." >&2; exit 1; }
docker compose version >/dev/null 2>&1 || { echo "ERROR: Docker Compose v2 is required." >&2; exit 1; }
[[ -f "$ENV_FILE" ]] || { echo "ERROR: Missing $ENV_FILE." >&2; exit 1; }
[[ -f "$COMPOSE_FILE" ]] || { echo "ERROR: Missing $COMPOSE_FILE." >&2; exit 1; }

echo "Checking production configuration..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" config -q

echo "Pulling images..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" pull

echo "Starting/updating services..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d --remove-orphans

echo "Current service state:"
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps

echo
echo "Recent backend logs:"
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs --tail=80 backend
