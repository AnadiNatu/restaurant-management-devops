#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.yml}"
ENV_FILE="${ENV_FILE:-.env}"

command -v docker >/dev/null 2>&1 || { echo "ERROR: Docker is not installed/in PATH." >&2; exit 1; }
docker compose version >/dev/null 2>&1 || { echo "ERROR: Docker Compose v2 is required." >&2; exit 1; }
[[ -f "$ENV_FILE" ]] || {
  echo "ERROR: $ENV_FILE not found. Create it from .env.example and fill in secrets." >&2
  exit 1
}
[[ -f "$COMPOSE_FILE" ]] || { echo "ERROR: Compose file $COMPOSE_FILE not found." >&2; exit 1; }

echo "Validating Compose configuration..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" config -q

echo "Building and starting restaurant application..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d --build

echo
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps
echo
echo "Logs: docker compose --env-file $ENV_FILE -f $COMPOSE_FILE logs -f"
