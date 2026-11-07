# PILO en AWS — automatizado + tu parte manual

**Región:** `eu-west-1` · **Perfil CLI:** `NicolasAlejandroGarcia` (o `export AWS_PROFILE=...`)

## Red / RDS “privado” (documentación AWS)

Todo el stack se crea por CLI (`provision-demo.sh`). RDS va con `--no-publicly-accessible` y el SG `pilo-rds` solo acepta **5432 desde el SG del EC2** (patrón que recomienda AWS, sin IP suelta). Detalle: `scripts/aws/NETWORK-SECURITY.md`.

Comprobar:

```bash
./scripts/aws/verify-deployment.sh
```

## 1. Crear toda la infra (una vez)

Desde la raíz del monorepo:

```bash
chmod +x scripts/aws/*.sh pilo-worker/scripts/build.sh
./scripts/aws/provision-demo.sh
```

Duración típica: **12–20 min** (RDS + primera build Docker en el EC2).

Salida local (gitignored):

| Archivo | Qué es |
|---------|--------|
| `scripts/aws/out/secrets.env` | JWT, internal key, password RDS |
| `scripts/aws/out/env.aws` | Plantilla para el backend en EC2 |
| `scripts/aws/out/pilo-ec2.pem` | SSH |
| `scripts/aws/out/outputs.json` | IPs, endpoints, IDs |
| `scripts/aws/out/HANDOFF.md` | Resumen generado en cada run |

Comprobación:

```bash
EIP=$(jq -r .elasticIp scripts/aws/out/outputs.json)
curl -s "http://$EIP/actuator/health"
```

Si `000` o timeout: el EC2 sigue en `docker build` (normal 15–25 min en t4g.micro). SSH o Session Manager:

```bash
ssh -i scripts/aws/out/pilo-ec2.pem ubuntu@$EIP
sudo journalctl -u pilo-backend -f
```

## 2. Lo que **solo vos** tenés que hacer

### A) Push a GitHub (CD backend + Lambda)

En el repo hay:

- `.github/workflows/deploy-aws.yml` — OIDC a `arn:aws:iam::944036658538:role/pilo-github-actions`
- `pilo-frontend/amplify.yml` — build Amplify

**Commiteá y pusheá a `main`** esos archivos (y `scripts/aws/` si querés el runbook en GitHub). Sin push, Actions no corre.

Luego en GitHub → **Actions**: habilitá workflows si te lo pide.

| Push modifica… | Qué despliega |
|----------------|---------------|
| `pilo-backend/**` | SSM → `pilo-update.sh` en EC2 (rebuild compose) |
| `pilo-worker/**` | Zip → Lambda `pilo-document-processor-dev` |

EC2 tiene que estar **running**. Si apagaste: `./scripts/aws/start-demo.sh`.

### B) Amplify (frontend HTTPS) — consola, ~10 min

1. [Amplify eu-west-1](https://eu-west-1.console.aws.amazon.com/amplify/home?region=eu-west-1) → **Create app** → **GitHub**.
2. Repo **`Nicolas-12000/Pilo`**, rama `main`.
3. **Root directory:** `pilo-frontend` · Framework Next.js SSR.
4. Variable: `NEXT_PUBLIC_ENABLE_DEMO_LOGIN=false`.
5. **No** pongas `NEXT_PUBLIC_API_URL`.

**Rewrites** (Hosting → Rewrites, **primera** regla; usá la IP de `outputs.json`):

```json
[
  {
    "source": "/api/<*>",
    "status": "200",
    "target": "http://TU_ELASTIC_IP/api/<*>"
  }
]
```

6. Cuando exista `https://XXXX.amplifyapp.com`:

```bash
./scripts/aws/sync-after-amplify.sh https://XXXX.amplifyapp.com
```

Eso actualiza CORS en S3, SSM y reinicia el backend en EC2.

### C) Primer login demo → apagar seed

Cuentas seed: `admin@pilo.test` / `Password123!` (solo academia).

En `scripts/aws/out/env.aws` poné `PILO_SEED_ENABLED=false`, subí SSM y reiniciá compose (o volvé a correr `sync-after-amplify` después de editar el archivo).

### D) Presupuesto

Ya tenés **My Zero-Spend Budget**. Opcional: alerta 5–10 USD en Billing.

### E) Ahorrar cuando no demostrás

```bash
./scripts/aws/stop-demo.sh   # para EC2 + RDS
./scripts/aws/start-demo.sh  # antes de demo o antes de un deploy Actions
```

---

## 3. Terraform (opcional, memoria / evolución)

Código en `infra/terraform/pilo-demo/`. **No tenés que instalar Terraform** para desplegar: `provision-demo.sh` + GitHub Actions alcanzan.

Sirve para el TFG (IaC documentado) o si más adelante querés `terraform apply` en otro entorno. Entonces:

```bash
sudo apt install terraform   # o tfenv / binario de hashicorp
```

Ver `infra/terraform/pilo-demo/README.md`. Si ya corriste el script CLI, **no** hagas `apply` en la misma cuenta sin `import` (nombres duplicados).

---

## 4. Troubleshooting rápido

| Problema | Acción |
|----------|--------|
| SSH timeout | Tu IP cambió — en consola EC2 → SG `pilo-ec2` → SSH desde tu IP actual, o Session Manager |
| Health no UP | Ver `journalctl -u pilo-backend` y logs Docker |
| Login CORS | Corré `sync-after-amplify.sh` con la URL exacta (https, sin `/` final) |
| Lambda no dispara | Bucket notification + CloudWatch Logs de `pilo-document-processor-dev` |
| Actions falla “no EC2” | `start-demo.sh` |

Guía larga (local): `docs/aws-operations.md`.
