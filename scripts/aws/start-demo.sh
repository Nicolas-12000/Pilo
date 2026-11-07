#!/usr/bin/env bash
set -euo pipefail

export AWS_PROFILE="${AWS_PROFILE:-NicolasAlejandroGarcia}"
export AWS_REGION="${AWS_REGION:-eu-west-1}"
export AWS_DEFAULT_REGION="$AWS_REGION"

DB_ID="${PILO_DB_ID:-pilo-demo}"
INSTANCE_NAME="${PILO_EC2_NAME:-pilo-api}"

INSTANCE_ID="$(aws ec2 describe-instances \
  --filters "Name=tag:Name,Values=$INSTANCE_NAME" \
    "Name=instance-state-name,Values=stopped,pending,running" \
  --query 'Reservations[0].Instances[0].InstanceId' --output text)"

if [[ -z "$INSTANCE_ID" || "$INSTANCE_ID" == "None" ]]; then
  echo "No EC2 tagged Name=$INSTANCE_NAME. Run scripts/aws/provision-demo.sh first." >&2
  exit 1
fi

STATE="$(aws ec2 describe-instances --instance-ids "$INSTANCE_ID" --query 'Reservations[0].Instances[0].State.Name' --output text)"
RDS_STATE="$(aws rds describe-db-instances --db-instance-identifier "$DB_ID" --query 'DBInstances[0].DBInstanceStatus' --output text 2>/dev/null || echo missing)"

echo "RDS $DB_ID: $RDS_STATE"
if [[ "$RDS_STATE" == "stopped" ]]; then
  echo "Starting RDS (5–10 min)..."
  aws rds start-db-instance --db-instance-identifier "$DB_ID" >/dev/null
  aws rds wait db-instance-available --db-instance-identifier "$DB_ID"
fi

if [[ "$STATE" == "stopped" ]]; then
  echo "Starting EC2 $INSTANCE_ID..."
  aws ec2 start-instances --instance-ids "$INSTANCE_ID" >/dev/null
fi

aws ec2 wait instance-status-ok --instance-ids "$INSTANCE_ID"
EIP="$(aws ec2 describe-addresses --filters "Name=tag:Name,Values=$INSTANCE_NAME" --query 'Addresses[0].PublicIp' --output text)"
echo "EC2 ready. Elastic IP: $EIP"
echo "Health: curl -s http://$EIP/actuator/health"
