# PILO — Operaciones externas (AWS, RDS, Terraform, CD)

Guía de **lo que hacés vos al final** para no gastar Free Tier antes de la demo.  
El código ya está preparado: perfil `aws`, S3, Lambda worker, RDS. No hace falta Terraform el primer día.

Objetivo del producto (enunciado): reducir el trabajo manual de organizar y revisar documentos. En cloud eso significa **no meter PDFs ni Node en el mismo micro que Java**.

---

## Arquitectura free-tier: qué va dónde

```
Ciudadano / Revisor
        │
        ▼
┌──────────────────────┐     HTTPS      ┌─────────────────────────────┐
│ Frontend             │ ──────────────►│ EC2 t4g.micro (VPC pública) │
│ Amplify o Vercel     │                │ Solo Spring Boot (API)      │
│ (NO en el EC2)       │                └──────────┬────────┬─────────┘
└──────────────────────┘                           │        │
                                                   │        │ SG :5432
                      PUT presigned                │        ▼
        ┌──────────────────────────────────────────┘  ┌─────────────────┐
        ▼                                             │ RDS PostgreSQL  │
┌──────────────────┐   ObjectCreated                  │ db.t4g.micro    │
│ S3 pilo-docs     │ ───────────────► Lambda          │ VPC privada     │
│ (privado)        │                  document-       │ sin IP pública  │
│ PDFs / imágenes  │                  processor       └─────────────────┘
└──────────────────┘                  (Gemini/mock)
```

| Pieza | Dónde | Por qué |
|-------|--------|---------|
| **Frontend Next.js** | **Amplify Hosting** (AWS) o **Vercel** (gratis). **No en el EC2. No en el bucket de documentos.** | Next no es un `index.html` estático (`force-dynamic`). En el mismo `t4g.micro` que Java se queda sin RAM y se cae. S3 estático solo valdría con `output: 'export'`, que hoy no encaja con las rutas dinámicas. |
| **Backend Spring** | **EC2 t4g.micro** (ARM, Ubuntu) | Un JAR, ~400–650 MB heap. Perfil `aws` baja el pool de BD a 5 y Tomcat a 20 hilos. |
| **PostgreSQL** | **RDS db.t4g.micro** en la **misma VPC**, **sin acceso público** | El EC2 entra por security group al puerto 5432. No abras RDS a `0.0.0.0`. |
| **Documentos** | **S3 privado** (`pilo-docs-*`) | El EC2 no guarda PDFs en disco. Upload = presigned PUT. Preview/descarga autenticada pasa por la API (máx. 10–15 MB). |
| **IA documental** | **Lambda** `pilo-document-processor` | S3 `ObjectCreated` → extrae JSON → callback interno. Así Gemini **no corre en el EC2**. |
| **BOE / datos.gob.es / SIA** | El **backend en EC2** llama APIs públicas | Prototipo académico; no hay SARA ni sistemas internos. |
| **Deadline worker / EventBridge** | **No lo creés ahora** | El plazo ya se muestra en la UI. Un Lambda extra = más logs y más superficie. |
| **Terraform / Semgrep / Trivy** | **Después de la demo** | No bloquean el enunciado. |

### Qué no crear (ahí se va el dinero)

- NAT Gateway (varios USD/día).
- ALB + ECS Fargate “porque queda bonito”.
- Segundo EC2 para el frontend.
- RDS público + EC2 en otra región.
- CloudWatch Logs sin retención (deja **3 días**).
- Versionado S3 + Intelligent-Tiering en un bucket de demo.
- Lambda de plazos, OpenSearch, ElastiCache, EKS.

### VPC mínima (sí, una VPC)

1. VPC por defecto de la región **o** una VPC simple: 1 subnet pública (EC2), 1 subnet privada (RDS).
2. Security group `pilo-ec2`: 22 solo tu IP, 8080 (o 443 si ponés nginx) al mundo / al CloudFront.
3. Security group `pilo-rds`: **5432 solo desde `pilo-ec2`**.
4. **Sin NAT.** El EC2 tiene IP pública para salir a BOE, datos.gob.es y Gemini (si algún día procesás inline). La Lambda sale por su propia red.

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
7. **Public access: No.** RDS solo es visible desde el SG del EC2.
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

