# PILO — Guía del proyecto

Documento orientado a **demo, evaluación y defensa del caso de estudio**. Para arquitectura, stack, API completa y setup detallado, ver [`README.md`](README.md).

> **Nota:** Este archivo está en `.gitignore` por defecto. Si quieres versionarlo en el repo, quita `pilo.md` de `.gitignore`.

---

## 1. Qué es PILO (en una frase)

Plataforma web académica para gestionar trámites administrativos: el ciudadano sube documentos, la **IA extrae datos**, el **backend valida con reglas** y un **revisor** puede cerrar el expediente.

**Principio clave:** la IA interpreta; el sistema decide.

---

## 2. Cobertura del caso de estudio

| Requisito | Estado | Notas |
|-----------|--------|-------|
| Trámite = requisitos + tareas | ✅ Hecho | Workflow fijo de 4 pasos al crear expediente |
| Iniciar expediente y ver progreso | ✅ Hecho | `/procedures` → iniciar → `/cases/{id}` |
| Subir documentos por requisito | ✅ Hecho | Dropzone local; S3 + Lambda en perfil AWS |
| Estados pendiente / validado / rechazado | ✅ Hecho | Por documento y requisito |
| IA: tipo, extracción estructurada | ✅ Backend + UI | Mock local o Gemini con API key |
| Validación por reglas (no IA) | ✅ Hecho | Tipo, confianza, vencimiento |
| Motivos de rechazo visibles | ✅ Hecho | Códigos traducidos en la UI |
| Revisión final humana | ✅ Hecho | Rol REVIEWER → `/review` → aprobar/rechazar |
| Expediente APPROVED / REJECTED | ✅ Hecho | Tras revisión final |
| Auditoría / trazabilidad | ✅ Parcial | Timeline unificada (caso + documentos) |
| BOE + datos.gob.es | ⚠️ Parcial | Búsqueda con fallback demo; enlaces si la API responde |
| SIA (referencial) | ⚠️ Parcial | Endpoint + UI; datos vía catálogo público |
| Admin: catálogo de trámites | ✅ Hecho | CRUD de tipos y requisitos |
| Perfil de usuario | ✅ Hecho | Nombre, contraseña, tema |
| Auth con roles USER / REVIEWER / ADMIN | ✅ Hecho | JWT + cuentas demo en perfil `local` |

**Leyenda:** ✅ cumple para demo académica · ⚠️ implementado pero no “producción real” · ❌ no hecho

---

## 3. Qué falta (priorizado)

### No bloquea la demo académica

- Registro de usuarios (solo cuentas demo sembradas).
- Gestión de usuarios/roles desde admin.
- Vista previa / descarga de documentos subidos.
- Editor de workflow por trámite (hoy todos usan el mismo blueprint).
- Borrar un tipo de trámite desde admin.
- Tabla `ExternalReference` persistida y vinculada al catálogo (hoy solo búsqueda en expediente).
- BOE con búsqueda real por término (hoy sumario fijo + fallback).
- Tests E2E (Playwright/Cypress) del flujo completo.

### Documentado en README pero no en el repo

- Lambda `deadline-worker` + EventBridge para plazos.
- Módulos Terraform / despliegue AWS completo.
- Algunos endpoints del README aún no reflejan los últimos cambios (revisión, SIA, perfil).

### Mejoras opcionales de producto

- Más reglas de validación (formato DNI, cruce entre documentos).
- Notificaciones al ciudadano cuando se aprueba/rechaza.
- Panel de auditoría global para admin.

---

## 4. Guía de demo (15–20 min)

### Preparación

```bash
# Terminal 1 — backend (perfil local, datos demo)
cd pilo-backend && ./mvnw spring-boot:run

# Terminal 2 — frontend
cd pilo-frontend && pnpm dev
```

URLs habituales: frontend `http://localhost:3000` (o `3001` si cambiaste puerto), API `http://localhost:8080`.

| Cuenta | Email | Password | Rol |
|--------|-------|----------|-----|
| Ciudadano | `user@pilo.test` | `Password123!` | USER |
| Revisor | `reviewer@pilo.test` | `Password123!` | REVIEWER |
| Admin | `admin@pilo.test` | `Password123!` | ADMIN |

### Flujo A — Ciudadano (USER)

