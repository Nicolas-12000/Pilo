# Evidencia: pruebas y demo visual

Apartado para reclutadores, defensa del TFG y un futuro post. **Hoy hay tests automatizados; las fotos se añaden cuando grabemos la demo.**

## 1. Pruebas que ya corren

CI: [`.github/workflows/ci.yml`](../.github/workflows/ci.yml) (backend `mvn test`, frontend lint + Vitest + build).

### Backend (`pilo-backend`)

```bash
cd pilo-backend && ./mvnw test
```

Los `*IT` usan Testcontainers (hace falta Docker). Sin Docker, los unitarios siguen siendo útiles:

```bash
./mvnw test -Dtest=DocumentValidationServiceTest,DocumentContentServiceTest,IntegrationServiceTest,JwtServiceTest
```

| Área | Dónde | Qué cubre |
|------|--------|-----------|
| Auth JWT | `JwtServiceTest` | Firma y lectura del token |
| Auth HTTP | `AuthControllerIT` | Login, CORS, perfil, rate limit (Docker) |
| Catálogo | `ProcedureTypeControllerIT` | Trámites públicos |
| Workflow | `CaseWorkflowIT`, `ReviewWorkflowIT` | Alta de expediente y revisión final |
| Reglas (anti-alucinación) | `DocumentValidationServiceTest` | Tipo, confianza, DNI/NIE, fechas |
| Preview | `DocumentContentServiceTest` | Acceso, `PENDING_UPLOAD`, disposition |
| Integraciones | `IntegrationServiceTest`, `IntegrationQueryValidatorTest` | Parseo BOE/datos, query segura |

### Frontend (`pilo-frontend`)

```bash
cd pilo-frontend && pnpm test && pnpm lint && pnpm build
```

Vitest + Testing Library: login, catálogo, cliente HTTP.

### Qué falta (backlog honesto)

- E2E (Playwright): login → subir PDF → preview → revisión final.
- Contrato OpenAPI generado y snapshot del JSON de extracción.
- Los IT en CI necesitan Docker en el runner (hoy `mvn test` en GitHub lo intenta; si el runner no levanta Testcontainers, fallan).

No inventamos cobertura: si un recuadro no tiene test, no lo listamos como “cubierto”.

## 2. Fotos / GIF (cuando las tengamos)

Carpeta: [`docs/screenshots/`](screenshots/README.md).

Cuando existan, enlazalas así en el README:

```markdown
![Vista previa del documento](docs/screenshots/04-preview.png)
```

Un GIF de 20–30 s del flujo revisor vale más que diez diagramas de AWS.

## 3. ¿Llegamos a senior con esto?

**No.** PILO es un prototipo sólido de **mid / junior alto** si se presenta con honestidad. Senior no es “más Lambdas”.

| Lo que un senior suele haber **operado** | ¿PILO? |
|------------------------------------------|--------|
| Producto con usuarios reales o tráfico | No (demo académica) |
| Observabilidad: logs, métricas, trazas, alertas que alguien atiende | No en prod |
| Incidentes, regresiones, rollback | No |
| Performance / límites (tamaño, colas, timeouts) medidos | Parcial (límites de archivo y heap del micro) |
| Ownership de un área (mentoring, RFC, decir que no) | Solo en el diseño del repo |
| Profundidad: un dominio o un sistema llevado 12+ meses | TFG, no un sistema vivo |

Para **apuntar** a senior, el repo no tiene que inflarse. El perfil sí:

1. **Operar esto una temporada** (aunque sea una tarde a la semana): Amplify + EC2 encendido, un fallo real, una nota de postmortem de 15 líneas.
2. **E2E + un dashboard mínimo** (CloudWatch básico o healthcheck externo). “Lo medí” > “lo diseñé”.
3. **Una herida documentada**: p. ej. “el t4g.micro OOM en el `docker build` → swap; el front no puede llevar la IP del EC2 → rewrite Amplify”.
4. Experiencia laboral o PRs en código ajeno. Senior se acredita con **impacto sostenido**, no con un monorepo.

Hasta entonces, en LinkedIn/CV: *prototipo full-stack con IA acotada y criterio cloud*. Eso es creíble. *Arquitecto senior de plataforma gubernamental* no lo es.
