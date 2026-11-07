#!/usr/bin/env bash
# Re-ejecuta ec2-user-data.sh en la instancia vía SSM (si el primer boot falló).
set -euo pipefail

export AWS_PROFILE="${AWS_PROFILE:-NicolasAlejandroGarcia}"
export AWS_REGION="${AWS_REGION:-eu-west-1}"
export AWS_DEFAULT_REGION="$AWS_REGION"

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
UD="$ROOT/scripts/aws/ec2-user-data.sh"

INSTANCE_ID="${1:-$(aws ec2 describe-instances \
  --filters Name=tag:Name,Values=pilo-api Name=instance-state-name,Values=running \
  --query 'Reservations[0].Instances[0].InstanceId' --output text)}"
[[ -n "$INSTANCE_ID" && "$INSTANCE_ID" != "None" ]] || { echo "no running pilo-api"; exit 1; }

B64="$(base64 -w0 "$UD")"
PARAMS="$(python3 -c 'import json,sys; print(json.dumps({"commands":[sys.stdin.read()]}))' <<EOF
echo '$B64' | base64 -d | bash
EOF
)"

CMD_ID="$(aws ssm send-command \
  --instance-ids "$INSTANCE_ID" \
  --document-name AWS-RunShellScript \
  --timeout-seconds 3600 \
  --comment "pilo repair bootstrap" \
  --parameters "$PARAMS" \
  --query Command.CommandId --output text)"

echo "SSM command $CMD_ID on $INSTANCE_ID (puede tardar 20-40 min: apt, clone, docker build)"
echo "aws ssm get-command-invocation --command-id $CMD_ID --instance-id $INSTANCE_ID"