## Paso 6 — Desplegar backend en EC2 (optimizado)

Usá **un solo t4g.micro** (ARM, 1 GB). El JAR del repo ya arranca con heap limitado (`JAVA_TOOL_OPTIONS` en el `Dockerfile`).

### Lo que debe correr en esa máquina

- Docker o `java -jar` del backend.
- Nada más: **ni PostgreSQL local** (va en RDS), **ni Next.js**, **ni archivos de usuarios**.

### Aprovisionar (resumen)

```bash
# Ubuntu 24.04 ARM, t4g.micro, 8–16 GB disco gp3
sudo apt-get update
sudo apt-get install -y openjdk-21-jre-headless
# o Docker si preferís el Dockerfile del repo
```

Variables mínimas en `/etc/pilo.env`:

```bash
SPRING_PROFILES_ACTIVE=aws
SPRING_DATASOURCE_URL=jdbc:postgresql://TU-RDS:5432/pilo
SPRING_DATASOURCE_USERNAME=pilo_admin
SPRING_DATASOURCE_PASSWORD=...
SPRING_DATASOURCE_MAX_POOL_SIZE=5
SPRING_DATASOURCE_MIN_IDLE=1
SERVER_TOMCAT_MAX_THREADS=20
PILO_JWT_SECRET=...          # 32+ bytes
PILO_INTERNAL_API_KEY=...
PILO_CORS_ORIGINS=https://TU-FRONTEND
PILO_STORAGE_PROVIDER=s3
PILO_S3_BUCKET=pilo-docs-dev
AWS_REGION=eu-west-1
PILO_PROCESSING_MODE=external
PILO_AI_PROVIDER=gemini      # o mock para no gastar Gemini
GEMINI_API_KEY=...           # solo en Lambda si processing=external
PILO_INTEGRATIONS_DEMO_FALLBACK=false
```

IAM del EC2 (instance profile, no access keys en disco):

- `s3:PutObject`, `s3:GetObject`, `s3:AbortMultipartUpload` en `arn:aws:s3:::TU-BUCKET/cases/*`
- Nada de `s3:*` ni `AdministratorAccess`

### Build

```bash
cd pilo-backend
./mvnw -B package -DskipTests
java -jar target/pilo-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=aws
```

O Docker:

```bash
docker build -t pilo-backend ./pilo-backend
docker run --env-file /etc/pilo.env -p 8080:8080 --memory=768m --cpus=1 pilo-backend
```

`--memory=768m` evita que el contenedor mate al kernel del micro.

Checklist backend:

- [ ] `GET /actuator/health` responde
- [ ] Flyway OK en logs
- [ ] Presigned URL `method: PUT` hacia S3
- [ ] Preview `GET /api/v1/cases/{id}/documents/{docId}/content` con JWT
- [ ] CORS = origen real del frontend (Amplify/Vercel)

Apagá la instancia (`stop`, no terminate) cuando no demos. RDS igual.

---

## Paso 7 — Frontend: Amplify o Vercel, no EC2 ni S3 de documentos

El frontend es Next.js con rutas dinámicas (`/cases/[id]`). **No lo subas como sitio estático al bucket de PDFs** y **no lo arranques con `next start` en el mismo EC2** que Spring.

| Opción | Cuándo | Coste |
|--------|--------|-------|
| **Amplify Hosting** (recomendado si querés “todo AWS”) | Conectás el repo, build `pnpm build`, start `pnpm start` o framework Next | Free tier Amplify suele bastar para la demo |
| **Vercel** | Más rápido de enseñar | 0 USD en hobby |
| S3 + CloudFront estático | Solo si más adelante hacemos `output: 'export'` | Barato, hoy **no aplica** |
| `next start` en el EC2 del API | Nunca en t4g.micro compartido | Se cae Java |

