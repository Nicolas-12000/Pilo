#!/usr/bin/env bash
# First boot: wait for SSM /pilo/demo/env-aws, then docker compose up --build.
# Later deploys: reuse .env.aws on disk and rebuild.
set -euo pipefail

REGION="${AWS_REGION:-eu-west-1}"
PARAM="${PILO_SSM_ENV_PARAM:-/pilo/demo/env-aws}"
APP_DIR="${PILO_APP_DIR:-/home/ubuntu/pilo}"

export AWS_DEFAULT_REGION="$REGION"
mkdir -p "$APP_DIR"

need_env=1
if [[ -f "$APP_DIR/.env.aws" ]] && grep -q 'SPRING_DATASOURCE_URL=jdbc:postgresql://' "$APP_DIR/.env.aws"; then
  need_env=0
fi

if [[ "$need_env" -eq 1 ]]; then
  echo "[pilo-start] waiting for SSM parameter $PARAM"
  ok=0
  for _ in $(seq 1 80); do
    if aws ssm get-parameter --name "$PARAM" --with-decryption --query Parameter.Value --output text > /tmp/pilo.env.aws 2>/dev/null; then
      if grep -q 'SPRING_DATASOURCE_URL=jdbc:postgresql://' /tmp/pilo.env.aws; then
        install -m 600 -o ubuntu -g ubuntu /tmp/pilo.env.aws "$APP_DIR/.env.aws"
        rm -f /tmp/pilo.env.aws
        ok=1
        break
      fi
    fi
    sleep 15
  done
  if [[ "$ok" -ne 1 ]]; then
    echo "[pilo-start] timed out waiting for $PARAM" >&2
    exit 1
  fi
fi

if [[ ! -f "$APP_DIR/docker-compose.ec2.yml" ]]; then
  echo "[pilo-start] $APP_DIR/docker-compose.ec2.yml missing (clone failed?)" >&2
  exit 1
fi

cd "$APP_DIR"
echo "[pilo-start] docker compose up -d --build"
docker compose -f docker-compose.ec2.yml up -d --build
echo "[pilo-start] done"
