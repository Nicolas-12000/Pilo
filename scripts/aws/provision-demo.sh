#!/usr/bin/env bash
# Provision the PILO demo stack in eu-west-1 (SG, RDS, S3, IAM, EC2, EIP, Lambda, GitHub OIDC).
# Idempotent: re-running skips resources that already exist.
# Secrets go to scripts/aws/out/ (gitignored). Never prints passwords.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
OUT="$ROOT/scripts/aws/out"
USER_DATA="$ROOT/scripts/aws/ec2-user-data.sh"
WORKER="$ROOT/pilo-worker"

export AWS_PROFILE="${AWS_PROFILE:-NicolasAlejandroGarcia}"
export AWS_DEFAULT_REGION="${AWS_DEFAULT_REGION:-eu-west-1}"
export AWS_REGION="${AWS_REGION:-eu-west-1}"
export AWS_PAGER=""

PROJECT="pilo"
NAME_API="pilo-api"
KEY_NAME="pilo-ec2"
DB_ID="pilo-demo"
DB_NAME="pilo"
DB_USER="pilo_admin"
LAMBDA_NAME="pilo-document-processor-dev"
OIDC_ROLE="pilo-github-actions"
GITHUB_REPO="Nicolas-12000/Pilo"
SSM_ENV="/pilo/demo/env-aws"

mkdir -p "$OUT"
chmod 700 "$OUT"
touch "$OUT/.gitignore"
if [[ ! -s "$OUT/.gitignore" ]]; then
  printf '*\n!.gitignore\n' > "$OUT/.gitignore"
fi

log() { printf '==> %s\n' "$*"; }
die() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }

require_cmd() {
  command -v "$1" >/dev/null || die "missing command: $1"
}

json_escape() {
  python3 -c 'import json,sys; print(json.dumps(sys.stdin.read()), end="")'
}

ACCOUNT="$(aws sts get-caller-identity --query Account --output text)"
[[ -n "$ACCOUNT" && "$ACCOUNT" != "None" ]] || die "AWS CLI not authenticated. Use profile $AWS_PROFILE"
log "account $ACCOUNT  region $AWS_REGION  profile $AWS_PROFILE"

BUCKET="${PILO_S3_BUCKET:-pilo-docs-${ACCOUNT}}"
VPC_ID="$(aws ec2 describe-vpcs --filters Name=isDefault,Values=true --query 'Vpcs[0].VpcId' --output text)"
[[ -n "$VPC_ID" && "$VPC_ID" != "None" ]] || die "no default VPC in $AWS_REGION"

MY_IP="$(curl -fsS https://checkip.amazonaws.com | tr -d '[:space:]')"
[[ "$MY_IP" =~ ^[0-9.]+$ ]] || die "could not detect public IP"
SSH_CIDR="${PILO_SSH_CIDR:-${MY_IP}/32}"
log "SSH ingress $SSH_CIDR (override with PILO_SSH_CIDR)"

# --- secrets (reuse if present) ---
SECRETS="$OUT/secrets.env"
if [[ -f "$SECRETS" ]]; then
  # shellcheck disable=SC1090
  set -a && source "$SECRETS" && set +a
  log "reusing secrets in $SECRETS"
fi
if [[ -z "${RDS_PASSWORD:-}" || -z "${PILO_JWT_SECRET:-}" || -z "${PILO_INTERNAL_API_KEY:-}" ]]; then
  RDS_PASSWORD="$(python3 -c 'import secrets,string; a=string.ascii_letters+string.digits; print("".join(secrets.choice(a) for _ in range(28)))')"
  PILO_JWT_SECRET="$(openssl rand -base64 32)"
  PILO_INTERNAL_API_KEY="$(openssl rand -base64 32)"
  umask 077
  cat > "$SECRETS" <<EOF
RDS_PASSWORD=$RDS_PASSWORD
PILO_JWT_SECRET=$PILO_JWT_SECRET
PILO_INTERNAL_API_KEY=$PILO_INTERNAL_API_KEY
EOF
  log "wrote new secrets to $SECRETS"
fi

