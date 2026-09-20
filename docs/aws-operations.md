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
┌──────────────────────┐     /api rewrite     ┌─────────────────────────────┐
│ Amplify Hosting      │ ───────────────────► │ EC2 Elastic IP :80 nginx    │
│ Next.js (la web)     │                      │        ↓ 127.0.0.1:8080     │
│ https://*.amplifyapp │                      │        Spring Boot          │
└──────────────────────┘                      └──────────┬────────┬─────────┘
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
| **Frontend Next.js** | **Amplify Hosting** (AWS). **No en el EC2. No en el bucket `pilo-docs`.** | Toda la arquitectura queda en AWS. Amplify sirve SSR/SSG de Next (CloudFront + compute gestionado). El `t4g.micro` no aguanta Java + Node. El bucket de documentos es privado y no es un sitio web. |
| **Backend Spring** | **EC2 t4g.micro** + **nginx :80** → JAR en `127.0.0.1:8080` | El micro no expone Tomcat. Amplify y Lambda entran por el puerto 80. |
| **PostgreSQL** | **RDS db.t4g.micro** en la **misma VPC**, **sin acceso público** | El EC2 entra por security group al puerto 5432. No abras RDS a `0.0.0.0`. |
| **Documentos** | **S3 privado** (`pilo-docs-*`) | El EC2 no guarda PDFs en disco. Upload = presigned PUT. Preview/descarga autenticada pasa por la API (máx. 10–15 MB). |
| **IA documental** | **Lambda** `pilo-document-processor` | S3 `ObjectCreated` → extrae JSON → callback interno. Así Gemini **no corre en el EC2**. |
| **BOE / datos.gob.es / SIA** | El **backend en EC2** llama APIs públicas | Prototipo académico; no hay SARA ni sistemas internos. |
| **Deadline worker / EventBridge** | **No lo creés ahora** | El plazo ya se muestra en la UI. Un Lambda extra = más logs y más superficie. |
| **Terraform / Semgrep / Trivy** | **Después de la demo** | No bloquean el enunciado. |

### Qué no crear (ahí se va el dinero)

- NAT Gateway (varios USD/día).
- **API Gateway, ALB, Cognito.** El “gateway” de la demo es Amplify (rewrite `/api`) + nginx en el EC2. Spring ya hace JWT y CORS.
- ALB + ECS Fargate “porque queda bonito”.
- Segundo EC2 para el frontend.
- RDS público + EC2 en otra región.
- CloudWatch Logs sin retención (deja **3 días**).
- Versionado S3 + Intelligent-Tiering en un bucket de demo.
- Lambda de plazos, OpenSearch, ElastiCache, EKS.

### VPC mínima (sí, una VPC)

AWS **siempre** mete EC2, RDS y Lambda en una VPC. En free tier usás la **VPC por defecto** de la región (no hace falta crear otra; crear NAT/subnets custom es lo que cobra).

```
VPC (default, eu-west-1)
│
├─ Subnet pública
│    EC2  Elastic IP
│    SG pilo-ec2:   22 = solo TU IP
│                   80 = 0.0.0.0/0   (nginx; Amplify + Lambda)
│                   8080 = NADA
│    Spring solo en 127.0.0.1:8080 (no sale al SG)
│
├─ Subnet de RDS (sin IP pública)
│    RDS PostgreSQL
│    SG pilo-rds:   5432 = SOLO SG pilo-ec2
│
├─ S3 pilo-docs     Block Public Access ON
│    objetos solo con presigned URL o IAM del EC2/Lambda
│
└─ Lambda worker    no está “en tu subnet”; AWS la ejecuta
                    y sale a internet para pegarle al Elastic IP :80
                    con X-Internal-Api-Key
```

**Sin NAT Gateway.** El EC2 tiene IP pública para: SSH (tu IP), nginx, y para que Spring salga a BOE/datos.gob.es. RDS **nunca** tiene IP pública.

### Qué está protegido (defensa + demo)

