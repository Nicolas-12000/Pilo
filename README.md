# PILO

**Spain** — administrative procedures, open-government APIs, DNI/NIE validation. Not a generic “govtech” template bolted onto another country’s data.

Spanish administrative procedures are not slow because of the stamp. They are slow because a person retypes a DNI, checks an expiry date, and then waits for a missing annex that was never requested in the first place.

**PILO** (Platform for Intelligent Logistics of Official Procedures) is an academic case-file system for that loop. The challenge is Spanish public administration: *trámites* with dependent requirements, DNI/NIE documents, and catalogues that already live in the open ([BOE](https://www.boe.es/) consolidada, [datos.gob.es](https://datos.gob.es/), SIA). We use those sources as they are, not as decoration.

A citizen opens a procedure, uploads the papers, and a reviewer decides. An extractor reads the PDF. **The extractor never changes the status of the file.**

This is a working prototype, not a *sede electrónica*. It does not file anything with a public authority.

## The problem

A procedure is a pile of requirements with dependencies: identity, proof of address, a form that expires, a step that cannot start until another is done. Most of the time is spent transcribing what the document already says, then arguing about whether it said it.

The interesting engineering question is not wrapping a chatbot around a form. It is **who is allowed to decide**. If extraction invents an ID number, a backend that trusts it will approve a case that should never have moved.

## The rule

```
PDF  →  structured extraction  →  Spring Boot applies deterministic rules  →  workflow moves
```

- The worker may classify a document and fill fields it can actually see. If a field is not in the file, it is omitted. It does not invent.
- Validation is code: expected type, confidence floor, Spanish DNI/NIE checksums, date coherence, required fields.
- Only Spring Boot transitions the case. Audit rows are written for upload, validation, rejection, and the final review.

That split is the product. The rest is plumbing so a reviewer can *see* the PDF next to the extraction instead of trusting a green badge.

## What you can do in the app

The UI is in Spanish. The code is in English.

1. **Browse without an account.** The catalogue is public: title, requirements, target days.
2. **Open a case.** A citizen starts a procedure, gets a case number, a deadline, and a workflow with blocked steps until documents land.
3. **Upload.** PDF, JPEG, PNG or WebP, max 15 MB. The HTTP request returns; extraction runs later (in-process locally, Lambda on AWS). The case page polls only while something is still `PROCESSING`.
4. **Review.** A reviewer opens the file in the browser, reads the extracted fields, and searches live Spanish catalogues (BOE, [datos.gob.es](https://datos.gob.es/), SIA/PAG) via their JSON APIs — metadata and links only, no scraping, no dataset downloads. Then approves or rejects. Preview uses an authenticated blob URL so the iframe does not need a JWT.
5. **Administer.** An admin edits the catalogue and the validation rules. They can disable a user: the next API call fails, without rotating the JWT secret for everyone.

Local demo accounts (profile `local`, password `Password123!`):

- `user@pilo.test` — citizen
- `reviewer@pilo.test` — reviewer
- `admin@pilo.test` — admin

## How it is built

```
Browser (Next.js)  ──JSON/JWT──►  Spring Boot  ──►  PostgreSQL
                                      │
                                      ├── ObjectStorage  (disk locally, S3 in aws)
                                      │         └── ObjectCreated ──►  one Lambda
                                      │                                    │
                                      │         ◄── extraction callback ───┘
                                      └── BOE legislacion-consolidada
                                          datos.gob.es catalogue
```

| Piece | Choice | Why |
| --- | --- | --- |
| API | Java 21, Spring Boot, modular monolith | One deployable on a free-tier micro; modules (`auth`, `cases`, `documents`, `workflows`, …) instead of a microservice zoo |
| UI | Next.js, TypeScript, TanStack Query | App Router, Spanish copy, role-aware navigation |
| Files | `ObjectStorage` → local or S3 | Same domain code; presigned upload in production |
| Extraction | `StructuredExtractionClient` + a mock | Swap the provider; CI never calls a live one |
| Worker | Python 3.12 Lambda, S3 `ObjectCreated` | Keeps the PDF and extraction off the 1 GB EC2 |
| Schema | Flyway | Expand-only migrations; Hibernate `validate` |
| Auth | JWT + RBAC, user loaded on every request | Disable / password change invalidates the session without Redis |

### Modular monolith

The API is one deployable; cohesion is by **Java package** (`auth`, `cases`, `documents`, …). HTTP controllers stay thin: they resolve auth and call a **named use case** in `*.application` (for example `CreateCaseUseCase`, `CaseDocumentUseCase`, `CompleteFinalReviewUseCase`, `AuthAccountUseCase`). Domain work stays in existing `*Service` types.

Some boundaries are enforced in tests with **ArchUnit** (`ModularMonolithArchitectureTest`):

- **`integrations`** talks only to outbound HTTP and Jackson — it does not depend on `cases`, `procedures`, `workflows`, `documents`, or `users`.
- **`ai`** holds extraction adapters (`StructuredExtractionClient`, properties). It does not depend on persistence or workflow packages (the Lambda callback lives under `documents.internal` with the rest of `/internal/documents/**`).
- **`users`** does not call `audit` directly: admin enable/disable publishes `UserAccessChangeEvent`; **`audit`** listens and writes the trail (keeps identity decoupled from side effects).
- **`CaseController`**, **`DocumentController`**, **`WorkflowController`**, **`AuthController`** must not inject domain services directly (rules keep the application layer visible).

Import the repo root as a **Maven project** (see root `pom.xml`) so your IDE classpath includes **test** dependencies such as ArchUnit.

CI on every push: `./mvnw test` (JUnit, Testcontainers when Docker is there) and `pnpm lint && pnpm test && pnpm build`.

## Failure modes we actually handled

- **Lambda retries the same PDF.** The extraction callback is idempotent. Duplicate `ObjectCreated` events do not corrupt the case. After a few failures the document stays `PROCESSING_FAILED` instead of looping the extractor.
- **Someone gets into an account on a Friday.** Admin → Users → disable. The access token is still signed, but the filter loads `users.enabled` and `token_version` from Postgres and returns 401. Password change bumps the version and returns a fresh token.
- **JWT secret rotation on one box.** `PILO_JWT_SECRET` plus optional `PILO_JWT_PREVIOUS_SECRET`. The `aws` profile refuses to boot on the development defaults.
- **A deploy goes bad.** Images are tagged; `PILO_IMAGE_TAG=previous` brings the last known jar back on the same t4g.micro. No second machine, no ALB.

The slow part of the demo is extraction and, sometimes, BOE (4 s timeout, live HTTP). The API is not.

## Run it locally

You need JDK 21, Node 22, pnpm 11, and PostgreSQL on `localhost:5432`. Docker is optional (Compose for Postgres, required for Testcontainers).

```bash
cp .env.example .env
# point SPRING_DATASOURCE_* at your database; create database pilo if needed

# optional
docker compose up -d

cd pilo-backend && ./mvnw spring-boot:run   # profile local seeds the three users
cd pilo-frontend && pnpm install && pnpm dev
```

- App: `http://localhost:3000`
- API: `http://localhost:8080`
- Health: `http://localhost:8080/actuator/health`

Without an extraction API key, a mock fills the JSON. Point `pilo.ai.provider` at a live client when you want a real PDF read; the backend only consumes structured fields.

```bash
cd pilo-backend && ./mvnw test
cd pilo-frontend && pnpm test
```

## Repository

```
pilo-backend/     Spring Boot API
pilo-frontend/    Next.js UI
pilo-worker/      Lambda (S3 → extract → callback)
scripts/aws/      Provision, deploy helpers, verify
docker-compose.yml
.github/workflows/ci.yml
```

AWS: see `scripts/aws/README.md`.

## Screenshots for this README

Put PNG or WebP under `docs/screenshots/` (paths referenced below). Use the **public demo** (Amplify) or a clean local build; hide real DNI numbers if you use a real scan.

| File | What to capture |
|------|-----------------|
| `home-catalogue.png` | Landing: hero + grid **Trámites disponibles** (several procedure cards). |
| `procedure-detail.png` | Public procedure page: title, requirements list, target days, **Iniciar expediente**. |
| `case-documents.png` | Case view: requirement with **VALIDADO**, extracted fields, **eye** preview control. |
| `case-preview.png` | Document preview modal: image or PDF beside extraction (proves human verification). |
| `review-queue.png` | Reviewer **Cola de revisión** with at least one pending case. |
| `review-decision.png` | Approve/reject actions on a case ready for final review. |
| `references-external.png` | **Referencias externas** (BOE / datos.gob.es / SIA) after a search — readable cards. |
| `architecture.png` | Optional: simple diagram (browser → Next → API → S3/Lambda/Postgres); hand-drawn or Excalidraw is fine. |

Suggested embed block once files exist:

```markdown
![Catálogo de trámites](docs/screenshots/home-catalogue.png)
![Expediente y extracción](docs/screenshots/case-documents.png)
```

## License

Academic project. Not official government software. Do not use it to submit a real procedure.