# --- key pair ---
PEM="$OUT/${KEY_NAME}.pem"
if aws ec2 describe-key-pairs --key-names "$KEY_NAME" >/dev/null 2>&1; then
  log "key pair $KEY_NAME exists"
  [[ -f "$PEM" ]] || log "WARNING: $PEM missing locally; SSH will not work until you have the private key"
else
  aws ec2 create-key-pair --key-name "$KEY_NAME" --query KeyMaterial --output text > "$PEM"
  chmod 400 "$PEM"
  log "created key pair, private key $PEM"
fi

# --- security groups ---
sg_id_by_name() {
  aws ec2 describe-security-groups \
    --filters "Name=vpc-id,Values=$VPC_ID" "Name=group-name,Values=$1" \
    --query 'SecurityGroups[0].GroupId' --output text
}

sync_ssh_ingress() {
  local sg_id=$1
  local existing
  existing="$(aws ec2 describe-security-groups --group-ids "$sg_id" \
    --query 'SecurityGroups[0].IpPermissions[?FromPort==`22`].IpRanges[].CidrIp' --output text)"
  for cidr in $existing; do
    [[ -z "$cidr" || "$cidr" == "None" ]] && continue
    if [[ "$cidr" != "$SSH_CIDR" ]]; then
      aws ec2 revoke-security-group-ingress --group-id "$sg_id" \
        --ip-permissions "IpProtocol=tcp,FromPort=22,ToPort=22,IpRanges=[{CidrIp=$cidr}]" >/dev/null 2>&1 || true
    fi
  done
  if ! grep -qF "$SSH_CIDR" <<<"$existing"; then
    aws ec2 authorize-security-group-ingress --group-id "$sg_id" --ip-permissions \
      "IpProtocol=tcp,FromPort=22,ToPort=22,IpRanges=[{CidrIp=$SSH_CIDR,Description=SSH from provisioner}]" >/dev/null
    log "SSH on $sg_id -> $SSH_CIDR"
  fi
}

SG_EC2="$(sg_id_by_name pilo-ec2)"
if [[ -z "$SG_EC2" || "$SG_EC2" == "None" ]]; then
  SG_EC2="$(aws ec2 create-security-group --group-name pilo-ec2 --description "PILO nginx + SSH" --vpc-id "$VPC_ID" --query GroupId --output text)"
  aws ec2 create-tags --resources "$SG_EC2" --tags Key=Project,Value="$PROJECT" Key=Name,Value=pilo-ec2
  aws ec2 authorize-security-group-ingress --group-id "$SG_EC2" --ip-permissions \
    "IpProtocol=tcp,FromPort=22,ToPort=22,IpRanges=[{CidrIp=$SSH_CIDR,Description=SSH from provisioner}]" \
    "IpProtocol=tcp,FromPort=80,ToPort=80,IpRanges=[{CidrIp=0.0.0.0/0,Description=HTTP nginx}]"
  log "created SG pilo-ec2 $SG_EC2"
else
  log "SG pilo-ec2 $SG_EC2"
  sync_ssh_ingress "$SG_EC2"
fi

SG_RDS="$(sg_id_by_name pilo-rds)"
if [[ -z "$SG_RDS" || "$SG_RDS" == "None" ]]; then
  SG_RDS="$(aws ec2 create-security-group --group-name pilo-rds --description "PILO RDS from EC2" --vpc-id "$VPC_ID" --query GroupId --output text)"
  aws ec2 create-tags --resources "$SG_RDS" --tags Key=Project,Value="$PROJECT" Key=Name,Value=pilo-rds
  aws ec2 authorize-security-group-ingress --group-id "$SG_RDS" --ip-permissions \
    "IpProtocol=tcp,FromPort=5432,ToPort=5432,UserIdGroupPairs=[{GroupId=$SG_EC2,Description=Postgres from pilo-ec2}]"
  log "created SG pilo-rds $SG_RDS"
else
  log "SG pilo-rds $SG_RDS"
fi

# --- S3 ---
if aws s3api head-bucket --bucket "$BUCKET" 2>/dev/null; then
  log "bucket s3://$BUCKET"
