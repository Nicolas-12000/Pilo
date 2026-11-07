#!/usr/bin/env bash
# Refresh GitHub Actions OIDC trust for pilo-github-actions (idempotent).
set -euo pipefail

export AWS_PROFILE="${AWS_PROFILE:-NicolasAlejandroGarcia}"
export AWS_DEFAULT_REGION="${AWS_DEFAULT_REGION:-eu-west-1}"
export AWS_REGION="${AWS_REGION:-eu-west-1}"
export AWS_PAGER=""

GITHUB_REPO="${GITHUB_REPO:-Nicolas-12000/Pilo}"
GITHUB_OWNER_ID="${GITHUB_OWNER_ID:-190977331}"
GITHUB_REPO_ID="${GITHUB_REPO_ID:-1344265257}"
ROLE="pilo-github-actions"

ACCOUNT="$(aws sts get-caller-identity --query Account --output text)"
OIDC_ARN="arn:aws:iam::${ACCOUNT}:oidc-provider/token.actions.githubusercontent.com"

if ! aws iam get-open-id-connect-provider --open-id-connect-provider-arn "$OIDC_ARN" >/dev/null 2>&1; then
  echo "Creating GitHub OIDC provider in account $ACCOUNT…"
  aws iam create-open-id-connect-provider \
    --url "https://token.actions.githubusercontent.com" \
    --client-id-list sts.amazonaws.com \
    --thumbprint-list 6938fd4d98bab03faadb97b34396831e3780aea1 1c58a3a8518e8759bf075b76b750d4f2df264fcd
fi

# Minimal trust: aud + sub only (extra StringEquals on repository often breaks newer GitHub tokens).
read -r -d '' TRUST <<EOF || true
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": { "Federated": "${OIDC_ARN}" },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
        },
        "StringLike": {
          "token.actions.githubusercontent.com:sub": [
            "repo:${GITHUB_REPO}:*",
            "repo:Nicolas-12000@${GITHUB_OWNER_ID}/Pilo@${GITHUB_REPO_ID}:*"
          ]
        }
      }
    }
  ]
}
EOF

if aws iam get-role --role-name "$ROLE" >/dev/null 2>&1; then
  aws iam update-assume-role-policy --role-name "$ROLE" --policy-document "$TRUST"
  echo "Updated assume-role policy on ${ROLE} for repo ${GITHUB_REPO} (account ${ACCOUNT})."
else
  echo "Role ${ROLE} not found. Run scripts/aws/provision-demo.sh first." >&2
  exit 1
fi