1. **Catálogo** → `/procedures` (sin login).
2. Abrir un trámite → **Iniciar trámite** (login si hace falta).
3. En el expediente, **subir un PDF/imagen** por cada requisito obligatorio.
4. Esperar unos segundos: estado pasa a *Analizando con IA* → *Validado* o *Rechazado*.
5. Expandir el documento: ver **datos extraídos** (nombre, DNI, fechas…) y, si aplica, **motivo de rechazo**.
6. Panel **Referencias externas**: buscar BOE, datos.gob.es y SIA.
7. Sidebar **Actividad**: timeline con creación, subidas y validaciones.

### Flujo B — Revisor (REVIEWER)

1. Login como `reviewer@pilo.test`.
2. Nav **Revisión** → `/review` (solo desktop en nav lateral; en móvil ir a la URL).
3. Abrir un expediente en estado *En revisión* (todos los obligatorios validados).
4. Revisar documentos + extracciones IA.
5. **Aprobar expediente** o **Rechazar** (comentario opcional).
6. Comprobar estado final *Aprobado* / *Rechazado* y entrada en Actividad (*Revisión final completada*).

### Flujo C — Admin (ADMIN)

1. Login como `admin@pilo.test`.
2. **Administración** → editar catálogo, requisitos y reglas (`expectedDocumentType`, confianza mínima, vencimiento).
3. Puede hacer también el flujo de revisor.

### Qué decir en la defensa

- *“La IA no aprueba nada: solo devuelve JSON; `DocumentValidationService` aplica reglas.”*
- *“El revisor cierra el ciclo con `FINAL_REVIEW`; el ciudadano no auto-aprueba el expediente.”*
- *“BOE/SIA/datos.gob.es enriquecen contexto; no conectamos con administraciones reales.”*

---

## 5. IA en local

| Variable | Efecto |
|----------|--------|
| Sin `GEMINI_API_KEY` | Mock determinista (útil para demo estable) |
| Con `GEMINI_API_KEY` | Extracción real vía Gemini |

Modo procesamiento local: `PILO_PROCESSING_MODE=inline` (tras subida, el backend procesa en background).

---

## 6. Roles y pantallas

| Rol | Pantallas principales |
|-----|------------------------|
| USER | Inicio, Trámites, Expedientes, Perfil |
| REVIEWER | + Revisión (`/review`), acceso a cualquier expediente por URL |
| ADMIN | + Administración de catálogo, mismo acceso de revisor |

---

## 7. API nueva (post-MVP)

| Método | Ruta | Quién |
|--------|------|-------|
| `GET` | `/api/v1/cases/pending-review` | REVIEWER, ADMIN |
| `POST` | `/api/v1/cases/{id}/workflow/final-review` | REVIEWER, ADMIN |
| `GET` | `/api/v1/integrations/sia/search?query=` | Público |
| `PATCH` | `/api/v1/auth/me` | Autenticado |
| `POST` | `/api/v1/auth/me/password` | Autenticado |

Los documentos en `GET /cases/{id}/documents` incluyen `extraction`, `validationFailures` y `processingFailureReason`.

---

## 8. Checklist antes de presentar

- [ ] PostgreSQL levantado y backend arranca sin errores Flyway.
- [ ] Frontend apunta al backend (`NEXT_PUBLIC_API_URL`).
- [ ] CORS incluye tu puerto de frontend (`3000` o `3001`).
- [ ] Probado flujo USER → subida → validación visible.
- [ ] Probado flujo REVIEWER → aprobar expediente.
- [ ] Probado login admin y edición de un requisito.
- [ ] Decidir si demo con mock IA (más predecible) o Gemini (más real).

---

## 9. Relación README ↔ esta guía

| Documento | Para qué |
|-----------|----------|
| **README.md** | Arquitectura, modelo de datos, API, AWS, CI, setup técnico |
| **pilo.md** (este) | Estado vs enunciado, demo por roles, huecos conocidos |
| **pilo-frontend/DESING.MD** | Sistema de diseño UI (tokens, componentes) |

Si actualizas funcionalidad, sincroniza primero esta guía y luego el README (sección Demo y tabla de endpoints).

---

## 10. Próximos pasos sugeridos (si hay tiempo)

1. Actualizar README (demo + endpoints + puerto 3001).
2. Commits separados del último bloque de features.
3. Un test E2E mínimo: login → subir doc → revisar → aprobar.
4. (Opcional) Quitar `pilo.md` del `.gitignore` si debe entregarse con el repo.