else
  aws s3api create-bucket --bucket "$BUCKET" --create-bucket-configuration LocationConstraint="$AWS_REGION"
  log "created s3://$BUCKET"
fi
aws s3api put-public-access-block --bucket "$BUCKET" --public-access-block-configuration \
  BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true
aws s3api put-bucket-encryption --bucket "$BUCKET" --server-side-encryption-configuration \
  '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'
aws s3api put-bucket-cors --bucket "$BUCKET" --cors-configuration '{
  "CORSRules": [{
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["PUT", "GET", "HEAD"],
    "AllowedOrigins": ["http://localhost:3000", "http://127.0.0.1:3000"],
    "ExposeHeaders": ["ETag"],
    "MaxAgeSeconds": 3000
  }]
}'
aws s3api put-bucket-tagging --bucket "$BUCKET" --tagging "TagSet=[{Key=Project,Value=$PROJECT}]"

# --- IAM Lambda ---
LAMBDA_ROLE="pilo-document-processor-role"
LAMBDA_TRUST='{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"Service":"lambda.amazonaws.com"},"Action":"sts:AssumeRole"}]}'
if aws iam get-role --role-name "$LAMBDA_ROLE" >/dev/null 2>&1; then
  log "IAM role $LAMBDA_ROLE"
else
  aws iam create-role --role-name "$LAMBDA_ROLE" --assume-role-policy-document "$LAMBDA_TRUST" \
    --tags Key=Project,Value="$PROJECT" >/dev/null
  log "created IAM role $LAMBDA_ROLE"
fi
aws iam attach-role-policy --role-name "$LAMBDA_ROLE" \
  --policy-arn arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole
aws iam put-role-policy --role-name "$LAMBDA_ROLE" --policy-name pilo-s3-read --policy-document "{
  \"Version\":\"2012-10-17\",
  \"Statement\":[{\"Effect\":\"Allow\",\"Action\":[\"s3:GetObject\"],\"Resource\":\"arn:aws:s3:::${BUCKET}/cases/*\"}]
}"
LAMBDA_ROLE_ARN="$(aws iam get-role --role-name "$LAMBDA_ROLE" --query Role.Arn --output text)"

# --- IAM EC2 instance profile ---
EC2_ROLE="pilo-ec2-s3"
EC2_TRUST='{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"Service":"ec2.amazonaws.com"},"Action":"sts:AssumeRole"}]}'
if aws iam get-role --role-name "$EC2_ROLE" >/dev/null 2>&1; then
  log "IAM role $EC2_ROLE"
else
  aws iam create-role --role-name "$EC2_ROLE" --assume-role-policy-document "$EC2_TRUST" \
    --tags Key=Project,Value="$PROJECT" >/dev/null
fi
aws iam attach-role-policy --role-name "$EC2_ROLE" \
  --policy-arn arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore
aws iam put-role-policy --role-name "$EC2_ROLE" --policy-name pilo-ec2-inline --policy-document "{
  \"Version\":\"2012-10-17\",
  \"Statement\":[
    {\"Effect\":\"Allow\",\"Action\":[\"s3:PutObject\",\"s3:GetObject\",\"s3:DeleteObject\",\"s3:AbortMultipartUpload\"],\"Resource\":\"arn:aws:s3:::${BUCKET}/cases/*\"},
    {\"Effect\":\"Allow\",\"Action\":[\"ssm:GetParameter\",\"ssm:GetParameters\"],\"Resource\":\"arn:aws:ssm:${AWS_REGION}:${ACCOUNT}:parameter/pilo/demo/*\"}
  ]
}"
if aws iam get-instance-profile --instance-profile-name "$EC2_ROLE" >/dev/null 2>&1; then
  log "instance profile $EC2_ROLE"
else
  aws iam create-instance-profile --instance-profile-name "$EC2_ROLE" >/dev/null
  aws iam add-role-to-instance-profile --instance-profile-name "$EC2_ROLE" --role-name "$EC2_ROLE"
  log "created instance profile $EC2_ROLE (waiting for IAM propagation)"
  sleep 15
fi

