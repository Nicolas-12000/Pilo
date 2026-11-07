data "aws_caller_identity" "current" {}

data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
  filter {
    name   = "default-for-az"
    values = ["true"]
  }
}

data "aws_ami" "ubuntu_arm" {
  most_recent = true
  owners      = ["099720109477"]

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd-gp3/ubuntu-noble-24.04-arm64-server-*"]
  }
}

locals {
  account_id   = data.aws_caller_identity.current.account_id
  bucket_name  = var.s3_bucket_name != "" ? var.s3_bucket_name : "pilo-docs-${local.account_id}"
  db_id        = "pilo-demo"
  ec2_name     = "pilo-api"
  lambda_name  = "pilo-document-processor-dev"
  key_name     = "pilo-ec2"
  ssm_env_name = "/pilo/demo/env-aws"

  env_aws_content = <<-EOT
    SPRING_PROFILES_ACTIVE=aws
    SPRING_DATASOURCE_URL=jdbc:postgresql://${aws_db_instance.main.address}:5432/pilo
    SPRING_DATASOURCE_USERNAME=pilo_admin
    SPRING_DATASOURCE_PASSWORD=${var.db_password}
    PILO_JWT_SECRET=${var.pilo_jwt_secret}
    PILO_INTERNAL_API_KEY=${var.pilo_internal_api_key}
    PILO_CORS_ORIGINS=${var.pilo_cors_origins}
    PILO_S3_BUCKET=${local.bucket_name}
    AWS_REGION=${var.aws_region}
    PILO_STORAGE_PROVIDER=s3
    PILO_PROCESSING_MODE=external
    PILO_AI_PROVIDER=mock
    PILO_INTEGRATIONS_DEMO_FALLBACK=false
    PILO_SEED_ENABLED=${var.pilo_seed_enabled}
  EOT
}
