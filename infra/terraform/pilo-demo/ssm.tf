resource "aws_ssm_parameter" "env_aws" {
  name  = local.ssm_env_name
  type  = "SecureString"
  value = local.env_aws_content
}
