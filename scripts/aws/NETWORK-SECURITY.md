# Red y RDS — alineado con AWS (CLI / consola)

Referencia oficial: [RDS en VPC](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/USER_VPC.WorkingWithRDSInstanceinaVPC.html), [SG de RDS](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Overview.RDSSecurityGroups.html), tutorial [EC2 + PostgreSQL](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_GettingStarted.CreatingConnecting.PostgreSQL.html).

## Lo que hace `provision-demo.sh` (y cumple AWS)

| Requisito AWS | PILO demo |
|---------------|-----------|
| RDS **sin** acceso público (`--no-publicly-accessible`) | Sí — `PubliclyAccessible=false` |
| EC2 y RDS en la **misma VPC** | Sí — VPC por defecto `eu-west-1` |
| SG de RDS: **5432 solo desde SG del EC2** (no IPs sueltas) | Sí — `pilo-rds` ← `pilo-ec2` |
| DB subnet group en **≥2 AZ** | Sí — `pilo-default` en 3 subnets |
| API Spring **no** expuesta en internet (`8080` solo loopback) | Sí — `127.0.0.1:8080` + nginx `:80` |
| Cifrado RDS en reposo | Sí — `storage-encrypted` |
| Admin sin claves en disco en EC2 | Sí — instance profile S3 + SSM |
| Deploy sin abrir SSH | Sí — SSM + rol `AmazonSSMManagedInstanceCore` |

El endpoint `pilo-demo.*.rds.amazonaws.com` desde el EC2 resuelve a **IP privada**; desde tu laptop **no** conecta (esperado).

## Subred “privada” vs demo free-tier

El tutorial de AWS separa **subred pública** (EC2) y **subred privada** (RDS). En la **VPC por defecto** todas las subnets tienen ruta al IGW, pero RDS **sin IP pública** sigue siendo un DB instance privado (solo tráfico dentro de la VPC).

No usamos subnets dedicadas + **NAT Gateway** porque en la guía de coste PILO es explícito: NAT/ALB fuera del free-tier. El EC2 necesita internet para `git clone` y `docker pull`; con NAT pagarías ~USD/mes. Patrón elegido: **EC2 con EIP en subnet por defecto + RDS privado por SG y flag público**.

## Tráfico end-to-end

```
Internet → :80 EC2 (nginx) → 127.0.0.1:8080 Spring
Spring → RDS :5432 (IP privada, SG pilo-ec2 → pilo-rds)
Browser → presigned → S3 → Lambda → http://EIP/internal/... (Lambda fuera de VPC, entra por nginx)
Amplify → rewrite /api → http://EIP/api/...
```

Lambda **no** va en VPC (menos cold start, sin ENI); llama al backend por la EIP en **:80**, igual que Amplify.

## Comprobar desde terminal

```bash
./scripts/aws/verify-deployment.sh
```

## Endurecimiento opcional (después de la demo)

- Quitar regla SSH `:22` y usar solo **Session Manager** (`aws ssm start-session --target i-...`).
- JDBC con TLS: `?sslmode=require` en `SPRING_DATASOURCE_URL` (cert RDS).
- Secrets Manager para password RDS en lugar de SSM flat file.
- VPC custom + subnets privadas reales + NAT **solo** si el presupuesto lo permite.
