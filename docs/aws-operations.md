# PILO — Operaciones externas (AWS, RDS, Terraform, CD)

Guía paso a paso de lo que **configurás vos** en AWS y Terraform.  
El código del repo ya expone contratos, variables y perfiles para conectar esas piezas.

---

## Mapa de responsabilidades

| Componente | Lo hace el código (repo) | Lo hacés vos (AWS / Terraform / GitHub) |
|------------|--------------------------|----------------------------------------|
| Worker Lambda | `pilo-worker/` (Python) | Crear función, trigger S3, IAM, env vars |
| Storage S3 | `S3ObjectStorage`, presigned PUT | Bucket, CORS, evento `ObjectCreated` |
| Backend API | Spring Boot + perfil `aws` | RDS, despliegue (ECS/EC2/Render/etc.) |
| Secretos | Lee env vars / Parameter Store | Crear parámetros y secrets en AWS/GitHub |
| Terraform | Outputs esperados documentados abajo | Módulos, state, `apply` |
| CD | Workflow de ejemplo en §8 | Secrets en GitHub Actions |

---

## Variables que el código espera

### Backend (`pilo-backend`)

| Variable | Ejemplo | Descripción |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | `aws` | Activa S3 + procesamiento externo |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://pilo.xxxx.eu-west-1.rds.amazonaws.com:5432/pilo` | RDS |
| `SPRING_DATASOURCE_USERNAME` | `pilo_admin` | Usuario DB |
| `SPRING_DATASOURCE_PASSWORD` | *(secreto)* | Password DB |
| `PILO_JWT_SECRET` | *(32+ bytes random)* | JWT producción |
| `PILO_INTERNAL_API_KEY` | *(32+ bytes random)* | Worker → backend |
| `PILO_CORS_ORIGINS` | `https://app.tudominio.com` | Origen frontend |
| `PILO_STORAGE_PROVIDER` | `s3` | `local` en dev, `s3` en prod |
| `PILO_S3_BUCKET` | `pilo-docs-prod` | Bucket documentos |
| `AWS_REGION` | `eu-west-1` | Región del bucket/RDS |
| `PILO_PROCESSING_MODE` | `external` | Lambda procesa; Spring no llama IA inline |
| `GEMINI_API_KEY` | *(secreto)* | Solo si worker/backend usan Gemini |
| `PILO_AI_PROVIDER` | `gemini` | `gemini` \| `mock` (worker Python) |

Perfil local (sin AWS):

```bash
SPRING_PROFILES_ACTIVE=local
PILO_STORAGE_PROVIDER=local
PILO_PROCESSING_MODE=inline
PILO_STORAGE_PATH=./storage
```

### Worker Lambda (`pilo-worker`)

| Variable | Descripción |
|----------|-------------|
| `PILO_BACKEND_URL` | URL pública del backend, ej. `https://api.tudominio.com` |
| `PILO_INTERNAL_API_KEY` | Mismo valor que el backend |
| `PILO_AI_PROVIDER` | `gemini` o `mock` |
| `GEMINI_API_KEY` | API key Google AI Studio |
| `GEMINI_MODEL` | `gemini-2.0-flash` (default) |
| `PILO_S3_BUCKET` | Opcional; se infiere del evento S3 |

---

## Flujo de documentos en producción

```
Frontend  →  POST /presigned-url  →  Backend crea Document (PENDING_UPLOAD)
Frontend  →  PUT S3 (presigned)   →  Objeto en bucket
S3 event  →  Lambda worker        →  POST /internal/documents/{id}/upload-completed
Lambda    →  LLM extraction       →  POST /internal/documents/{id}/extractions
Backend   →  validación + workflow
```

Formato de clave S3 (obligatorio):

```
cases/{caseId}/documents/{documentId}/{fileName}
```

El worker parsea `documentId` desde la clave.

---

## Paso 1 — Cuenta AWS y billing

