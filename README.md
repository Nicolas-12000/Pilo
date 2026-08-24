# PILO

**Platform for Intelligent Logistics of Official Procedures**

PILO is an academic web platform for configuring and running administrative procedures: cases, document requirements, deterministic validation, and a workflow with dependencies. AI interprets documents; Spring Boot owns state and business decisions.

This repository is a modular monolith. The current slice covers a functional demo through phase 3 (without AWS): public catalog, cases, document upload with local storage, Gemini extraction (or mock when no API key), workflow progress, audit trail, and BOE/datos reference search.

## Stack

| Layer | Technology |
| --- | --- |
| Frontend | Next.js, TypeScript, Tailwind CSS |
| Backend | Java 21, Spring Boot 4, Spring Security, JWT |
| Database | PostgreSQL |
| Tests | JUnit, Testcontainers, Vitest |
| CI | GitHub Actions |
| Infra (later) | AWS S3, Lambda, Terraform |

## Repository layout

```
.
├── pilo-backend/     Spring Boot API (`/api/v1`)
├── pilo-frontend/    Next.js app
├── docker-compose.yml   Optional local PostgreSQL
└── .env.example      Shared local-variable template
```

## Prerequisites

- JDK 21
- Node.js 22 and pnpm 11
- Docker (for Testcontainers; also if you use the optional Compose file)
- A PostgreSQL instance on `localhost:5432`

You can keep an existing Postgres container. Compose is only for a clean machine.

## Local setup

1. Copy environment templates (never commit real `.env` files):

```bash
cp .env.example .env
cp pilo-backend/.env.example pilo-backend/.env
cp pilo-frontend/.env.example pilo-frontend/.env.local
```

2. Point `SPRING_DATASOURCE_*` at your database. Create a `pilo` database if it does not exist.

   Optional for real AI extraction (otherwise the backend uses deterministic mock data):

   ```bash
   GEMINI_API_KEY=your-key-here
   ```

3. Optional, only if you do not already run Postgres:

```bash
docker compose up -d
```

4. Start the API (profile `local` seeds demo users):

```bash
cd pilo-backend
./mvnw spring-boot:run
```

5. Start the UI:

```bash
cd pilo-frontend
pnpm install
pnpm dev
```

- API: `http://localhost:8080`
- Web: `http://localhost:3000`
- Health: `http://localhost:8080/actuator/health`

### Demo users (`local` profile)

| Email | Password | Role |
| --- | --- | --- |
| `user@pilo.test` | `Password123!` | USER |
| `reviewer@pilo.test` | `Password123!` | REVIEWER |
| `admin@pilo.test` | `Password123!` | ADMIN |

## Demo flow (phase 3, local)

1. Open `http://localhost:3000/procedures` and browse procedures without logging in.
2. Open a procedure and click **Iniciar trámite** (login if prompted).
3. Upload a PDF or image for each requirement on the case page.
4. The backend stores files under `PILO_STORAGE_PATH` (default `./storage`), calls Gemini when `GEMINI_API_KEY` is set, validates extracted fields deterministically, updates workflow tasks, and records audit events.
5. Use **Mis expedientes** to return to your cases. The integration panel searches BOE and datos.gob.es (with demo fallbacks when APIs are unavailable).

## Tests

Backend tests use Testcontainers. They do not read your `.env` and they do not touch the local `pilo` database.

```bash
cd pilo-backend && ./mvnw test
cd pilo-frontend && pnpm test
```

## API

| Method | Path | Auth |
| --- | --- | --- |
| `GET` | `/api/v1/procedures/types` | Public |
| `GET` | `/api/v1/procedures/types/{id}` | Public |
| `POST` | `/api/v1/auth/login` | Public |
| `GET` | `/api/v1/auth/me` | Bearer JWT |
| `POST` | `/api/v1/cases` | Bearer JWT |
| `GET` | `/api/v1/cases` | Bearer JWT |
| `GET` | `/api/v1/cases/{id}` | Bearer JWT |
| `POST` | `/api/v1/cases/{id}/documents/upload` | Bearer JWT (multipart) |
| `GET` | `/api/v1/cases/{id}/documents` | Bearer JWT |
| `GET` | `/api/v1/cases/{id}/workflow` | Bearer JWT |
| `GET` | `/api/v1/integrations/boe/search?query=` | Public |
| `GET` | `/api/v1/integrations/datos/search?query=` | Public |

The procedure catalog and integration search are public. Cases, documents, and workflow require authentication.

Code, tables, and endpoints are in English. User-facing copy is in Spanish.

## Conventions

- AI never changes case state. The backend validates and transitions.
- Demo mode stores uploads on disk (`PILO_STORAGE_PATH`). Production will use S3 presigned URLs and Lambda workers.
- Prefer a solid MVP over extra cloud services.

## License

Academic project. Not an official government system and not a substitute for real administrative filings.