# --- GitHub OIDC for Actions ---
OIDC_URL="https://token.actions.githubusercontent.com"
OIDC_ARN="arn:aws:iam::${ACCOUNT}:oidc-provider/token.actions.githubusercontent.com"
if aws iam get-open-id-connect-provider --open-id-connect-provider-arn "$OIDC_ARN" >/dev/null 2>&1; then
  log "OIDC provider GitHub Actions"
else
  aws iam create-open-id-connect-provider \
    --url "$OIDC_URL" \
    --client-id-list sts.amazonaws.com \
    --thumbprint-list 6938fd4d98bab03faadb97b34396831e3780aea1 1c58a3a8518e8759bf075b76b750d4f2df264fcd \
    --tags Key=Project,Value="$PROJECT" >/dev/null
  log "created OIDC provider"
fi
# GitHub may send legacy sub (repo:ORG/REPO:*) or immutable sub (repo:ORG@id/REPO@id:*) since 2026.
GITHUB_OWNER="${GITHUB_OWNER:-Nicolas-12000}"
GITHUB_REPO_NAME="${GITHUB_REPO_NAME:-Pilo}"
GITHUB_OWNER_ID="${GITHUB_OWNER_ID:-190977331}"
GITHUB_REPO_ID="${GITHUB_REPO_ID:-1344265257}"
GH_TRUST=$(cat <<EOF
{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"Federated":"${OIDC_ARN}"},"Action":"sts:AssumeRoleWithWebIdentity","Condition":{"StringEquals":{"token.actions.githubusercontent.com:aud":"sts.amazonaws.com"},"StringLike":{"token.actions.githubusercontent.com:sub":["repo:${GITHUB_REPO}:*","repo:${GITHUB_OWNER}@${GITHUB_OWNER_ID}/${GITHUB_REPO_NAME}@${GITHUB_REPO_ID}:*"]}}}]}
EOF
)
if aws iam get-role --role-name "$OIDC_ROLE" >/dev/null 2>&1; then
  aws iam update-assume-role-policy --role-name "$OIDC_ROLE" --policy-document "$GH_TRUST"
  log "IAM role $OIDC_ROLE"
else
  aws iam create-role --role-name "$OIDC_ROLE" --assume-role-policy-document "$GH_TRUST" \
    --tags Key=Project,Value="$PROJECT" >/dev/null
  log "created IAM role $OIDC_ROLE"
