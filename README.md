# PILO

**Platform for Intelligent Logistics of Official Procedures**

PILO is an academic web platform for configuring and running administrative procedures: cases, document requirements, deterministic validation, and a workflow with dependencies. AI interprets documents; Spring Boot owns state and business decisions.

This repository is a modular monolith. The current slice is week 1: local environment, authentication, and a login UI.

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

## Tests

Backend tests use Testcontainers. They do not read your `.env` and they do not touch the local `pilo` database.

```bash
cd pilo-backend && ./mvnw test
cd pilo-frontend && pnpm test
```

## API (week 1)

| Method | Path | Auth |
| --- | --- | --- |
| `GET` | `/api/v1/procedures/types` | Public |
| `GET` | `/api/v1/procedures/types/{id}` | Public |
| `POST` | `/api/v1/auth/login` | Public |
| `GET` | `/api/v1/auth/me` | Bearer JWT |

The procedure catalog is public. Authentication is required only when starting a case (next slice).

Code, tables, and endpoints are in English. User-facing copy is in Spanish.

## Conventions

- AI never changes case state. The backend validates and transitions.
- Uploads will go to S3 with presigned URLs; binaries will not pass through Spring Boot.
- Prefer a solid MVP over extra cloud services.

## License

Academic project. Not an official government system and not a substitute for real administrative filings.
