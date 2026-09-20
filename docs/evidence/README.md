# Evidencia visual (capturas)

Carpeta para **fotos y un GIF** cuando hagamos la demo usable. Hoy está vacía a propósito: no subas pantallazos con datos reales de nadie.

Cuando las tengas, el README raíz las enlazará aquí. Sirven para LinkedIn, la defensa y reclutadores que no van a clonar el repo.

## Qué fotografiar (checklist)

Usá las cuentas de `local` (`user@pilo.test`, `reviewer@pilo.test`, `admin@pilo.test`). Recortá la barra del navegador si sale el puerto.

| # | Archivo sugerido | Qué se tiene que ver |
|---|------------------|----------------------|
| 1 | `01-catalogo.png` | Catálogo público de trámites, sin login |
| 2 | `02-expediente-requisitos.png` | Expediente con requisitos, progreso y dropzone |
| 3 | `03-extraccion-ia.png` | Documento validado o rechazado + campos extraídos + motivo de regla |
| 4 | `04-preview-revisor.png` | Modal de vista previa del PDF/imagen **junto** a los datos IA |
| 5 | `05-bandeja-revision.png` | `/review` con al menos un expediente en revisión |
| 6 | `06-resolucion.png` | Aprobar/rechazar expediente (comentario opcional) |
| 7 | `07-referencias-externas.png` | BOE / datos.gob.es / SIA con badge **En vivo** o **Demo** |
| 8 | `08-admin-reglas.png` | Admin editando `expectedDocumentType` / confianza |
| 9 | `09-auditoria.png` | Timeline de actividad del expediente |

Opcional: `demo.gif` (15–25 s) del flujo 2 → 4 → 6. Pesa menos que un vídeo de LinkedIn.

## Cómo guardarlas

```
docs/evidence/
  README.md          ← este archivo
  01-catalogo.png
  …
  demo.gif
```

PNG o WebP, ancho ~1400 px. Nada de `.env`, JWT, IPs de EC2 ni DNI de una persona real.

## Cómo se verán en el README

Cuando existan, enlazá así (no hace falta que estén todas el primer día):

```markdown
![Vista previa del documento en la revisión](docs/evidence/04-preview-revisor.png)
```

## Qué no sustituyen las fotos

Las capturas **no** demuestran producción, ni senioridad, ni que AWS esté desplegado. Demuestran el producto: trámite, IA, reglas, humano.