fi
aws iam put-role-policy --role-name "$OIDC_ROLE" --policy-name pilo-github-deploy --policy-document "{
  \"Version\":\"2012-10-17\",
  \"Statement\":[
    {\"Effect\":\"Allow\",\"Action\":[\"lambda:UpdateFunctionCode\",\"lambda:UpdateFunctionConfiguration\",\"lambda:GetFunction\",\"lambda:GetFunctionConfiguration\"],\"Resource\":\"arn:aws:lambda:${AWS_REGION}:${ACCOUNT}:function:${LAMBDA_NAME}\"},
    {\"Effect\":\"Allow\",\"Action\":[\"ec2:DescribeInstances\",\"ssm:DescribeInstanceInformation\"],\"Resource\":\"*\"},
    {\"Effect\":\"Allow\",\"Action\":[\"ssm:SendCommand\"],\"Resource\":[\"arn:aws:ec2:${AWS_REGION}:${ACCOUNT}:instance/*\",\"arn:aws:ssm:${AWS_REGION}::document/AWS-RunShellScript\"]},
    {\"Effect\":\"Allow\",\"Action\":[\"ssm:GetCommandInvocation\",\"ssm:ListCommandInvocations\"],\"Resource\":\"*\"}
  ]
}"

# --- RDS subnet group + instance ---
SUBNET_IDS="$(aws ec2 describe-subnets --filters "Name=vpc-id,Values=$VPC_ID" "Name=default-for-az,Values=true" --query 'Subnets[].SubnetId' --output text)"
read -r -a SUBNET_ARR <<< "$SUBNET_IDS"
if aws rds describe-db-subnet-groups --db-subnet-group-name pilo-default >/dev/null 2>&1; then
  log "DB subnet group pilo-default"
else
  aws rds create-db-subnet-group --db-subnet-group-name pilo-default \
    --db-subnet-group-description "PILO default VPC" \
    --subnet-ids "${SUBNET_ARR[@]}" \
    --tags Key=Project,Value="$PROJECT" >/dev/null
  log "created DB subnet group"
fi

RDS_STATUS="$(aws rds describe-db-instances --db-instance-identifier "$DB_ID" --query 'DBInstances[0].DBInstanceStatus' --output text 2>/dev/null || true)"
if [[ -z "$RDS_STATUS" || "$RDS_STATUS" == "None" ]]; then
  log "creating RDS $DB_ID (this takes ~8-12 min)"
  aws rds create-db-instance \
    --db-instance-identifier "$DB_ID" \
    --db-instance-class db.t4g.micro \
    --engine postgres \
    --engine-version 16.15 \
    --master-username "$DB_USER" \
    --master-user-password "$RDS_PASSWORD" \
    --allocated-storage 20 \
    --storage-type gp3 \
    --db-name "$DB_NAME" \
    --vpc-security-group-ids "$SG_RDS" \
    --db-subnet-group-name pilo-default \
    --no-publicly-accessible \
    --backup-retention-period 1 \
    --no-multi-az \
    --storage-encrypted \
    --no-deletion-protection \
    --tags Key=Project,Value="$PROJECT" Key=Name,Value="$DB_ID" >/dev/null
else
  log "RDS $DB_ID status=$RDS_STATUS"
fi

# --- Elastic IP (allocate early so Lambda env can use it) ---
EIP_ALLOC="$(aws ec2 describe-addresses --filters Name=tag:Name,Values=$NAME_API --query 'Addresses[0].AllocationId' --output text)"
EIP="$(aws ec2 describe-addresses --filters Name=tag:Name,Values=$NAME_API --query 'Addresses[0].PublicIp' --output text)"
if [[ -z "$EIP_ALLOC" || "$EIP_ALLOC" == "None" ]]; then
  read -r EIP_ALLOC EIP < <(aws ec2 allocate-address --domain vpc \
    --tag-specifications "ResourceType=elastic-ip,Tags=[{Key=Name,Value=$NAME_API},{Key=Project,Value=$PROJECT}]" \
    --query '[AllocationId,PublicIp]' --output text)
  log "allocated Elastic IP $EIP"
else
  log "Elastic IP $EIP ($EIP_ALLOC)"
fi

# --- EC2 ---
INSTANCE_ID="$(aws ec2 describe-instances \
  --filters Name=tag:Name,Values=$NAME_API Name=instance-state-name,Values=pending,running,stopping,stopped \
  --query 'Reservations[0].Instances[0].InstanceId' --output text)"
if [[ -z "$INSTANCE_ID" || "$INSTANCE_ID" == "None" ]]; then
  AMI="$(aws ssm get-parameters --names /aws/service/canonical/ubuntu/server/24.04/stable/current/arm64/hvm/ebs-gp3/ami-id --query 'Parameters[0].Value' --output text)"
  log "launching t4g.micro $AMI"
  INSTANCE_ID="$(aws ec2 run-instances \
    --image-id "$AMI" \
    --instance-type t4g.micro \
    --key-name "$KEY_NAME" \
    --security-group-ids "$SG_EC2" \
    --iam-instance-profile "Name=$EC2_ROLE" \
    --block-device-mappings '[{"DeviceName":"/dev/sda1","Ebs":{"VolumeSize":20,"VolumeType":"gp3","DeleteOnTermination":true}}]' \
    --user-data "file://${USER_DATA}" \
    --metadata-options HttpTokens=required,HttpPutResponseHopLimit=2 \
    --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=$NAME_API},{Key=Project,Value=$PROJECT}]" \
    --query 'Instances[0].InstanceId' --output text)"
  log "started $INSTANCE_ID"
else
  log "EC2 $INSTANCE_ID"
fi

