# PILO

**Platform for Intelligent Logistics of Official Procedures**

PILO es una plataforma web académica para la gestión y automatización de trámites administrativos que requieren múltiples documentos, requisitos, validaciones y pasos dependientes entre sí. El sistema permite representar un trámite como un conjunto de requisitos y tareas, permitiendo al usuario iniciar un expediente, consultar su progreso, cargar documentación y conocer qué elementos se encuentran pendientes, aprobados o rechazados.

La inteligencia artificial se utiliza para el procesamiento de documentos: clasificación, extracción de información estructurada y detección del tipo de documento. Las validaciones de requisitos y las decisiones de estado del trámite son realizadas mediante reglas deterministas en el backend.

> **Nota:** PILO es un prototipo académico. No sustituye sistemas oficiales ni ejecuta trámites gubernamentales reales.

---

## Tabla de contenidos

- [Arquitectura general](#arquitectura-general)
- [Stack tecnológico](#stack-tecnológico)
- [Estructura del repositorio](#estructura-del-repositorio)
- [Backend — Monolito modular](#backend--monolito-modular)
- [Frontend — Next.js](#frontend--nextjs)
- [Worker — Procesador documental](#worker--procesador-documental)
- [Pipeline de IA documental](#pipeline-de-ia-documental)
- [Modelo de datos](#modelo-de-datos)
- [API REST](#api-rest)
- [Workflow engine](#workflow-engine)
- [Integraciones externas](#integraciones-externas)
- [Arquitectura AWS (producción)](#arquitectura-aws-producción)
- [Seguridad](#seguridad)
- [CI/CD y DevSecOps](#cicd-y-devsecops)
- [Testing](#testing)
- [Setup local](#setup-local)
- [Demo](#demo)
- [Convenciones](#convenciones)

---

## Arquitectura general

```
                                     ┌─────────────────────┐
                                     │   External Sources   │
                                     │ BOE / datos.gob.es   │
                                     └──────────┬──────────┘
                                                │
                                                ▼
                                         Integration Layer
                                                │
┌──────────────┐       HTTPS       ┌────────────▼───────────┐
│   Next.js    │ ─────────────────►│      Spring Boot       │
│  TypeScript  │◄──────────────────│    Modular Monolith    │
│  Tailwind    │                   └──────┬─────┬─────┬─────┘
└──────────────┘                          │     │     │
                                          │     │     └──────────► Gemini LLM API
                                          │     │
                                          │     └────────────────► S3 / Local Storage
                                          │                             │
                                          │                       ObjectCreated
                                          │                             │
                                          │                             ▼
                                          │                     Lambda Processor
                                          │                             │
                                          │                      Structured JSON
                                          │                             │
                                          ◄─────────────────────────────┘
                                          │
                                          ▼
                                   PostgreSQL (RDS)
                                          ▲
                                          │
                                    EventBridge
                                          │
                                          ▼
                                   Deadline Lambda

                    ┌──────────────────────────────────────────┐
                    │ Terraform + GitHub Actions + CloudWatch  │
                    └──────────────────────────────────────────┘
```

### Principio fundamental: IA como componente, backend como autoridad

```
┌─────────────┐     Interpreta      ┌──────────────┐     Valida        ┌──────────────┐
│  Documento   │ ──────────────────► │   IA (LLM)   │ ────────────────►│ Spring Boot  │
│  del usuario │                     │  Extracción   │                  │  Reglas det.  │
└─────────────┘                     │  estructurada │                  │  Transiciones │
                                    └──────────────┘                  │  Auditoría    │
                                                                      └──────────────┘
```

- La IA **interpreta, clasifica y extrae** datos estructurados de los documentos.
- Spring Boot **valida determinísticamente**, controla estados, ejecuta transacciones y aplica reglas de negocio.
- **La IA nunca cambia directamente el estado de un expediente.**

---

## Stack tecnológico

| Capa              | Tecnología                                            |
|-------------------|-------------------------------------------------------|
| **Frontend**      | Next.js 16, TypeScript, Tailwind CSS 4, TanStack Query, React Hook Form, Zod, Lucide icons |
| **Backend**       | Java 21, Spring Boot 4.1, Spring Security, Spring Data JPA, Flyway, Lombok |
| **Base de datos** | PostgreSQL 16                                         |
| **Autenticación** | JWT (jjwt 0.12.6), RBAC                              |
| **Almacenamiento**| AWS S3 (prod) / Filesystem local (dev)                |
| **IA**            | Gemini API (prod) / Mock determinista (dev)           |
| **Worker**        | Python 3.12, AWS Lambda                               |
| **Infraestructura** | Terraform, Docker Compose                          |
| **CI/CD**         | GitHub Actions                                        |
| **Tests**         | JUnit 5, Testcontainers, Vitest, Testing Library      |

---

## Estructura del repositorio

```
pilo/
├── pilo-backend/                  # API REST — Spring Boot (monolito modular)
│   ├── src/main/java/com/pilo/
│   │   ├── PiloBackendApplication.java
│   │   ├── auth/                  # Autenticación, JWT, Spring Security config
│   │   ├── users/                 # Entidad User, roles, seeders
│   │   ├── procedures/            # Catálogo de tipos de trámite, requisitos
│   │   ├── cases/                 # Expedientes (ProcedureCase)
│   │   ├── documents/             # Documentos, upload, almacenamiento, validación
│   │   │   ├── storage/           # Abstracción Local/S3
│   │   │   ├── validation/        # Reglas deterministas de validación
│   │   │   └── internal/          # Callback interno para extracciones
│   │   ├── workflows/             # Motor de workflow, tareas, dependencias
│   │   ├── ai/                    # Cliente Gemini, mock, extracción estructurada
│   │   ├── audit/                 # Eventos de auditoría
│   │   ├── integrations/          # BOE, datos.gob.es
│   │   └── common/               # Error handling, config, utilidades
│   ├── src/main/resources/
│   │   └── db/migration/          # Migraciones Flyway
│   ├── pom.xml
│   └── Dockerfile
│
├── pilo-frontend/                 # UI — Next.js + TypeScript
│   ├── src/
│   │   ├── app/                   # App Router (rutas)
│   │   │   ├── page.tsx           # Landing
│   │   │   ├── login/             # Página de login
│   │   │   ├── procedures/        # Catálogo público de trámites
│   │   │   ├── cases/             # Detalle de expediente
│   │   │   ├── my-cases/          # Mis expedientes
│   │   │   ├── admin/             # Panel de administración
│   │   │   └── profile/           # Perfil de usuario
│   │   ├── features/              # Feature modules
│   │   │   ├── auth/              # Lógica de autenticación
│   │   │   ├── cases/             # Lógica de expedientes
│   │   │   ├── procedures/        # Lógica de catálogo
│   │   │   ├── admin/             # Lógica de administración
│   │   │   ├── home/              # Lógica de landing
│   │   │   └── profile/           # Lógica de perfil
│   │   ├── components/
│   │   │   ├── ui/                # Componentes genéricos (Button, Card, Badge, etc.)
│   │   │   ├── pilo/              # Componentes de dominio (CaseCard, WorkflowStepper, etc.)
│   │   │   ├── layout/            # Layout, navbar
│   │   │   ├── motion/            # Animaciones
│   │   │   └── providers/         # Providers (query, theme)
│   │   ├── lib/                   # API client, auth, rutas, utilidades
│   │   └── test/                  # Setup de tests
│   ├── package.json
│   └── vitest.config.ts
│
├── pilo-worker/                   # Procesador documental — AWS Lambda (Python)
│   ├── handler.py                 # Entry point Lambda
│   ├── extraction.py              # Lógica de extracción con LLM
│   ├── backend_client.py          # Callback al backend
│   ├── requirements.txt
│   └── scripts/
│
├── infra/                         # Infraestructura como código
│   └── modules/
│       ├── s3/                    # Bucket S3
│       └── lambda/                # Lambda functions
│
├── docs/                          # Guías operativas
│   └── aws-operations.md          # Operaciones AWS y Terraform
│
├── .github/
│   └── workflows/
│       └── ci.yml                 # Pipeline CI con GitHub Actions
│
├── docker-compose.yml             # PostgreSQL local (opcional)
├── .env.example                   # Template de variables de entorno
└── pilo.md                        # Especificación técnica completa
```

---

## Backend — Monolito modular

El backend sigue una arquitectura de **monolito modular** en Spring Boot. Cada módulo encapsula sus controladores, servicios, entidades y repositorios.

```
com.pilo/
├── auth/          ← Autenticación y autorización
├── users/         ← Gestión de usuarios y roles
├── procedures/    ← Catálogo de trámites y requisitos
├── cases/         ← Expedientes (lifecycle)
├── documents/     ← Documentos, storage, validación
├── workflows/     ← Motor de workflow
├── ai/            ← Extracción con IA
├── audit/         ← Trazabilidad
├── integrations/  ← APIs externas
└── common/        ← Transversales
```

### Módulos y responsabilidades

| Módulo          | Responsabilidad                                                    | Clases clave |
|-----------------|-------------------------------------------------------------------|-------------|
| `auth`          | Login, emisión JWT, filtros de seguridad, rate limiting, CORS      | `SecurityConfig`, `JwtService`, `JwtAuthenticationFilter`, `LoginRateLimiter` |
| `users`         | Entidad `User`, enum `Role` (USER, REVIEWER, ADMIN), seeding      | `User`, `UserRepository`, `LocalUserSeeder` |
| `procedures`    | Tipos de trámite, requisitos, reglas de validación, catálogo CRUD  | `ProcedureType`, `Requirement`, `ProcedureCatalogFactory`, `AdminProcedureCatalogService` |
| `cases`         | Expedientes: creación, estados, progreso, acceso                   | `ProcedureCase`, `CaseService`, `CaseAccessService`, `CaseNumberGenerator` |
| `documents`     | Upload, almacenamiento (Local/S3), procesamiento, validación       | `DocumentService`, `DocumentProcessingService`, `DocumentValidationService` |
| `documents/storage` | Abstracción de almacenamiento con dos implementaciones          | `ObjectStorage` (interfaz), `LocalObjectStorage`, `S3ObjectStorage` |
| `documents/validation` | Reglas deterministas de validación de documentos              | `DocumentRule`, `ConfidenceRule`, `ExpectedDocumentTypeRule`, `FutureExpirationRule` |
| `workflows`     | Definición y ejecución del workflow, dependencias, transiciones    | `WorkflowService`, `WorkflowTask`, `WorkflowStepDefinition`, `WorkflowBlueprints` |
| `ai`            | Cliente de extracción estructurada (Gemini/Mock), callback interno | `StructuredExtractionClient`, `GeminiStructuredExtractionClient`, `MockStructuredExtractionClient` |
| `audit`         | Eventos de auditoría inmutables                                    | `AuditEvent`, `AuditService` |
| `integrations`  | Consultas a BOE y datos.gob.es                                     | `IntegrationService`, `IntegrationController` |
| `common`        | Error handling global, async config, cache, auth helpers           | `ApiExceptionHandler`, `AsyncConfig`, `CacheConfig` |

### Patrón de almacenamiento dual

```
                     ┌──────────────────┐
                     │  ObjectStorage   │   ← Interfaz
                     └───────┬──────────┘
                             │
              ┌──────────────┼──────────────┐
              ▼                             ▼
┌──────────────────────┐      ┌──────────────────────┐
│  LocalObjectStorage  │      │   S3ObjectStorage     │
│  (dev — filesystem)  │      │   (prod — presigned)  │
└──────────────────────┘      └──────────────────────┘

Selección por propiedad: PILO_STORAGE_PROVIDER=local|s3
```

### Patrón de procesamiento dual

```
                     ┌──────────────────────────────┐
                     │ StructuredExtractionClient    │   ← Interfaz
                     └───────────┬──────────────────┘
                                 │
              ┌──────────────────┼──────────────────┐
              ▼                                     ▼
┌────────────────────────────┐     ┌────────────────────────────┐
│ MockStructuredExtraction   │     │ GeminiStructuredExtraction │
│ (dev — datos deterministas)│     │ (prod — Gemini API)        │
└────────────────────────────┘     └────────────────────────────┘

Selección por propiedad: PILO_AI_PROVIDER=mock|gemini
```

---

## Frontend — Next.js

El frontend utiliza **Next.js 16 con App Router** y está organizado por features:

```
src/
├── app/              # Rutas (file-based routing)
│   ├── page.tsx          → /              Landing
│   ├── login/            → /login         Login
│   ├── procedures/       → /procedures    Catálogo público
│   ├── cases/            → /cases/[id]    Detalle expediente
│   ├── my-cases/         → /my-cases      Mis expedientes
│   ├── admin/            → /admin/*       Administración
│   └── profile/          → /profile       Perfil
├── features/         # Lógica de negocio por dominio (hooks, queries, mutations)
├── components/
│   ├── ui/           # Design system: Button, Card, Badge, Field, Progress, Skeleton, etc.
│   ├── pilo/         # Componentes de dominio: CaseCard, WorkflowStepper, FileDropzone, etc.
│   ├── layout/       # Layout shell, navbar
│   ├── motion/       # Animaciones y transiciones
│   └── providers/    # Query client, theme provider
└── lib/              # API client, auth context, rutas, utilidades
```

### Principios del frontend

- El frontend **no conoce detalles** de AWS S3 ni del proveedor LLM.
- **TanStack Query** para data fetching con cache y revalidación.
- **React Hook Form + Zod** para formularios con validación.
- **Sonner** para notificaciones toast.
- **Lucide React** para iconografía.

---

## Worker — Procesador documental

El worker es una **AWS Lambda en Python 3.12** que procesa documentos de forma asíncrona:

```
pilo-worker/
├── handler.py            # Entry point: recibe evento S3, orquesta el procesamiento
├── extraction.py         # Llama al LLM (Gemini) para extracción estructurada
├── backend_client.py     # Envía el JSON estructurado de vuelta al backend
└── requirements.txt      # Dependencias Python
```

### Flujo del worker

```
S3 ObjectCreated event
        │
        ▼
  handler.py
        │
        ├── Descarga documento de S3
        ├── Extrae texto / metadata
        └── Llama a extraction.py
                │
                ├── Prompt al LLM
                └── Parsea respuesta como JSON estructurado
                        │
                        ▼
              backend_client.py
                        │
                        └── POST /api/v1/internal/documents/{id}/extractions
```

---

## Pipeline de IA documental

```
[ User Upload ]
       │
       ▼
[ S3 / Local Storage ]
       │
       │ ObjectCreated (prod) / Inline (dev)
       ▼
[ Lambda / DocumentProcessingService ]
       │
       ├── Extraer texto / metadata
       └── LLM structured extraction
                │
                ▼
       [ Structured JSON ]
                │
                ▼
      [ Spring Boot Callback ]
                │
         Deterministic validation
                │
                ▼
         Update document status
                │
                ▼
        Re-evaluate requirements
                │
                ▼
         Advance workflow if valid
```

### Contrato de salida estructurada

```json
{
  "documentType": "rental_contract",
  "confidence": 0.96,
  "extractedFields": {
    "personName": "Juan Pérez",
    "idNumber": "12345678X",
    "issueDate": "2026-08-10",
    "expirationDate": "2027-08-10",
    "address": "Calle Mayor 12, Madrid"
  }
}
```

### Reglas de validación determinista

El backend aplica reglas configurables por requisito. Implementaciones actuales:

| Regla                      | Clase                        | Descripción |
|----------------------------|------------------------------|-------------|
| Confianza mínima           | `ConfidenceRule`             | `confidence >= threshold` |
| Tipo de documento esperado | `ExpectedDocumentTypeRule`   | `detectedType == expectedType` |
| Fecha de expiración futura | `FutureExpirationRule`       | `expirationDate > now()` |

> **Regla de seguridad:** Si el LLM dice "document is valid", eso **no basta** para aprobar. Spring Boot comprueba las reglas definidas por el sistema.

---

## Modelo de datos

```
┌──────────────────┐     1:N     ┌───────────────────┐     1:N     ┌──────────────────┐
│       User       │────────────►│  ProcedureCase    │────────────►│    Document      │
│──────────────────│             │───────────────────│             │──────────────────│
│ id               │             │ id                │             │ id               │
│ email            │             │ case_number       │             │ case_id          │
│ password_hash    │             │ user_id           │             │ requirement_id   │
│ full_name        │             │ procedure_type_id │             │ file_name        │
│ role (enum)      │             │ status (enum)     │             │ mime_type        │
│ created_at       │             │ progress_%        │             │ file_size        │
└──────────────────┘             │ deadline_at       │             │ s3_key           │
                                 │ created_at        │             │ status (enum)    │
                                 └─────────┬─────────┘             │ created_at       │
                                           │                       └────────┬─────────┘
                                           │ 1:N                            │ 1:1
                                           ▼                                ▼
                                 ┌───────────────────┐     ┌────────────────────────┐
                                 │  WorkflowTask     │     │  DocumentExtraction    │
                                 │───────────────────│     │────────────────────────│
                                 │ id                │     │ id                     │
                                 │ case_id           │     │ document_id            │
                                 │ task_name         │     │ document_type_detected │
                                 │ status (enum)     │     │ confidence             │
                                 │ assigned_role     │     │ extracted_data (JSONB) │
                                 │ depends_on_task_id│     │ processed_at           │
                                 │ updated_at        │     └────────────────────────┘
                                 └───────────────────┘

┌──────────────────┐     1:N     ┌───────────────────┐
│  ProcedureType   │────────────►│   Requirement     │
│──────────────────│             │───────────────────│
│ id               │             │ id                │
│ title            │             │ procedure_type_id │
│ description      │             │ code              │
│ target_days      │             │ name              │
│ created_at       │             │ description       │
└──────────────────┘             │ is_mandatory      │
                                 │ validation_rules  │  ← JSONB
                                 └───────────────────┘

┌───────────────────┐            ┌───────────────────┐
│   AuditEvent      │            │ ExternalReference  │
│───────────────────│            │───────────────────│
│ id                │            │ id                 │
│ actor_id          │            │ procedure_type_id  │
│ action            │            │ source_name        │
│ resource          │            │ external_id        │
│ result            │            │ metadata           │
│ metadata (JSONB)  │            └───────────────────┘
│ timestamp         │
└───────────────────┘
```

### Estados

| Entidad         | Estados posibles                                     |
|-----------------|------------------------------------------------------|
| `ProcedureCase` | `PENDING`, `IN_PROGRESS`, `UNDER_REVIEW`, `APPROVED`, `REJECTED` |
| `Document`      | `UPLOADED`, `PROCESSING`, `VALIDATED`, `REJECTED`     |
| `WorkflowTask`  | `PENDING`, `IN_PROGRESS`, `COMPLETED`, `BLOCKED`      |

---

## API REST

Base path: `/api/v1`

### Endpoints públicos

| Método | Endpoint                                 | Descripción                          |
|--------|------------------------------------------|--------------------------------------|
| `POST` | `/auth/login`                            | Autenticación y emisión de JWT       |
| `GET`  | `/procedures/types`                      | Catálogo de tipos de trámite         |
| `GET`  | `/procedures/types/{id}`                 | Detalle de un tipo de trámite        |
| `GET`  | `/integrations/boe/search?query={q}`     | Buscar información pública del BOE   |
| `GET`  | `/integrations/datos/search?query={q}`   | Buscar catálogo de datos públicos    |
| `GET`  | `/integrations/sia/search?query={q}`     | Referencias SIA (catálogo público)   |

### Endpoints autenticados (Bearer JWT)

| Método | Endpoint                                     | Descripción                                   |
|--------|----------------------------------------------|-----------------------------------------------|
| `GET`  | `/auth/me`                                   | Datos del usuario autenticado                 |
| `PATCH`| `/auth/me`                                   | Actualizar nombre del perfil                  |
| `POST` | `/auth/me/password`                          | Cambiar contraseña                            |
| `POST` | `/cases`                                     | Crear expediente                              |
| `GET`  | `/cases`                                     | Listar expedientes del usuario                |
| `GET`  | `/cases/pending-review`                      | Bandeja de revisión (REVIEWER / ADMIN)        |
| `GET`  | `/cases/{id}`                                | Detalle, requisitos y timeline de auditoría   |
| `POST` | `/cases/{id}/documents/upload`               | Subir documento (multipart, modo local)       |
| `GET`  | `/cases/{id}/documents`                      | Documentos con extracción IA y fallos de regla |
| `GET`  | `/cases/{id}/workflow`                       | Estado del workflow                           |
| `POST` | `/cases/{id}/workflow/final-review`          | Aprobar o rechazar expediente (REVIEWER/ADMIN)|

### Endpoints internos (API key entre servicios)

| Método | Endpoint                                     | Descripción                          |
|--------|----------------------------------------------|--------------------------------------|
| `POST` | `/internal/documents/{id}/extractions`       | Callback con resultado de extracción |

---

## Workflow engine

El workflow se representa como un conjunto de tareas con **dependencias**:

```
Solicitud iniciada
        │
        ▼
Validar identidad
        │
        ├──────────────┐
        ▼              ▼
Revisar contrato   Revisar seguro
        │              │
        └──────┬───────┘
               ▼
        Revisión final
               │
         ┌─────┴─────┐
         ▼           ▼
     Aprobado      Rechazado
```

- Cada `WorkflowTask` tiene un `depends_on_task_id` que define el orden.
- El backend **determina cuándo una tarea puede avanzar** basándose en el estado de sus dependencias.
- Los blueprints de workflow se definen en `WorkflowBlueprints` y se instancian al crear un expediente.
- La IA no modifica directamente el workflow.

---

## Integraciones externas

### Principio de integración

```
External API  →  Integration Adapter  →  Normalized DTO  →  Application Service
```

No se propagan modelos de APIs externas por el dominio interno.

### BOE OpenData

- Consultar información normativa pública.
- Asociar referencias normativas con tipos de trámite.
- Mostrar fuentes de referencia al usuario.

### datos.gob.es

- Buscar datasets públicos por categoría administrativa.
- Enriquecer el catálogo de referencias.

### SIA (referencial)

- Búsqueda en catálogo público de procedimientos administrativos.
- Mostrada junto a BOE y datos.gob.es en el detalle del expediente.

Ambas integraciones incluyen **fallbacks demo** cuando las APIs externas no están disponibles (`pilo.integrations.demo-fallback-enabled`).

---

## Arquitectura AWS (producción)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              AWS Cloud                                 │
│                                                                        │
│  ┌──────────────┐    Presigned URL    ┌──────────────┐                 │
│  │  Spring Boot │ ──────────────────► │    S3        │                 │
│  │  (EC2/ECS)   │                     │  Documents   │                 │
│  │              │ ◄────── Callback ── │              │                 │
│  └──────┬───────┘                     └──────┬───────┘                 │
│         │                                    │ ObjectCreated           │
│         │                                    ▼                         │
│         │                             ┌──────────────┐                 │
│         │                             │   Lambda     │                 │
│         │                             │  doc-proc    │                 │
│         │                             └──────────────┘                 │
│         │                                                              │
│         ▼                                                              │
│  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐           │
│  │ RDS Postgres │     │ EventBridge  │────►│   Lambda     │           │
│  └──────────────┘     │ (scheduled)  │     │  deadlines   │           │
│                       └──────────────┘     └──────────────┘           │
│                                                                        │
│  ┌──────────────┐                                                      │
│  │  CloudWatch  │  Logs + métricas de todos los servicios              │
│  └──────────────┘                                                      │
└─────────────────────────────────────────────────────────────────────────┘
```

### Servicios AWS utilizados

| Servicio      | Uso                                                    |
|---------------|--------------------------------------------------------|
| **S3**        | Almacenamiento de documentos, presigned URLs           |
| **Lambda**    | `document-processor` (OCR/LLM), `deadline-worker`     |
| **RDS**       | PostgreSQL — estado transaccional                      |
| **EventBridge** | Tareas programadas (deadlines)                       |
| **CloudWatch** | Logs y métricas                                       |

### Servicios deliberadamente fuera del MVP

Kubernetes/EKS, Kafka/MSK, ElastiCache, OpenSearch, NAT Gateway, SageMaker — la ausencia es deliberada para minimizar coste y complejidad.

---

## Seguridad

### Autenticación y autorización

- **Spring Security** con filtro JWT stateless.
- **RBAC** con tres roles: `USER`, `REVIEWER`, `ADMIN`.
- **Rate limiting** en endpoint de login (`LoginRateLimiter`).
- Autorización por recurso (un usuario solo ve sus expedientes).

### Almacenamiento de archivos

- S3 bucket privado, sin URLs públicas.
- Presigned URLs con expiración corta.
- Validación de tamaño y MIME type.
- Nombres de objeto generados por backend (no confiados del usuario).

### Auditoría

Cada acción sensible genera un `AuditEvent`:

```
DOCUMENT_UPLOADED  |  DOCUMENT_VALIDATED  |  DOCUMENT_REJECTED
CASE_CREATED       |  CASE_APPROVED       |  CASE_REJECTED
WORKFLOW_ADVANCED
```

### Secretos

- No se almacenan claves en Git.
- Variables gestionadas mediante `.env` (local) y secretos de AWS/GitHub Actions (producción).

---

## CI/CD y DevSecOps

### Pipeline CI (GitHub Actions)

```
Push / Pull Request
       │
       ▼
 Lint / Format checks
       │
       ▼
   Unit tests
       │
       ▼
 Integration tests (Testcontainers)
       │
       ▼
 SAST / Dependency scan
       │
       ▼
     Build
```

### Pipeline CD (producción)

```
GitHub Actions
       │
       ├── Build frontend (Next.js)
       ├── Build backend image (Docker)
       ├── Package Lambdas (Python)
       ├── Terraform plan
       └── Terraform apply (con aprobación)
```

### Herramientas DevSecOps

| Herramienta | Propósito                     |
|-------------|-------------------------------|
| Semgrep     | SAST — análisis estático      |
| Trivy       | Container scanning            |
| Dependabot  | Dependency scanning           |
| GitHub      | Secret scanning               |

---

## Testing

### Backend

```bash
cd pilo-backend && ./mvnw test
```

- **Unit tests**: validadores, workflow transitions, cálculo de deadlines, reglas de autorización.
- **Integration tests**: Testcontainers con PostgreSQL. No leen `.env` ni tocan la BD local.
- **AI tests**: validación de schema, campos obligatorios, manejo de respuestas inválidas, confidence thresholds.

### Frontend

```bash
cd pilo-frontend && pnpm test
```

- Vitest + Testing Library + jsdom.

---

## Setup local

### Prerrequisitos

- JDK 21
- Node.js 22 y pnpm 11
- Docker (para Testcontainers; opcional para Compose)
- PostgreSQL en `localhost:5432`

### Instalación

1. **Copiar variables de entorno** (nunca commitear `.env` reales):

```bash
cp .env.example .env
cp pilo-backend/.env.example pilo-backend/.env
cp pilo-frontend/.env.example pilo-frontend/.env.local
```

2. **Configurar base de datos**: apuntar `SPRING_DATASOURCE_*` a tu PostgreSQL. Crear BD `pilo` si no existe.

3. **Opcional — IA real** (sin API key usa mock determinista):

```bash
GEMINI_API_KEY=your-key-here
```

4. **Opcional — PostgreSQL con Docker** (si no tienes uno):

```bash
docker compose up -d
```

5. **Iniciar el backend** (perfil `local` incluye datos de demo):

```bash
cd pilo-backend
./mvnw spring-boot:run
```

6. **Iniciar el frontend**:

```bash
cd pilo-frontend
pnpm install
pnpm dev
```

### URLs locales

| Servicio | URL                                   |
|----------|---------------------------------------|
| Web      | `http://localhost:3000` (o `3001` si cambiaste el puerto del dev server) |
| API      | `http://localhost:8080`                |
| Health   | `http://localhost:8080/actuator/health`|

Asegúrate de que `PILO_CORS_ORIGINS` incluya el puerto del frontend (`3000` y/o `3001`).

### Usuarios de demo (perfil `local`)

| Email                  | Password        | Rol      |
|------------------------|-----------------|----------|
| `user@pilo.test`       | `Password123!`  | USER     |
| `reviewer@pilo.test`   | `Password123!`  | REVIEWER |
| `admin@pilo.test`      | `Password123!`  | ADMIN    |

---

## Demo

### Flujo ciudadano (USER)

1. Abrir `/procedures` y explorar el catálogo sin login.
2. Abrir un trámite → **Iniciar trámite** (login con `user@pilo.test` si hace falta).
3. Subir PDF/imagen por requisito en `/cases/{id}`.
4. Ver extracción IA, estado validado/rechazado y motivos de rechazo por regla.
5. Consultar referencias externas (BOE, datos.gob.es, SIA) y la timeline de actividad.

### Flujo revisor (REVIEWER)

1. Login con `reviewer@pilo.test`.
2. Ir a **Revisión** (`/review`) y abrir un expediente *En revisión*.
3. Revisar documentos y pulsar **Aprobar expediente** o **Rechazar**.

### Flujo admin (ADMIN)

1. Login con `admin@pilo.test`.
2. **Administración** → editar catálogo y reglas de validación.
3. Puede ejecutar también el flujo de revisor.

Guía ampliada paso a paso: [`pilo.md`](pilo.md).

---

## Convenciones

- **Idioma del código**: inglés (clases, variables, métodos, tablas, columnas, endpoints, DTOs, enums).
- **Idioma de UI**: español (textos visibles para el usuario).
- **IA nunca cambia estado** del expediente. El backend valida y transiciona.
- **Demo mode**: almacenamiento local (`PILO_STORAGE_PROVIDER=local`). Producción usa S3 + Lambda.
- **Priorizar MVP sólido** sobre funcionalidades secundarias.

---

## Licencia

Proyecto académico. No es un sistema gubernamental oficial ni sustituye trámites administrativos reales.