1. [AWS Console](https://console.aws.amazon.com/)
2. **Billing → Budgets** → alerta a **10 USD**
3. Elegí **una región** y no la cambies: recomendado `eu-west-1`

```bash
aws configure --profile pilo
aws sts get-caller-identity --profile pilo
```

---

## Paso 2 — IAM

### Usuario `pilo-dev` (Terraform / deploy manual)

- Permiso inicial: `PowerUserAccess` (acotar después)
- Guardar `AWS_ACCESS_KEY_ID` y `AWS_SECRET_ACCESS_KEY` en gestor de contraseñas

### Rol Lambda `pilo-document-processor-role`

Trust policy: `lambda.amazonaws.com`

Policies mínimas:

- `AWSLambdaBasicExecutionRole` (CloudWatch Logs)
- Inline S3 read:

```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Action": ["s3:GetObject"],
    "Resource": "arn:aws:s3:::TU-BUCKET/cases/*"
  }]
}
```

### Rol backend (si corre en ECS/EC2)

- `s3:PutObject`, `s3:GetObject` en `arn:aws:s3:::TU-BUCKET/cases/*`
- Acceso a RDS (security group, no IAM DB auth en MVP)

---

## Paso 3 — S3

1. **Create bucket** → `pilo-docs-<env>` (nombre único global)
2. Block all public access: **ON**
3. Encryption: SSE-S3
4. **CORS** (consola → bucket → Permissions):

```json
[
  {
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["PUT", "GET", "HEAD"],
    "AllowedOrigins": [
      "http://localhost:3000",
      "https://TU-FRONTEND.com"
    ],
    "ExposeHeaders": ["ETag"],
    "MaxAgeSeconds": 3000
  }
]
```

5. Anotar `PILO_S3_BUCKET` y `AWS_REGION`

---

## Paso 4 — RDS PostgreSQL

1. **RDS → Create database**
2. Engine: PostgreSQL **16**
3. Template: Free tier (si aplica)
4. Instance: `db.t4g.micro`
5. DB name: `pilo`
6. Master: `pilo_admin` + password fuerte
7. **Public access: No** (ideal)
8. Security group: permitir **5432** solo desde el SG del backend

Anotar endpoint → `SPRING_DATASOURCE_URL`

Flyway corre al arrancar el backend; no hace falta migrar a mano.

**Coste:** apagar o borrar la instancia cuando no demos.

---

## Paso 5 — Lambda document processor

### 5.1 Empaquetar el worker

En tu máquina (desde el repo):

```bash
cd pilo-worker
./scripts/build.sh
# Genera: dist/pilo-document-processor.zip
```

### 5.2 Crear la función

| Campo | Valor |
|-------|-------|
| Name | `pilo-document-processor-dev` |
| Runtime | Python 3.12 |
| Architecture | arm64 |
| Handler | `handler.handler` |
| Timeout | 120 s |
| Memory | 512 MB |
| Role | `pilo-document-processor-role` |

Environment variables: ver tabla worker arriba.

**Importante:** `PILO_BACKEND_URL` debe ser alcanzable desde Lambda (API pública o ALB).

### 5.3 Trigger S3

1. Lambda → Add trigger → S3
2. Bucket: tu bucket
3. Event: `PUT`
4. Prefix: `cases/`
5. Suffix: *(vacío)*

### 5.4 Probar manualmente

Subí un objeto de prueba con clave válida y mirá CloudWatch Logs de la Lambda.

También podés probar el callback sin Lambda:

```bash
curl -X POST "$BACKEND/internal/documents/{documentId}/upload-completed" \
  -H "X-Internal-Api-Key: $PILO_INTERNAL_API_KEY"

curl -X POST "$BACKEND/internal/documents/{documentId}/extractions" \
  -H "Content-Type: application/json" \
  -H "X-Internal-Api-Key: $PILO_INTERNAL_API_KEY" \
  -d '{
    "documentType": "rental_contract",
    "confidence": 0.95,
    "extractedFields": {
      "personName": "Ana Usuario",
      "expirationDate": "2027-12-31"
    }
  }'
```

---

## Paso 6 — Desplegar backend

Opciones MVP (elegí una):

| Opción | Pros | Contras |
|--------|------|---------|
| **EC2 t4g.micro** | Control total, barato | Mantenimiento manual |
| **ECS Fargate** | Contenedor, escalable | Más Terraform |
| **Railway / Render** | Rápido | Menos “AWS puro” en demo |

### Build JAR local

```bash
cd pilo-backend
./mvnw -B package -DskipTests
java -jar target/pilo-backend-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=aws
```

### Dockerfile (referencia)

El repo incluye `pilo-backend/Dockerfile`. Build:

```bash
docker build -t pilo-backend ./pilo-backend
docker run -p 8080:8080 --env-file pilo-backend/.env.aws pilo-backend
```

Checklist backend en prod:

- [ ] Health: `GET /actuator/health`
- [ ] Flyway migraciones OK (logs al arranque)
- [ ] Presigned URL devuelve `method: PUT` y URL de S3
- [ ] CORS incluye tu frontend

---

## Paso 7 — Frontend

Desplegar en Vercel / S3+CloudFront / similar.

```bash
NEXT_PUBLIC_API_URL=https://api.tudominio.com
```

El frontend detecta `method: PUT` en presigned URL y sube directo a S3.

---

## Paso 8 — Terraform (vos)

Estructura sugerida:

```
infra/
├── environments/
│   ├── dev/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── terraform.tfvars
│   └── prod/
├── modules/
│   ├── s3/
│   ├── rds/
│   ├── lambda/
│   ├── iam/
│   └── networking/
└── README.md
```

### Outputs que conviene exportar

```hcl
output "s3_bucket_name" { value = module.s3.bucket_name }
output "rds_endpoint" { value = module.rds.endpoint }
output "lambda_function_name" { value = module.lambda.function_name }
output "backend_security_group_id" { value = module.networking.backend_sg_id }
```

### State

- Backend remoto: S3 + DynamoDB lock
- Nunca commitear `.tfstate` con secretos

### Orden de apply recomendado

1. VPC / SG (si aplica)
2. RDS
3. S3
4. IAM roles
5. Lambda + S3 trigger
6. Backend (ECS/EC2)
7. (Opcional) EventBridge deadline worker — fase posterior

---

## Paso 9 — GitHub Actions CD (vos)

Secrets en **Settings → Secrets and variables → Actions**:

| Secret | Uso |
|--------|-----|
| `AWS_ACCESS_KEY_ID` | Terraform / deploy |
| `AWS_SECRET_ACCESS_KEY` | Terraform / deploy |
| `AWS_REGION` | eu-west-1 |
| `PILO_S3_BUCKET` | Deploy / smoke tests |
| `PILO_INTERNAL_API_KEY` | Backend + Lambda |
| `PILO_JWT_SECRET` | Backend prod |
| `GEMINI_API_KEY` | Lambda |
| `TF_STATE_BUCKET` | Terraform backend |

Workflow sugerido (`.github/workflows/deploy.yml` — lo creás vos):

```yaml
name: deploy
on:
  push:
    branches: [main]
jobs:
  terraform:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: hashicorp/setup-terraform@v3
      - run: terraform init && terraform plan
        working-directory: infra/environments/dev
      # apply con environment protection / approval manual
  backend:
    needs: terraform
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: cd pilo-backend && ./mvnw -B package -DskipTests
      - run: docker build -t pilo-backend ./pilo-backend
      # push a ECR y deploy ECS — según tu módulo Terraform
  worker:
    needs: terraform
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: cd pilo-worker && ./scripts/build.sh
      - run: aws lambda update-function-code --function-name pilo-document-processor-dev --zip-file fileb://pilo-worker/dist/pilo-document-processor.zip
```

Protegé `terraform apply` con **GitHub Environment** + aprobación manual.

---

## Paso 10 — Deadline worker (fase posterior)

No bloquea la demo principal. Cuando toque:

1. Lambda `pilo-deadline-worker`
2. EventBridge rule: `rate(1 hour)`
3. Llama endpoint interno (por implementar) o consulta API admin

En local ya existe `@Scheduled` como alternativa.

---

## Checklist demo final

- [ ] Login usuario → crear expediente
- [ ] Subir PDF → aparece en S3 con clave `cases/.../documents/...`
- [ ] CloudWatch Lambda: extracción OK
- [ ] Backend: documento `VALIDATED` o `REJECTED`
- [ ] Workflow avanza
- [ ] Audit trail visible
- [ ] Integración BOE en UI

---

## Troubleshooting

| Síntoma | Causa probable |
|---------|----------------|
| Presigned URL 403 en browser | CORS del bucket o `Content-Type` no coincide |
| Lambda timeout | Subir memory/timeout; PDF grande |
| Callback 401 | `PILO_INTERNAL_API_KEY` distinto backend vs Lambda |
| Callback 404 document | Clave S3 no sigue formato `cases/{caseId}/documents/{documentId}/...` |
| Backend no llega a RDS | Security group / subnet |
| Documento stuck `PENDING_UPLOAD` | Lambda no disparó o falló antes del callback |

---

## Coste estimado MVP (eu-west-1)

| Servicio | ~USD/mes |
|----------|----------|
| RDS db.t4g.micro | 0–15 (free tier) |
| S3 | < 1 |
| Lambda | < 1 |
| EC2 t4g.micro (si usás) | ~6 |
| **Total demo** | **~5–20** |

Apagar RDS y EC2 fuera de horario de demo.