ASSOC="$(aws ec2 describe-addresses --allocation-ids "$EIP_ALLOC" --query 'Addresses[0].InstanceId' --output text)"
if [[ "$ASSOC" != "$INSTANCE_ID" ]]; then
  aws ec2 associate-address --instance-id "$INSTANCE_ID" --allocation-id "$EIP_ALLOC" --allow-reassociation >/dev/null
  log "associated $EIP -> $INSTANCE_ID"
fi

# --- wait RDS ---
log "waiting for RDS available..."
aws rds wait db-instance-available --db-instance-identifier "$DB_ID"
RDS_ENDPOINT="$(aws rds describe-db-instances --db-instance-identifier "$DB_ID" --query 'DBInstances[0].Endpoint.Address' --output text)"
log "RDS endpoint $RDS_ENDPOINT"

# --- env files ---
ENV_AWS="$OUT/env.aws"
umask 077
cat > "$ENV_AWS" <<EOF
SPRING_PROFILES_ACTIVE=aws
SPRING_DATASOURCE_URL=jdbc:postgresql://${RDS_ENDPOINT}:5432/${DB_NAME}
SPRING_DATASOURCE_USERNAME=${DB_USER}
SPRING_DATASOURCE_PASSWORD=${RDS_PASSWORD}
PILO_JWT_SECRET=${PILO_JWT_SECRET}
PILO_INTERNAL_API_KEY=${PILO_INTERNAL_API_KEY}
PILO_CORS_ORIGINS=http://localhost:3000
PILO_S3_BUCKET=${BUCKET}
AWS_REGION=${AWS_REGION}
PILO_STORAGE_PROVIDER=s3
PILO_PROCESSING_MODE=external
PILO_AI_PROVIDER=mock
PILO_INTEGRATIONS_DEMO_FALLBACK=false
PILO_SEED_ENABLED=true
EOF
chmod 600 "$ENV_AWS"
aws ssm put-parameter --name "$SSM_ENV" --type SecureString --overwrite --value "$(cat "$ENV_AWS")" >/dev/null
log "stored $SSM_ENV (EC2 will pull it and start compose)"

# --- Lambda zip + function ---
log "building worker zip"
if [[ ! -x "$WORKER/scripts/build.sh" ]]; then
  chmod +x "$WORKER/scripts/build.sh"
fi
( cd "$WORKER" && ./scripts/build.sh )
ZIP="$WORKER/dist/pilo-document-processor.zip"
[[ -f "$ZIP" ]] || die "worker zip not built"

LAMBDA_ARN="$(aws lambda get-function --function-name "$LAMBDA_NAME" --query Configuration.FunctionArn --output text 2>/dev/null || true)"
ENV_JSON=$(python3 - "$EIP" "$PILO_INTERNAL_API_KEY" <<'PY'
import json, sys
eip, key = sys.argv[1], sys.argv[2]
print(json.dumps({"Variables": {
    "PILO_BACKEND_URL": f"http://{eip}",
    "PILO_INTERNAL_API_KEY": key,
    "PILO_AI_PROVIDER": "mock",
    "GEMINI_MODEL": "gemini-3.6-flash",
}}))
PY
)
if [[ -z "$LAMBDA_ARN" || "$LAMBDA_ARN" == "None" ]]; then
  log "creating Lambda $LAMBDA_NAME"
  sleep 8
  log "waiting for Lambda to become active..."
  # create-function returns before the function is invokable; S3 notification needs Active.
  sleep 3
  LAMBDA_ARN="$(aws lambda create-function \
    --function-name "$LAMBDA_NAME" \
    --runtime python3.12 \
    --architectures arm64 \
    --handler handler.handler \
    --role "$LAMBDA_ROLE_ARN" \
    --zip-file "fileb://${ZIP}" \
    --timeout 120 \
    --memory-size 512 \
    --environment "$ENV_JSON" \
    --tags "Project=${PROJECT}" \
    --query FunctionArn --output text)"
  aws lambda wait function-active-v2 --function-name "$LAMBDA_NAME"
else
  log "updating Lambda code $LAMBDA_NAME"
  aws lambda update-function-code --function-name "$LAMBDA_NAME" --zip-file "fileb://${ZIP}" >/dev/null
  aws lambda wait function-updated --function-name "$LAMBDA_NAME"
  aws lambda update-function-configuration \
    --function-name "$LAMBDA_NAME" \
    --timeout 120 --memory-size 512 \
    --environment "$ENV_JSON" >/dev/null
