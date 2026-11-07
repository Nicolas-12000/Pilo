# PILO demo — Terraform (eu-west-1)

Infra equivalente a `scripts/aws/provision-demo.sh`: VPC por defecto, SG, RDS, S3, EC2+EIP, Lambda, IAM (EC2 + Lambda + GitHub OIDC), SSM `/pilo/demo/env-aws`.

## ¿Cuándo usarlo?

| Camino | Cuándo |
|--------|--------|
| **`scripts/aws/provision-demo.sh`** | **Primera vez hoy** — ya probado con tu cuenta |
| **Terraform** | TFG / repetir entorno / evolucionar infra con PRs |

**No** corras `terraform apply` en vacío si ya creaste todo con el script CLI (nombres chocan). Opciones:

1. **Seguir con el script** (recomendado ahora) y usá Terraform como documentación en la memoria.
2. **Migrar:** `terraform import` recurso a recurso (ver lista abajo) y después `plan` hasta cero drift.
3. **Reset:** destruir recursos manualmente y un solo `terraform apply`.

## Apply desde cero (solo si no usaste el script)

```bash
cd pilo-worker && ./scripts/build.sh && cd -
cd infra/terraform/pilo-demo
cp terraform.tfvars.example terraform.tfvars   # completá secretos
terraform init
terraform apply
```

La clave SSH queda en `out/pilo-ec2.pem` (gitignored vía `infra/terraform/**`).

## Import rápido (si ya corriste `provision-demo.sh`)

Con `scripts/aws/out/outputs.json` y la consola/CLI a mano:

```bash
export AWS_PROFILE=NicolasAlejandroGarcia
cd infra/terraform/pilo-demo
terraform init
# Ejemplos (sustituí IDs reales):
# terraform import aws_security_group.ec2 sg-xxx
# terraform import aws_db_instance.main pilo-demo
# terraform import aws_s3_bucket.docs pilo-docs-944036658538
# terraform import aws_instance.api i-xxx
# terraform import aws_eip.api eipalloc-xxx
# terraform import aws_lambda_function.worker pilo-document-processor-dev
```

GitHub OIDC y roles IAM también se pueden importar; el README del TFG puede enlazar este archivo.

## Outputs

Tras `apply`: `terraform output elastic_ip`, `health_url`, `github_actions_role_arn`, `amplify_rewrite_target`.

Amplify y el rewrite JSON **no** están en Terraform (OAuth GitHub en navegador). Ver `scripts/aws/README.md`.
