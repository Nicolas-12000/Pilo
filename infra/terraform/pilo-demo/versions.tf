terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    tls = {
      source  = "hashicorp/tls"
      version = "~> 4.0"
    }
    local = {
      source  = "hashicorp/local"
      version = "~> 2.0"
    }
  }

  # Descomentá cuando tengas bucket + tabla para state remoto (producción / equipo).
  # backend "s3" {
  #   bucket         = "pilo-terraform-state-ACCOUNT"
  #   key            = "demo/terraform.tfstate"
  #   region         = "eu-west-1"
  #   dynamodb_table = "pilo-terraform-lock"
  #   encrypt        = true
  # }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project = "pilo"
      Env     = "demo"
      IaC     = "terraform"
    }
  }
}