| Capa | Qué hay | Gratis |
|------|---------|--------|
| Red | RDS inaccesible desde internet; 8080 cerrado; SSH restringido | Sí |
| Discos | PDFs en S3 privado, no en el EC2 | Sí |
| App | JWT + roles USER/REVIEWER/ADMIN; login con rate limit; CORS = Amplify | Sí |
| Worker | `/internal/**` exige `X-Internal-Api-Key` (comparación constant-time) | Sí |
| IAM | Instance profile: solo `s3:Get/Put` en `cases/*` | Sí |
| Front | HTTPS en Amplify (ellos terminan TLS) | Sí |

### Qué es un compromiso de prototipo (decirlo en la defensa)

- **Puerto 80 en HTTP** entre Amplify/Lambda y nginx. Sin dominio no hay Let's Encrypt. El navegador igual ve HTTPS (Amplify).
- **EC2 en subnet pública.** Evitamos NAT (~32 USD/mes). El API queda alcanzable en `:80`; nginx solo proxya `/api` y `/actuator/health`.
- **Health público** (`GET /actuator/health`) sin detalles de config.
- **Sin WAF / Shield avanzado.** Para un TFG con budget 5 USD no aplica.
- Cuentas demo: **desactivar** `NEXT_PUBLIC_ENABLE_DEMO_LOGIN` y no usar `Password123!` en Amplify.

Secretos (`PILO_JWT_SECRET`, password RDS, `PILO_INTERNAL_API_KEY`) van en `.env.aws` en el disco del EC2 (chmod 600) o, si querés un plus, SSM Parameter Store (free tier da para unos parámetros). **Nunca** en Git.

---

## Mapa de responsabilidades

| Componente | Lo hace el código (repo) | Lo hacés vos (AWS / Terraform / GitHub) |
|------------|--------------------------|----------------------------------------|
| Worker Lambda | `pilo-worker/` (Python) | Crear función, trigger S3, IAM, env vars |
| Storage S3 | `S3ObjectStorage`, presigned PUT | Bucket, CORS, evento `ObjectCreated` |
| Frontend | Next.js (Amplify lo construye) | Amplify Hosting + rewrite `/api` + CORS |
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
      "https://TU-APP.amplifyapp.com"
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

### IP estable (Elastic IP) — obligatorio

Si el EC2 se para y arranca, **la IP pública cambia**. El front no puede depender de `ec2-12-34.compute.amazonaws.com`.

1. EC2 → **Elastic IPs** → Allocate (VPC).
2. Associate a tu instancia.
3. Anotá `ELASTIC_IP` (ej. `18.203.x.x`). Esa es la dirección del API para Amplify.

Mientras la EIP esté asociada a la instancia, **no cambia**. Si tirás la VM y creás otra, reasociás la misma EIP: el front no se toca.

### Aprovisionar (resumen)

```bash
# Ubuntu 24.04 ARM, t4g.micro, 16 GB disco gp3 (el build de Maven necesita swap)
sudo apt-get update
sudo apt-get install -y docker.io docker-compose-v2 git nginx
sudo usermod -aG docker ubuntu
# volvé a entrar por SSH para que el grupo docker aplique
```

### Cómo desplegar el back (clonar + Docker)

**Correr con Docker, sí. Compilar Maven a mano, no.**  
**Buildear la imagen en el propio EC2, sí** (el micro es ARM; tu WSL suele ser x86 y la imagen no arranca).

El `t4g.micro` tiene 1 GB: sin swap el `docker build` se muere. Antes de clonar:

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

```bash
git clone TU_REPO pilo
cd pilo
cp .env.example .env.aws
nano .env.aws    # pegá las variables de abajo; la DB es RDS, no el compose local
docker compose -f docker-compose.ec2.yml up -d --build
```

Eso **solo** levanta Spring. **No** uses `docker-compose.yml`: ese archivo es Postgres de desarrollo y chocaría con RDS.