fi

aws lambda add-permission \
  --function-name "$LAMBDA_NAME" \
  --statement-id pilo-s3-invoke \
  --action lambda:InvokeFunction \
  --principal s3.amazonaws.com \
  --source-arn "arn:aws:s3:::${BUCKET}" \
  --source-account "$ACCOUNT" >/dev/null 2>&1 || true

aws s3api put-bucket-notification-configuration --bucket "$BUCKET" --notification-configuration "{
  \"LambdaFunctionConfigurations\": [{
    \"Id\": \"pilo-document-processor-on-put\",
    \"LambdaFunctionArn\": \"${LAMBDA_ARN}\",
    \"Events\": [\"s3:ObjectCreated:Put\"],
    \"Filter\": {\"Key\": {\"FilterRules\": [{\"Name\": \"prefix\", \"Value\": \"cases/\"}]}}
  }]
}"
log "S3 PUT prefix cases/ -> $LAMBDA_NAME"

# --- wait EC2 status ---
log "waiting for EC2 status-ok (cloud-init + packages)..."
aws ec2 wait instance-status-ok --instance-ids "$INSTANCE_ID"

# --- outputs ---
OUT_JSON="$OUT/outputs.json"
python3 - "$OUT_JSON" <<PY
import json
json.dump({
  "account": "$ACCOUNT",
  "region": "$AWS_REGION",
  "vpcId": "$VPC_ID",
  "elasticIp": "$EIP",
  "instanceId": "$INSTANCE_ID",
  "rdsEndpoint": "$RDS_ENDPOINT",
  "bucket": "$BUCKET",
  "lambda": "$LAMBDA_NAME",
  "sshUser": "ubuntu",
  "sshKey": "$PEM",
  "sshCidr": "$SSH_CIDR",
  "githubOidcRole": "arn:aws:iam::${ACCOUNT}:role/${OIDC_ROLE}",
  "backendHealth": "http://${EIP}/actuator/health",
  "amplifyRewrite": "http://${EIP}/api/<*>",
}, open("$OUT_JSON", "w"), indent=2)
print("wrote $OUT_JSON")
PY

HANDOFF="$OUT/HANDOFF.md"
cat > "$HANDOFF" <<EOF
# PILO — lo que ya está creado y lo que te toca a vos

