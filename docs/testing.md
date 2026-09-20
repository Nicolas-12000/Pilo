# Pruebas

Qué hay hoy, cómo se corre, y qué dejamos para más adelante. El CI (`.github/workflows/ci.yml`) ejecuta backend `mvn test` y frontend `pnpm lint|test|build`.

## Backend (`pilo-backend`)

```bash
cd pilo-backend && ./mvnw test
```

| Tipo | Qué cubre | Requisito |
|------|-----------|-----------|
| Unitarias | JWT, reglas de documento (tipo, confianza, DNI/NIE, fechas), parser BOE/datos, query de integraciones | Nada extra |
| Integración (`*IT`) | Auth, catálogo, workflow, revisión final | **Docker** (Testcontainers + PostgreSQL). Sin Docker esos tests fallan al levantar contexto. |

La IA **no** se testea llamando a Gemini en CI. Se testea el contrato: JSON extraído + reglas deterministas (un DNI inválido se rechaza aunque el mock ponga confianza alta).

## Frontend (`pilo-frontend`)

```bash
cd pilo-frontend && pnpm test
pnpm lint
pnpm build
```

Vitest + Testing Library (catálogo público, formularios). No hay pruebas del modal de preview todavía.

## Worker (`pilo-worker`)

Sin suite automatizada. Se prueba a mano: S3 `PUT` → logs de Lambda → documento `VALIDATED` / `REJECTED` en el API.

## Qué no hay (y no bloquea el enunciado)

- E2E (Playwright/Cypress) del flujo login → subir → preview → aprobar.
- Tests de carga.
- Semgrep / Trivy en el CI (están mencionados en el README histórico; no forman parte del pipeline actual).

## Futuro (cuando haya fotos de demo)

1. Un E2E mínimo con Playwright: USER sube un PDF de fixture → REVIEWER abre preview → aprueba.
2. Test del rewrite Amplify: no aplica en local; smoke manual `https://….amplifyapp.com/api/v1/auth/login`.
3. Capturas en [`docs/evidence/`](evidence/README.md).

Hasta entonces, la “prueba de producto” es: arrancar local, recorrer los tres roles, y guardar las imágenes de esa carpeta.
