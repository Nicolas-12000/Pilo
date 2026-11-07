#!/usr/bin/env bash
# Pull main and rebuild the backend container. Used by GitHub Actions via SSM.
set -euo pipefail

APP_DIR="${PILO_APP_DIR:-/home/ubuntu/pilo}"

# SSM (root) cannot run git in /home/ubuntu/pilo without safe.directory or dropping privileges.
if [[ "$(id -u)" -eq 0 ]]; then
  export PILO_APP_DIR="$APP_DIR"
  exec sudo -u ubuntu -E HOME=/home/ubuntu bash "$0" "$@"
fi

cd "$APP_DIR"

if [[ ! -f .env.aws ]]; then
  echo "[pilo-update] missing .env.aws" >&2
  exit 1
fi

git fetch origin
git reset --hard origin/main
docker compose -f docker-compose.ec2.yml up -d --build

TAG="$(git rev-parse --short HEAD)"
docker tag "pilo-backend:${PILO_IMAGE_TAG:-ec2}" "pilo-backend:${TAG}" || true
docker tag "pilo-backend:${PILO_IMAGE_TAG:-ec2}" pilo-backend:previous || true
echo "[pilo-update] deployed $TAG"