Cuenta \`944036658538\`, región \`eu-west-1\`. Secretos en \`$SECRETS\` y \`$ENV_AWS\` (no los subas a Git).

## Ya creado por CLI

| Recurso | Valor |
|---------|--------|
| Elastic IP (API) | \`$EIP\` |
| EC2 | \`$INSTANCE_ID\` (\`t4g.micro\`, Ubuntu 24.04 ARM) |
| RDS | \`$RDS_ENDPOINT\` (db \`pilo\`, user \`pilo_admin\`) |
| S3 | \`s3://$BUCKET\` |
| Lambda | \`$LAMBDA_NAME\` (trigger S3 \`cases/\`) |
| SSH | \`ubuntu@$EIP\` — clave \`$PEM\` |
| GitHub OIDC role | \`arn:aws:iam::${ACCOUNT}:role/${OIDC_ROLE}\` |

El EC2 instala Docker/nginx solo y, cuando aparece el parámetro SSM \`$SSM_ENV\`, hace \`docker compose -f docker-compose.ec2.yml up -d --build\`. La **primera build de Maven en 1 GB RAM tarda 15–25 min**.

Comprobá:

\`\`\`bash
curl -s http://$EIP/actuator/health
# esperado: {"status":"UP"}  (si 000/timeout, esperá el build)
\`\`\`

Logs en la instancia:

\`\`\`bash
ssh -i "$PEM" ubuntu@$EIP
sudo journalctl -u pilo-backend -f
sudo docker compose -f /home/ubuntu/pilo/docker-compose.ec2.yml logs -f backend
\`\`\`

Si SSH da timeout, tu IP cambió. Actualizá el SG \`pilo-ec2\` (puerto 22) a tu IP actual, o usá Session Manager.

Seed demo: \`PILO_SEED_ENABLED=true\` en el primer arranque (\`admin@pilo.test\` / \`Password123!\`). Cuando funcione el login, ponelo en \`false\` (ver abajo).

## Lo que tenés que hacer vos (consola / GitHub)

No se puede automatizar el OAuth Amplify ↔ GitHub ni el push del workflow.

### 1) Amplify (frontend + HTTPS + CD del front)

1. Entrá a https://eu-west-1.console.aws.amazon.com/amplify/home?region=eu-west-1
2. **Create new app** → **GitHub** → autorizá y elegí **\`Nicolas-12000/Pilo\`**.
3. Branch: \`main\`. **Root directory / app root:** \`pilo-frontend\`.
4. Framework: Next.js (SSR / Web Compute).
5. **No** definas \`NEXT_PUBLIC_API_URL\`.
6. Environment variable: \`NEXT_PUBLIC_ENABLE_DEMO_LOGIN=false\`.
7. Hosting → **Rewrites and redirects** → JSON, **primera** regla:

\`\`\`json
[
  {
    "source": "/api/<*>",
    "status": "200",
    "target": "http://$EIP/api/<*>"
  }
]
\`\`\`

8. Cuando tengas la URL \`https://XXXX.amplifyapp.com\`:

\`\`\`bash
./scripts/aws/sync-after-amplify.sh https://XXXX.amplifyapp.com
\`\`\`

### 2) Despliegue continuo backend + Lambda

El workflow ya está en el repo: \`.github/workflows/deploy-aws.yml\`.

1. **Commit y push a \`main\`** de ese workflow (y de \`scripts/aws/\` si querés tenerlos en Git). Sin ese push, Actions no existe.
2. No hace falta access key: GitHub asume el rol \`pilo-github-actions\` por OIDC.
3. Un push a \`main\` que toque \`pilo-backend/**\` rebuild en el EC2 (SSM). Uno que toque \`pilo-worker/**\` sube el zip a Lambda.
4. En GitHub → Actions, la primera corrida puede pedir habilitar workflows si el repo es nuevo.

El EC2 tiene que estar **running**. Si lo apagaste para ahorrar, \`scripts/aws/start-demo.sh\` y después re-run del workflow.

### 3) Apagar / encender para no gastar

Ya tenés budget **My Zero-Spend Budget** (1 USD). Además:

\`\`\`bash
scripts/aws/stop-demo.sh    # para EC2 + RDS
scripts/aws/start-demo.sh   # vuelve a levantar (RDS ~5-8 min)
\`\`\`

### 4) Después del primer login (seguridad demo)

En \`$ENV_AWS\` poné \`PILO_SEED_ENABLED=false\`, subí el parámetro SSM otra vez y \`docker compose ... up -d\` (sin rebuild). Cambiá las contraseñas demo o creá usuarios reales.

### 5) Gemini (opcional)

Hoy Lambda usa \`PILO_AI_PROVIDER=mock\`. Si querés Gemini de verdad, en la consola Lambda → Environment agregá \`GEMINI_API_KEY\` y \`PILO_AI_PROVIDER=gemini\`.

## Smoke test

1. \`curl -s http://$EIP/actuator/health\` → UP
2. Front Amplify carga login (HTTPS)
3. Login → trámite → subir PDF → objeto en \`s3://$BUCKET/cases/...\`
4. CloudWatch de \`$LAMBDA_NAME\` sin error; documento VALIDATED o REJECTED
EOF

log "HANDOFF: $HANDOFF"
VERIFY="$ROOT/scripts/aws/verify-deployment.sh"
if [[ -x "$VERIFY" ]]; then
  log "running verify-deployment.sh"
  "$VERIFY" || log "verify reported issues (backend may still be building)"
fi
log "done. Elastic IP $EIP  RDS $RDS_ENDPOINT  bucket $BUCKET"
log "backend will keep building on EC2; check: curl -s http://$EIP/actuator/health"
