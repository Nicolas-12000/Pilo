#!/usr/bin/env bash
# Comprueba que el despliegue sigue el patrón AWS: RDS sin IP pública, 5432 solo desde SG EC2, mismo VPC.
set -euo pipefail

export AWS_PROFILE="${AWS_PROFILE:-NicolasAlejandroGarcia}"
export AWS_REGION="${AWS_REGION:-eu-west-1}"
export AWS_DEFAULT_REGION="$AWS_REGION"

DB_ID="${PILO_DB_ID:-pilo-demo}"
EC2_NAME="${PILO_EC2_NAME:-pilo-api}"
FAIL=0

ok() { printf '  OK  %s\n' "$*"; }
bad() { printf ' FAIL %s\n' "$*"; FAIL=1; }

echo "=== PILO AWS verify ($AWS_REGION) ==="

INSTANCE_ID="$(aws ec2 describe-instances \
  --filters "Name=tag:Name,Values=$EC2_NAME" "Name=instance-state-name,Values=running" \
  --query 'Reservations[0].Instances[0].InstanceId' --output text)"
[[ -n "$INSTANCE_ID" && "$INSTANCE_ID" != "None" ]] || bad "EC2 $EC2_NAME not running"
[[ "$FAIL" -eq 0 ]] && ok "EC2 $INSTANCE_ID running"

read -r EC2_VPC EC2_SG IMDS < <(
  aws ec2 describe-instances --instance-ids "$INSTANCE_ID" \
    --query 'Reservations[0].Instances[0].[VpcId,SecurityGroups[0].GroupId,MetadataOptions.HttpTokens]' --output text
)
[[ "$IMDS" == "required" ]] && ok "IMDSv2 required" || bad "IMDSv2 not required"

PUBLIC="$(aws rds describe-db-instances --db-instance-identifier "$DB_ID" \
  --query 'DBInstances[0].PubliclyAccessible' --output text)"
RDS_VPC="$(aws rds describe-db-instances --db-instance-identifier "$DB_ID" \
  --query 'DBInstances[0].DBSubnetGroup.VpcId' --output text)"
RDS_SG="$(aws rds describe-db-instances --db-instance-identifier "$DB_ID" \
  --query 'DBInstances[0].VpcSecurityGroups[0].VpcSecurityGroupId' --output text)"
RDS_EP="$(aws rds describe-db-instances --db-instance-identifier "$DB_ID" \
  --query 'DBInstances[0].Endpoint.Address' --output text)"

[[ "$PUBLIC" == "False" || "$PUBLIC" == "false" ]] && ok "RDS PubliclyAccessible=false" || bad "RDS is publicly accessible ($PUBLIC)"
[[ "$EC2_VPC" == "$RDS_VPC" ]] && ok "EC2 and RDS same VPC $EC2_VPC" || bad "EC2/RDS VPC mismatch"

SG_REF="$(aws ec2 describe-security-groups --group-ids "$RDS_SG" \
  --query 'SecurityGroups[0].IpPermissions[?FromPort==`5432`].UserIdGroupPairs[0].GroupId' --output text)"
IP_RANGES="$(aws ec2 describe-security-groups --group-ids "$RDS_SG" \
  --query 'SecurityGroups[0].IpPermissions[?FromPort==`5432`].IpRanges[0].CidrIp' --output text)"
if [[ "$SG_REF" == "$EC2_SG" && ( -z "$IP_RANGES" || "$IP_RANGES" == "None" ) ]]; then
  ok "RDS :5432 only from EC2 security group"
else
  bad "RDS :5432 lock (ec2_sg=$SG_REF expected $EC2_SG, cidr=$IP_RANGES)"
fi

OPEN8080="$(aws ec2 describe-security-groups --group-ids "$EC2_SG" \
  --query 'length(SecurityGroups[0].IpPermissions[?FromPort==`8080` || ToPort==`8080`])' --output text)"
[[ "${OPEN8080:-0}" == "0" ]] && ok "port 8080 not on EC2 SG" || bad "8080 exposed on EC2 SG"

EIP="$(aws ec2 describe-addresses --filters "Name=tag:Name,Values=$EC2_NAME" --query 'Addresses[0].PublicIp' --output text)"
[[ -n "$EIP" && "$EIP" != "None" ]] && ok "Elastic IP $EIP" || bad "missing Elastic IP"

LAMBDA_URL="$(aws lambda get-function-configuration --function-name pilo-document-processor-dev \
  --query 'Environment.Variables.PILO_BACKEND_URL' --output text 2>/dev/null || true)"
[[ "$LAMBDA_URL" == "http://${EIP}" ]] && ok "Lambda -> http://$EIP" || bad "Lambda PILO_BACKEND_URL=$LAMBDA_URL"

ACCOUNT="$(aws sts get-caller-identity --query Account --output text)"
NOTIF="$(aws s3api get-bucket-notification-configuration --bucket "pilo-docs-${ACCOUNT}" \
  --query 'LambdaFunctionConfigurations[0].Filter.Key.FilterRules[0].Value' --output text 2>/dev/null || true)"
[[ "$NOTIF" == "cases/" ]] && ok "S3 trigger prefix cases/" || bad "S3 Lambda notification ($NOTIF)"

SSM="$(aws ssm describe-instance-information --filters "Key=InstanceIds,Values=$INSTANCE_ID" \
  --query 'InstanceInformationList[0].PingStatus' --output text)"
[[ "$SSM" == "Online" ]] && ok "SSM Online" || bad "SSM $SSM"

echo
echo "RDS (private DNS from EC2): $RDS_EP"
echo "Health: curl -s http://$EIP/actuator/health"
echo "Doc: scripts/aws/NETWORK-SECURITY.md"
echo
exit "$FAIL"
