variable "aws_region" {
  type    = string
  default = "eu-west-1"
}

variable "project" {
  type    = string
  default = "pilo"
}

variable "ssh_cidr" {
  description = "Tu IP pública /32 para SSH al EC2"
  type        = string
}

variable "github_repo" {
  description = "repo/subject para OIDC (ej. Nicolas-12000/Pilo)"
  type        = string
  default     = "Nicolas-12000/Pilo"
}

variable "s3_bucket_name" {
  description = "Nombre global del bucket documentos (vacío = pilo-docs-ACCOUNT_ID)"
  type        = string
  default     = ""
}

variable "db_password" {
  description = "Password maestro RDS"
  type        = string
  sensitive   = true
}

variable "pilo_jwt_secret" {
  type      = string
  sensitive = true
}

variable "pilo_internal_api_key" {
  type      = string
  sensitive = true
}

variable "pilo_cors_origins" {
  description = "Orígenes CORS (añadí la URL Amplify cuando la tengas)"
  type        = string
  default     = "http://localhost:3000"
}

variable "lambda_zip_path" {
  description = "Zip del worker (corré pilo-worker/scripts/build.sh antes del apply)"
  type        = string
  default     = "../../../pilo-worker/dist/pilo-document-processor.zip"
}

variable "ec2_user_data_path" {
  type    = string
  default = "../../../scripts/aws/ec2-user-data.sh"
}

variable "pilo_seed_enabled" {
  description = "true solo el primer arranque demo"
  type        = bool
  default     = true
}
