#!/usr/bin/env bash

# ==========================================================
# Restaurant Management - EC2 Deployment
# ==========================================================

set -euo pipefail


# ==========================================================
# CONFIGURATION
# ==========================================================

WORK_DIR="$HOME/restaurant-management-app"

DEVOPS_REPO="${DEVOPS_REPO:-}"
BACKEND_REPO="${BACKEND_REPO:-}"
FRONTEND_REPO="${FRONTEND_REPO:-}"


echo ""
echo "========================================================"
echo "  Restaurant Management - EC2 Deployment"
echo "========================================================"
echo ""


# ==========================================================
# DOCKER INSTALLATION
# ==========================================================

install_docker() {

    if command -v docker &>/dev/null; then

        echo "Docker already installed:"
        docker --version

        return
    fi


    echo "Installing Docker..."

    sudo apt-get update -qq

    sudo apt-get install -y \
        ca-certificates \
        curl \
        gnupg \
        lsb-release


    sudo install -m 0755 -d /etc/apt/keyrings


    curl -fsSL \
        https://download.docker.com/linux/ubuntu/gpg \
        | sudo gpg --dearmor \
        -o /etc/apt/keyrings/docker.gpg


    sudo chmod a+r /etc/apt/keyrings/docker.gpg


    echo \
        "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
        https://download.docker.com/linux/ubuntu \
        $(lsb_release -cs) stable" \
        | sudo tee \
        /etc/apt/sources.list.d/docker.list > /dev/null


    sudo apt-get update -qq


    sudo apt-get install -y \
        docker-ce \
        docker-ce-cli \
        containerd.io \
        docker-buildx-plugin \
        docker-compose-plugin


    sudo usermod -aG docker "$USER"

    echo "Docker installed."
}


install_docker


# ==========================================================
# VERIFY DOCKER COMPOSE
# ==========================================================

if ! docker compose version &>/dev/null; then

    echo "Docker Compose V2 not found."

    exit 1
fi


echo "Docker Compose:"
docker compose version


# ==========================================================
# WORK DIRECTORY
# ==========================================================

mkdir -p "$WORK_DIR"

cd "$WORK_DIR"

echo "Working directory:"
echo "$WORK_DIR"


# ==========================================================
# CLONE / UPDATE REPOSITORIES
# ==========================================================

clone_or_pull() {

    local dir="$1"

    local repo="$2"


    if [[ -d "$dir/.git" ]]; then

        echo "Updating $dir..."

        git -C "$dir" pull --ff-only


    elif [[ -n "$repo" ]]; then

        echo "Cloning $repo..."

        git clone "$repo" "$dir"


    else

        echo "Repository $dir not found."

        echo "Set the corresponding repository variable."

        exit 1
    fi
}


clone_or_pull "restaurant-management" "$BACKEND_REPO"

clone_or_pull "restaurant-ui" "$FRONTEND_REPO"


# ==========================================================
# DEVOPS REPOSITORY
# ==========================================================

if [[ -d "$WORK_DIR/devops/.git" ]]; then

    git -C "$WORK_DIR/devops" pull --ff-only

elif [[ -n "$DEVOPS_REPO" ]]; then

    git clone "$DEVOPS_REPO" "$WORK_DIR/devops"

else

    echo "DevOps repository not found."

    exit 1
fi


# ==========================================================
# ENVIRONMENT
# ==========================================================

cd "$WORK_DIR/devops"


if [[ ! -f ".env.prod" ]]; then

    echo ""
    echo ".env.prod not found."
    echo ""
    echo "Create it from:"
    echo ".env.prod.example"
    echo ""

    exit 1
fi


echo ".env.prod found."


# ==========================================================
# DEPLOY
# ==========================================================

echo ""
echo "Building and starting Restaurant Management..."
echo ""


docker compose \
    -f docker-compose.prod.yml \
    --env-file .env.prod \
    pull


docker compose \
    -f docker-compose.prod.yml \
    --env-file .env.prod \
    up -d \
    --remove-orphans


# ==========================================================
# STATUS
# ==========================================================

echo ""

echo "Container status:"

docker compose \
    -f docker-compose.prod.yml \
    ps


echo ""

echo "Deployment complete!"

echo ""

echo "Useful commands:"

echo "docker compose -f docker-compose.prod.yml --env-file .env.prod logs -f backend"

echo "docker compose -f docker-compose.prod.yml --env-file .env.prod logs -f frontend"

echo "docker compose -f docker-compose.prod.yml ps"

echo "docker compose -f docker-compose.prod.yml down"

echo ""