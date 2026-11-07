#!/usr/bin/env bash
set -euo pipefail

export AWS_PROFILE="${AWS_PROFILE:-NicolasAlejandroGarcia}"
export AWS_REGION="${AWS_REGION:-eu-west-1}"
export AWS_DEFAULT_REGION="$AWS_REGION"

DB_ID="${PILO_DB_ID:-pilo-demo}"
INSTANCE_NAME="${PILO_EC2_NAME:-pilo-api}"

INSTANCE_ID="$(aws ec2 describe-instances \
  --filters "Name=tag:Name,Values=$INSTANCE_NAME" \
    "Name=instance-state-name,Values=pending,running,stopping,stopped" \
  --query 'Reservations[0].Instances[0].InstanceId' --output text)"

if [[ -n "$INSTANCE_ID" && "$INSTANCE_ID" != "None" ]]; then
  STATE="$(aws ec2 describe-instances --instance-ids "$INSTANCE_ID" --query 'Reservations[0].Instances[0].State.Name' --output text)"
  if [[ "$STATE" == "running" || "$STATE" == "pending" ]]; then
    echo "Stopping EC2 $INSTANCE_ID..."
    aws ec2 stop-instances --instance-ids "$INSTANCE_ID" >/dev/null
    aws ec2 wait instance-stopped --instance-ids "$INSTANCE_ID"
  else
    echo "EC2 already $STATE"
  fi
fi

if aws rds describe-db-instances --db-instance-identifier "$DB_ID" >/dev/null 2>&1; then
  RDS_STATE="$(aws rds describe-db-instances --db-instance-identifier "$DB_ID" --query 'DBInstances[0].DBInstanceStatus' --output text)"
  if [[ "$RDS_STATE" == "available" ]]; then
    echo "Stopping RDS $DB_ID..."
    aws rds stop-db-instance --db-instance-identifier "$DB_ID" >/dev/null
    echo "RDS stop requested (tarda unos minutos)."
  else
    echo "RDS status: $RDS_STATE"
  fi
else
  echo "No RDS $DB_ID"
fi

echo "Done. Para volver: scripts/aws/start-demo.sh"
