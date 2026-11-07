#!/bin/bash
set -euxo pipefail
exec > /var/log/pilo-bootstrap.log 2>&1

REPO_URL="https://github.com/Nicolas-12000/Pilo.git"
APP_DIR="/home/ubuntu/pilo"

if [[ ! -f /swapfile ]]; then
  fallocate -l 2G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi
swapon /swapfile || true

export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y docker.io docker-compose-v2 git nginx curl unzip
if ! command -v aws >/dev/null 2>&1; then
  curl -sS "https://awscli.amazonaws.com/awscli-exe-linux-aarch64.zip" -o /tmp/awscliv2.zip
  unzip -q /tmp/awscliv2.zip -d /tmp
  /tmp/aws/install --update
  rm -rf /tmp/aws /tmp/awscliv2.zip
fi

systemctl enable --now docker
usermod -aG docker ubuntu

if [[ ! -d "$APP_DIR/.git" ]]; then
  rm -rf "$APP_DIR"
  for i in 1 2 3 4 5; do
    if sudo -u ubuntu git clone "$REPO_URL" "$APP_DIR"; then
      break
    fi
    sleep 10
  done
fi

if [[ -f "$APP_DIR/scripts/aws/nginx-pilo.conf" ]]; then
  cp "$APP_DIR/scripts/aws/nginx-pilo.conf" /etc/nginx/sites-available/pilo
else
  cat > /etc/nginx/sites-available/pilo <<'NGINX'
server {
    listen 80 default_server;
    listen [::]:80 default_server;
    server_name _;
    client_max_body_size 1m;
    location /actuator/health { proxy_pass http://127.0.0.1:8080; }
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 120s;
    }
    location /internal/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 120s;
    }
    location / { return 404; }
}
NGINX
fi

ln -sf /etc/nginx/sites-available/pilo /etc/nginx/sites-enabled/pilo
rm -f /etc/nginx/sites-enabled/default
nginx -t
systemctl reload nginx

# Inline copies so the first boot works before these files exist on GitHub main.
cat > /usr/local/bin/pilo-start.sh <<'START'
#!/usr/bin/env bash
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
START

cat > /usr/local/bin/pilo-update.sh <<'UPDATE'
#!/usr/bin/env bash
set -euo pipefail
APP_DIR="${PILO_APP_DIR:-/home/ubuntu/pilo}"
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
UPDATE
chmod 755 /usr/local/bin/pilo-start.sh /usr/local/bin/pilo-update.sh

cat > /etc/systemd/system/pilo-backend.service <<'UNIT'
[Unit]
Description=PILO backend (docker compose)
After=network-online.target docker.service
Wants=network-online.target docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
ExecStart=/usr/local/bin/pilo-start.sh
TimeoutStartSec=0
Restart=on-failure
RestartSec=45

[Install]
WantedBy=multi-user.target
UNIT

systemctl daemon-reload
systemctl enable --now pilo-backend.service

date -u > /var/lib/pilo-bootstrap-done
echo "bootstrap complete"
