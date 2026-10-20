#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"
ENV_FILE="${ENV_FILE:-.env.prod}"
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.prod.yml}"

usage() {
  cat <<'HELP'
Usage: ./manage.sh <command>

Commands:
  up          Pull images and start/update services
  down        Stop and remove containers (keeps named volumes)
  restart     Restart services
  status      Show service status
  logs        Follow all service logs
  backend     Follow backend logs
  frontend    Follow frontend logs
  pull        Pull configured images
  config      Validate and print Compose config (secrets may be shown!)
  backup-db   Create a PostgreSQL custom-format backup (requires pg_dump and DB env)
HELP
}

command -v docker >/dev/null 2>&1 || { echo "ERROR: Docker not found." >&2; exit 1; }
docker compose version >/dev/null 2>&1 || { echo "ERROR: Docker Compose v2 required." >&2; exit 1; }

cmd="${1:-}"
[[ -n "$cmd" ]] || { usage; exit 2; }

compose() {
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "$@"
}

case "$cmd" in
  up)
    [[ -f "$ENV_FILE" ]] || { echo "ERROR: Missing $ENV_FILE." >&2; exit 1; }
    compose config -q
    compose pull
    compose up -d --remove-orphans
    compose ps
    ;;
  down) compose down ;;
  restart) compose restart ;;
  status) compose ps ;;
  logs) compose logs -f --tail=150 ;;
  backend) compose logs -f --tail=200 backend ;;
  frontend) compose logs -f --tail=200 frontend ;;
  pull) compose pull ;;
  config)
    echo "WARNING: rendered configuration may contain secrets; do not share its output."
    compose config
    ;;
  backup-db)
    : "${DB_URL:?Set DB_URL in $ENV_FILE}"
    : "${DB_USERNAME:?Set DB_USERNAME in $ENV_FILE}"
    : "${DB_PASSWORD:?Set DB_PASSWORD in $ENV_FILE}"
    command -v pg_dump >/dev/null 2>&1 || { echo "ERROR: Install PostgreSQL client tools (pg_dump)." >&2; exit 1; }
    db_url="${DB_URL#jdbc:postgresql://}"
    hostport="${db_url%%/*}"
    database="${db_url#*/}"
    database="${database%%\?*}"
    host="${hostport%%:*}"
    port="${hostport##*:}"
    [[ "$port" =~ ^[0-9]+$ ]] || port=5432
    mkdir -p backups
    backup="backups/restaurant-$(date +%Y%m%d-%H%M%S).dump"
    PGPASSWORD="$DB_PASSWORD" pg_dump --host="$host" --port="$port" --username="$DB_USERNAME" --format=custom --file="$backup" "$database"
    chmod 600 "$backup"
    echo "Backup created: $backup"
    ;;
  *) usage; exit 2 ;;
esac
