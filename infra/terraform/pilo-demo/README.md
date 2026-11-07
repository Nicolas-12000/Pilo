# Terraform (optional)

Same stack as `scripts/aws/provision-demo.sh` (RDS, EC2, S3, Lambda, OIDC). **Use the shell script for the live demo** unless you plan a clean `terraform apply` in a new account.

If resources already exist from the CLI script, do not `apply` blindly — import or document as IaC reference only.