```bash
cd pilo-frontend
pnpm install --frozen-lockfile
pnpm build
```

Variable de build:

```bash
NEXT_PUBLIC_API_URL=https://api.tudominio.com
# o http://EC2_PUBLIC_DNS:8080 en demo sin dominio
```

El frontend detecta `method: PUT` en presigned URL y sube **directo a S3** (el EC2 no recibe el PDF). Preview y descarga van a la API con JWT.

---

## Paso 8 — Terraform (vos, después de la demo viva)

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

## Paso 10 — Deadline worker, Semgrep y Trivy (fase posterior)

No bloquean el enunciado ni la demo usable.

Cuando toque, **después** de tener EC2 + RDS + S3 + Lambda documental estables:

1. Lambda `pilo-deadline-worker` + EventBridge `rate(1 hour)` — el plazo ya se ve en la UI.
2. Semgrep (SAST) y Trivy (imagen) en GitHub Actions — el CI actual ya corre tests.
3. Terraform en `infra/` — hoy la carpeta es un esqueleto a propósito.

---

## Checklist demo final

- [ ] Login usuario → crear expediente → subir PDF
- [ ] El PDF aparece en S3 (`cases/.../documents/...`), no en el disco del EC2
- [ ] CloudWatch Lambda: extracción OK (o mock)
- [ ] Backend: documento `VALIDATED` o `REJECTED` con motivo
- [ ] Revisor abre **vista previa**, contrasta datos IA y aprueba/rechaza
- [ ] Timeline de auditoría visible
- [ ] Búsqueda BOE / datos.gob.es en vivo (`PILO_INTEGRATIONS_DEMO_FALLBACK=false`)

---

## Troubleshooting

| Síntoma | Causa probable |
|---------|----------------|
| Presigned URL 403 en browser | CORS del bucket o `Content-Type` no coincide |
| EC2 “killed” / instance unreachable | Heap + Next + Postgres en el mismo micro. Sacá frontend y RDS. |
| Lambda timeout | PDF grande; 512 MB / 120 s basta para la demo |
| Callback 401 | `PILO_INTERNAL_API_KEY` distinto backend vs Lambda |
| Callback 404 document | Clave S3 no sigue `cases/{caseId}/documents/{documentId}/...` |
| Backend no llega a RDS | Security group: 5432 solo desde el SG del EC2 |
| Documento stuck `PENDING_UPLOAD` | Lambda no disparó o falló antes del callback |
| Preview 409 DOCUMENT_NOT_READY | Aún `PENDING_UPLOAD`; esperá el callback |

---

## Coste estimado demo (eu-west-1, apagado el resto del mes)

| Servicio | Qué hacer | ~USD |
|----------|-----------|------|
| EC2 t4g.micro | Stop cuando no demos | 0–6 (horas de demo ≈ céntimos) |
| RDS db.t4g.micro | Stop/snapshot; Free Tier si aplica | 0–15 |
| S3 documentos | < 1 GB, sin versionado, retención logs 3 días | < 1 |
| Lambda documental | Cientos de invocaciones | < 1 |
| Amplify / Vercel | Hobby / free tier | 0 |
| NAT / ALB / segundo EC2 | **No crear** | — |
| **Total si apagás máquinas** | | **~0–5** |

Presupuesto: **Billing → Budgets → 5 USD**. Si llega el aviso, stop EC2 y RDS.

Orden práctico para vos al final (un sábado):

1. Budget 5 USD + región fija.
2. VPC / SG (EC2 público, RDS privado).
3. RDS micro + S3 privado + CORS.
4. EC2 + JAR perfil `aws` + instance profile S3.
5. Frontend Amplify/Vercel con `NEXT_PUBLIC_API_URL`.
6. Lambda worker + trigger S3 (si querés IA fuera del EC2).
7. Terraform / CD / Semgrep: otro día.