La primera build baja Maven (~unos minutos). Después `up -d` es rápido. El contenedor publica `127.0.0.1:8080`; nginx (siguiente apartado) escucha en `:80`.

No ejecutes `pnpm` ni el worker en esta máquina.

Variables mínimas en `pilo/.env.aws` (no lo subas a git):

```bash
SPRING_PROFILES_ACTIVE=aws
SPRING_DATASOURCE_URL=jdbc:postgresql://TU-RDS:5432/pilo
SPRING_DATASOURCE_USERNAME=pilo_admin
SPRING_DATASOURCE_PASSWORD=...
SPRING_DATASOURCE_MAX_POOL_SIZE=5
SPRING_DATASOURCE_MIN_IDLE=1
SERVER_TOMCAT_MAX_THREADS=20
# 0.0.0.0 dentro del contenedor; el host solo publica 127.0.0.1:8080
SERVER_ADDRESS=0.0.0.0
SERVER_PORT=8080
PILO_JWT_SECRET=...          # 32+ bytes
PILO_INTERNAL_API_KEY=...
PILO_CORS_ORIGINS=https://TU-APP.amplifyapp.com
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

### Actualizar el API

```bash
cd ~/pilo
git pull
docker compose -f docker-compose.ec2.yml up -d --build
```

No hace falta `mvn` en el host: el Dockerfile es multi-stage.

### Nginx delante del contenedor (sí, conviene)

No pongas Tomcat en internet. Nginx en el mismo EC2 ocupa poco RAM y deja un puerto normal:

```
Internet :80  →  nginx  →  127.0.0.1:8080  (Spring)
```

```bash
sudo apt-get install -y nginx
```

`/etc/nginx/sites-available/pilo`:

```nginx
server {
    listen 80 default_server;
    listen [::]:80 default_server;
    server_name _;

    client_max_body_size 1m;

    location /actuator/health {
        proxy_pass http://127.0.0.1:8080;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 120s;
    }

    location / {
        return 404;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/pilo /etc/nginx/sites-enabled/pilo
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t && sudo systemctl reload nginx
```

Security group: **80 abierto**, **8080 cerrado**. Amplify y Lambda hablan con `http://ELASTIC_IP` (puerto 80).

**HTTPS (443 + Let's Encrypt): no lo hagas todavía.** Hace falta un dominio (Route53). Amplify ya da HTTPS al navegador; el tramo Amplify → nginx puede ser HTTP en la demo. Cuando tengas `api.tudominio.com`, entonces `certbot --nginx` y el rewrite de Amplify pasa a `https://api.tudominio.com/api/<*>`.

Lambda: `PILO_BACKEND_URL=http://ELASTIC_IP` (sin `:8080`).

Checklist backend:

- [ ] `curl http://ELASTIC_IP/actuator/health` responde (por nginx)
- [ ] `8080` no está abierto en el SG
- [ ] Flyway OK en logs
- [ ] Presigned URL `method: PUT` hacia S3
- [ ] Preview autenticado funciona vía Amplify `/api/...`
- [ ] CORS = origen Amplify (`https://….amplifyapp.com`)

Apagá la instancia (`stop`, no terminate) cuando no demos. RDS igual.

---

## Paso 7 — Frontend en Amplify (no es un S3 vuestro)

Hay **dos “S3” distintos**. Mezclarlos es lo que genera el lío de la IP:

| Recurso | Qué es | ¿El navegador le pega al EC2? |
|---------|--------|-------------------------------|
| **Amplify Hosting** (`https://xxxx.amplifyapp.com`) | La **web** (Next.js). AWS pone CloudFront detrás. Vos no creás este bucket. | No. El usuario entra aquí. |
| **S3 `pilo-docs`** | Los **PDFs**. Privado. Presigned PUT. | No. El navegador sube el archivo **directo a S3** con URL firmada. |

El EC2 **nunca sirve el HTML**. Solo Spring (`/api/v1/...`).

### Cómo evita el front quedarse con una IP muerta

El navegador **no llama a `http://18.203.x.x:8080`**. Llama a la misma URL de Amplify y Amplify **reenvía** `/api/*` al Elastic IP:

```
Navegador
   │
   │  1. https://xxxx.amplifyapp.com/login          → HTML/JS (Amplify)
   │  2. https://xxxx.amplifyapp.com/api/v1/auth/login
   │        rewrite 200 ───────────────────────────► nginx :80 → Spring :8080
   │  3. https://pilo-docs.s3.amazonaws.com/cases/…  → PDF (presigned, no es el front)
```

Si el EC2 se cae y levantás **otra** instancia con **la misma Elastic IP**, Amplify sigue igual: no rebuild, no cambia `NEXT_PUBLIC_API_URL`.

HTTPS de Amplify + HTTP en el micro no rompe el navegador: el browser solo ve `https://….amplifyapp.com`.

### 7.1 Crear la app

1. [Amplify Console](https://console.aws.amazon.com/amplify/) → **Create new app** → GitHub (root **`pilo-frontend`**).
2. Framework: **Next.js — SSR** (no “Static HTML”).
3. Build (si no lo detecta):

```yaml
version: 1
frontend:
  phases:
    preBuild:
      commands:
        - corepack enable
        - pnpm install --frozen-lockfile
    build:
      commands:
        - pnpm build
  artifacts:
    baseDirectory: .next
    files:
      - '**/*'
  cache:
    paths:
      - node_modules/**/*
      - .next/cache/**/*
```

**No pongas `NEXT_PUBLIC_API_URL` en Amplify.** En producción el cliente usa rutas relativas `/api/v1/...`.

### 7.2 Rewrite hacia el Elastic IP (esto SÍ se actualiza si cambia la IP)

Amplify → **Hosting → Rewrites and redirects** → abrir el editor JSON y **añadir al principio** (antes de las reglas de Next):

```json
[
  {
    "source": "/api/<*>",
    "status": "200",
    "target": "http://ELASTIC_IP/api/<*>"
  }
]
```

`status: 200` = proxy, no redirect. El usuario no ve la IP del EC2.

Si algún día reasociás la EIP a otra VM, este `target` es **el único sitio** que tocarías (y solo si cambiaste de IP; con Elastic IP no hace falta).

### 7.3 CORS

Aunque el browser hable con Amplify, Spring recibe `Origin: https://xxxx.amplifyapp.com`:

```bash
PILO_CORS_ORIGINS=https://TU-APP.amplifyapp.com
```

Bucket `pilo-docs` → el mismo origen (paso 3). SG del EC2: **80** abierto (nginx). **8080 cerrado**.

### 7.4 Comprobar

- [ ] `https://xxxxx.amplifyapp.com` carga el login (candado HTTPS).
- [ ] En DevTools → Network, el login es `https://xxxxx.amplifyapp.com/api/v1/auth/login` (no la IP).
- [ ] PUT del PDF va a `*.s3.*.amazonaws.com`, no a Amplify.
- [ ] Parás y arrancás el EC2: misma Elastic IP → el login sigue funcionando.

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
| Amplify Hosting | Free tier; una app Next | 0–1 |
| NAT / ALB / segundo EC2 / Vercel | **No hace falta** | — |
| **Total si apagás máquinas** | | **~0–5** |

Presupuesto: **Billing → Budgets → 5 USD**. Si llega el aviso, stop EC2 y RDS.

Orden práctico para vos al final (un sábado):

1. Budget 5 USD + región fija.
2. VPC / SG (EC2 público, RDS privado).
3. RDS micro + S3 privado + CORS.
4. EC2 + JAR perfil `aws` + instance profile S3.
5. Frontend **Amplify Hosting** con `NEXT_PUBLIC_API_URL` → CORS en API y en S3.
6. Lambda worker + trigger S3 (si querés IA fuera del EC2).
7. Terraform / CD / Semgrep: otro día.
