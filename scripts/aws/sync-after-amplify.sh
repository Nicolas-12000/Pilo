#!/usr/bin/env bash
# Uso: ./sync-after-amplify.sh https://main.xxxxx.amplifyapp.com
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
OUT="$ROOT/scripts/aws/out"
ENV_FILE="${PILO_ENV_AWS:-$OUT/env.aws}"
SSM_ENV="${PILO_SSM_ENV:-/pilo/demo/env-aws}"

export AWS_PROFILE="${AWS_PROFILE:-NicolasAlejandroGarcia}"
export AWS_REGION="${AWS_REGION:-eu-west-1}"
export AWS_DEFAULT_REGION="$AWS_REGION"

AMPLIFY_ORIGIN="${1:-}"
[[ -n "$AMPLIFY_ORIGIN" ]] || { echo "usage: $0 https://YOUR.amplifyapp.com" >&2; exit 1; }
AMPLIFY_ORIGIN="${AMPLIFY_ORIGIN%/}"

[[ -f "$ENV_FILE" ]] || { echo "missing $ENV_FILE — run provision-demo.sh first" >&2; exit 1; }

ACCOUNT="$(aws sts get-caller-identity --query Account --output text)"
BUCKET="$(grep '^PILO_S3_BUCKET=' "$ENV_FILE" | cut -d= -f2-)"
[[ -n "$BUCKET" ]] || BUCKET="pilo-docs-${ACCOUNT}"

python3 - "$ENV_FILE" "$AMPLIFY_ORIGIN" <<'PY'
import re, sys
path, origin = sys.argv[1], sys.argv[2]
text = open(path).read()
if re.search(r'^PILO_CORS_ORIGINS=', text, re.M):
    text = re.sub(r'^PILO_CORS_ORIGINS=.*$', f'PILO_CORS_ORIGINS={origin}', text, count=1, flags=re.M)
else:
    text += f'\nPILO_CORS_ORIGINS={origin}\n'
open(path, 'w').write(text)
PY

aws ssm put-parameter --name "$SSM_ENV" --type SecureString --overwrite --value "file://$ENV_FILE" >/dev/null

aws s3api put-bucket-cors --bucket "$BUCKET" --cors-configuration "{
  \"CORSRules\": [{
    \"AllowedHeaders\": [\"*\"],
    \"AllowedMethods\": [\"PUT\", \"GET\", \"HEAD\"],
    \"AllowedOrigins\": [\"http://localhost:3000\", \"http://127.0.0.1:3000\", \"${AMPLIFY_ORIGIN}\"],
    \"ExposeHeaders\": [\"ETag\"],
    \"MaxAgeSeconds\": 3000
  }]
}"

INSTANCE_ID="$(aws ec2 describe-instances \
  --filters Name=tag:Name,Values=pilo-api Name=instance-state-name,Values=running \
  --query 'Reservations[0].Instances[0].InstanceId' --output text)"
[[ -n "$INSTANCE_ID" && "$INSTANCE_ID" != "None" ]] || { echo "EC2 not running; start with start-demo.sh" >&2; exit 1; }

CMD="$(aws ssm send-command \
  --instance-ids "$INSTANCE_ID" \
  --document-name AWS-RunShellScript \
  --comment "sync env after Amplify" \
  --parameters commands="[\"aws ssm get-parameter --name $SSM_ENV --with-decryption --region $AWS_REGION --query Parameter.Value --output text > /tmp/env.aws && install -m 600 -o ubuntu -g ubuntu /tmp/env.aws /home/ubuntu/pilo/.env.aws && cd /home/ubuntu/pilo && docker compose -f docker-compose.ec2.yml up -d\"]" \
  --query Command.CommandId --output text)"

echo "SSM $CMD on $INSTANCE_ID — esperá ~1 min y probá login en $AMPLIFY_ORIGIN"
