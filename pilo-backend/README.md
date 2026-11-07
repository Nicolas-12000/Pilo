# PILO — Backend API

Spring Boot modular monolith: expedientes, documentos, workflow, integraciones BOE/datos.gob.es.

Full architecture and story: [PILO monorepo](https://github.com/Nicolas-12000/Pilo).

## Run locally

JDK 21, PostgreSQL on `localhost:5432`.

```bash
cp ../.env.example .env   # from monorepo root, or create .env here
./mvnw spring-boot:run    # profile `local` seeds demo users
```

API: `http://localhost:8080` · Health: `http://localhost:8080/actuator/health`

```bash
./mvnw test
```

See root `application.yaml` and monorepo `docker-compose.yml` for Postgres.
