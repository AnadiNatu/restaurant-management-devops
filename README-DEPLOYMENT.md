# Restaurant website deployment kit

## Included
- `docker-compose.yml`: local development, PostgreSQL, backend, frontend.
- `docker-compose.prod.yml`: production containers using published images and an external PostgreSQL URL (for example RDS).
- `postgres-init/`: optional first-boot PostgreSQL initialization scripts.
- `build-and-run.sh`: local Docker build/start.
- `deploy-ec2.sh`: pull/restart production services on EC2.
- `manage.sh`: production operations and database backup helper.
- `init-letsencrypt.sh`, `renew-letsencrypt.sh`: Certbot standalone certificate issue/renewal.
- `.github/workflows/deploy.yml`: build/push images and deploy to EC2 on pushes to `main`.

## Before use
1. Review the two Docker build contexts and Dockerfile paths. The workflow assumes:
   - `backend/restaurant-management/Dockerfile`
   - `frontend/restaurant-ui/Dockerfile`
2. Add `.env` for local development and `.env.prod` on EC2. Do not commit either file.
3. Ensure the frontend's Nginx configuration actually proxies `/api/` to `backend:8080` and has TLS server configuration before expecting HTTPS to work. This kit cannot modify an unseen Nginx configuration.
4. Production Compose deliberately does not publish the backend port or run a local PostgreSQL container. It expects `DB_URL` to point to your production PostgreSQL service (e.g. RDS).
5. The Let's Encrypt scripts stop the frontend temporarily because Certbot standalone needs port 80. Domain DNS must point to EC2 and TCP/80 must be open. After issuance, Nginx must reference the generated certificate paths and the frontend must be restarted.
6. Run certificate renewal periodically with cron/systemd, e.g. a weekly cron job as root. Test renewal first during a maintenance window. The scripts briefly interrupt the frontend.
7. `postgres-init` scripts run only when PostgreSQL's data directory is empty. Never delete a production volume to rerun initialization.

## Local development
```bash
cp .env.example .env   # if you have an example; otherwise create .env manually
chmod +x *.sh
./build-and-run.sh
```
Required local variables include `POSTGRES_PASSWORD`, `DB_PASSWORD`, and a strong `JWT_SECRET`. The Compose file defaults `DB_URL` to the internal `postgres` service.

## EC2 manual deployment
Upload `docker-compose.prod.yml`, `deploy-ec2.sh`, `manage.sh`, and a securely-created `.env.prod` to the same directory. Then:
```bash
chmod +x deploy-ec2.sh manage.sh init-letsencrypt.sh renew-letsencrypt.sh
./deploy-ec2.sh
```

## GitHub Actions secrets
Configure repository secrets:
- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN` (Docker Hub access token, not account password)
- `EC2_HOST` (public IP or DNS)
- `EC2_USER` (e.g. `ec2-user` or `ubuntu`, depending on AMI)
- `EC2_SSH_PRIVATE_KEY` (private key text for the EC2 host)
- `EC2_APP_DIR` (absolute deployment directory on EC2)

The workflow expects the compose file and `.env.prod` to already exist in `EC2_APP_DIR`. The SSH user must be able to run Docker commands. Set Docker Hub image names to match your actual Docker Hub account.

## Important caveats
- This workflow builds the `latest` tag and a commit-SHA tag, but deploys `latest`. For stricter release control, deploy the SHA tag instead.
- Do not expose PostgreSQL or backend ports publicly in production.
- Never store production secrets in Git, Docker image layers, workflow files, or logs.
- Test the Compose files with `docker compose --env-file .env -f docker-compose.yml config -q` and the production equivalent before deployment.
