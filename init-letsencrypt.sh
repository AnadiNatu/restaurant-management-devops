#!/usr/bin/env bash
set -Eeuo pipefail

# Uses Certbot standalone mode. The frontend container must release port 80 while
# ACME HTTP-01 validation runs. DNS must already point to this EC2 host and inbound
# TCP/80 must be allowed by the EC2 security group and OS firewall.
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

ENV_FILE="${ENV_FILE:-.env.prod}"
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.prod.yml}"
[[ -f "$ENV_FILE" ]] || { echo "ERROR: Missing $ENV_FILE." >&2; exit 1; }

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

: "${DOMAIN:?Set DOMAIN in .env.prod, e.g. example.com}"
: "${CERTBOT_EMAIL:?Set CERTBOT_EMAIL in .env.prod}"
CERT_NAME="${CERT_NAME:-$DOMAIN}"
STAGING="${CERTBOT_STAGING:-0}"

command -v docker >/dev/null 2>&1 || { echo "ERROR: Docker is required." >&2; exit 1; }
docker compose version >/dev/null 2>&1 || { echo "ERROR: Docker Compose v2 required." >&2; exit 1; }

mkdir -p /etc/letsencrypt
if [[ -d "/etc/letsencrypt/live/$CERT_NAME" ]]; then
  echo "Certificate already exists at /etc/letsencrypt/live/$CERT_NAME"
  echo "Use renew-letsencrypt.sh to renew it; not overwriting it."
  exit 0
fi

echo "Stopping frontend to free port 80 for Certbot..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" stop frontend || true

cleanup() {
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" start frontend >/dev/null 2>&1 || true
}
trap cleanup EXIT

staging_args=()
if [[ "$STAGING" == "1" ]]; then
  staging_args+=(--staging)
fi

docker run --rm -it \
  -p 80:80 \
  -v /etc/letsencrypt:/etc/letsencrypt \
  certbot/certbot:latest certonly \
  --standalone \
  --preferred-challenges http \
  --non-interactive --agree-tos \
  --email "$CERTBOT_EMAIL" \
  --cert-name "$CERT_NAME" \
  -d "$DOMAIN" \
  "${staging_args[@]}"

echo "Certificate request finished. Ensure frontend Nginx is configured for TLS and references:"
echo "  /etc/letsencrypt/live/$CERT_NAME/fullchain.pem"
echo "  /etc/letsencrypt/live/$CERT_NAME/privkey.pem"
