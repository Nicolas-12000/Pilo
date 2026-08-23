---
version: alpha
name: PILO
description: Sistema de diseño de PILO, plataforma de gestión de trámites administrativos con IA. Personalidad cálida y humana sin elegancia de startup — un único acento terracota gobierna la acción; el resto del color es semántica de estado directamente mapeada al modelo de datos (ProcedureCase, Document, WorkflowTask). Mobile-first, Next.js 14+ / App Router / Tailwind v4 / shadcn-Radix. Este documento es la única fuente de verdad visual — cualquier componente nuevo debe derivar sus valores de aquí, nunca de hex/px inventados en el momento.
colors:
  primary: "#B5551E"
  primary-hover: "#9E481D"
  primary-active: "#8A3F19"
  primary-disabled: "#E8DED2"
  on-primary: "#FFFFFF"
  on-primary-disabled: "#A79A8A"
  primary-container: "#F3DDD0"
  on-primary-container: "#5C2A0F"
  secondary: "#6B6259"
  on-secondary: "#FFFFFF"
  secondary-container: "#EDE8E2"
  on-secondary-container: "#3A342C"
  success: "#2F7D53"
  on-success: "#FFFFFF"
  success-container: "#DCEEE0"
  on-success-container: "#1B4E33"
  warning: "#9E6819"
  on-warning: "#FFFFFF"
  warning-container: "#F5E4C8"
  on-warning-container: "#5C3E0C"
  danger: "#B23B3B"
  on-danger: "#FFFFFF"
  danger-container: "#F6DCDC"
  on-danger-container: "#6B1F1F"
  info: "#3E6B8A"
  on-info: "#FFFFFF"
  info-container: "#DCE9F0"
  on-info-container: "#1F3E52"
  background: "#FAF6F1"
  surface: "#FFFFFF"
  surface-dim: "#F0EAE3"
  surface-container-low: "#FBF7F2"
  surface-container: "#F5EFE8"
  surface-container-high: "#EFE7DD"
  surface-container-highest: "#E8DED2"
  on-surface: "#241C16"
  on-surface-variant: "#6B6259"
  outline: "#A79A8A"
  outline-variant: "#DFD3C4"
  background-dark: "#1C1712"
  surface-dark: "#241E18"
  surface-container-dark: "#2C241D"
  surface-container-high-dark: "#352C23"
  on-surface-dark: "#F2EAE2"
  on-surface-variant-dark: "#C9BCAC"
  outline-dark: "#5C5044"
  primary-container-dark: "#4A2C1B"
  on-primary-container-dark: "#F3DDD0"
  success-container-dark: "#1E3A2A"
  on-success-container-dark: "#B7E3C6"
  warning-container-dark: "#3A2A0E"
  on-warning-container-dark: "#F0D9A8"
  danger-container-dark: "#3A1A1A"
  on-danger-container-dark: "#F0BEBE"
  info-container-dark: "#1C3040"
  on-info-container-dark: "#BEDCEC"
typography:
  display:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 2.25rem
    fontWeight: 700
    lineHeight: 1.15
    letterSpacing: -0.01em
  headline-lg:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 1.5rem
    fontWeight: 600
    lineHeight: 1.25
  headline-md:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 1.25rem
    fontWeight: 600
    lineHeight: 1.3
  title-md:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 1.0625rem
    fontWeight: 600
    lineHeight: 1.35
  title-sm:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 0.9375rem
    fontWeight: 600
    lineHeight: 1.35
  body-lg:
    fontFamily: "Inter, sans-serif"
    fontSize: 1.0625rem
    fontWeight: 400
    lineHeight: 1.55
  body-md:
    fontFamily: "Inter, sans-serif"
    fontSize: 1rem
    fontWeight: 400
    lineHeight: 1.55
  body-sm:
    fontFamily: "Inter, sans-serif"
    fontSize: 0.875rem
    fontWeight: 400
    lineHeight: 1.5
  label-md:
    fontFamily: "Inter, sans-serif"
    fontSize: 0.875rem
    fontWeight: 600
    lineHeight: 1.3
  label-caps:
    fontFamily: "Inter, sans-serif"
    fontSize: 0.6875rem
    fontWeight: 700
    lineHeight: 1
    letterSpacing: 0.06em
  code:
    fontFamily: "JetBrains Mono, ui-monospace, monospace"
    fontSize: 0.8125rem
    fontWeight: 500
    lineHeight: 1.4
rounded:
  xs: 4px
  sm: 8px
  md: 12px
  lg: 16px
  xl: 24px
  full: 9999px
spacing:
  xxs: 4px
  xs: 8px
  sm: 12px
  md: 16px
  lg: 24px
  xl: 32px
  xxl: 48px
  section: 64px
elevation:
  card: "0 1px 2px rgba(36, 28, 22, 0.06), 0 2px 8px rgba(36, 28, 22, 0.04)"
  elevated: "0 4px 16px rgba(36, 28, 22, 0.10)"
  overlay: "0 12px 32px rgba(36, 28, 22, 0.16)"
  card-dark: "0 1px 2px rgba(0, 0, 0, 0.30)"
  elevated-dark: "0 4px 16px rgba(0, 0, 0, 0.40)"
  overlay-dark: "0 12px 32px rgba(0, 0, 0, 0.50)"
motion:
  feedback: 120ms
  content: 200ms
  page: 250ms
  easing: "cubic-bezier(0.2, 0, 0, 1)"
icons:
  library: lucide-react
  strokeWidth: 1.75
  size-sm: 16px
  size-md: 20px
  size-lg: 24px
  status-pending: Clock
  status-in-progress: Loader2
  status-under-review: AlertTriangle
  status-approved: CheckCircle2
  status-rejected: XCircle
  action-upload: UploadCloud
  entity-document: FileText
  entity-case: FolderOpen
  entity-notification: Bell
  entity-external-source: Landmark
  entity-deadline: CalendarClock
  entity-audit: History
  nav-cases: FolderOpen
  nav-documents: FileText
  nav-notifications: Bell
  nav-profile: User
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    typography: "{typography.label-md}"
    rounded: "{rounded.md}"
    padding: 14px 20px
    height: 48px
  button-primary-hover:
    backgroundColor: "{colors.primary-hover}"
  button-primary-active:
    backgroundColor: "{colors.primary-active}"
  button-primary-disabled:
    backgroundColor: "{colors.primary-disabled}"
    textColor: "{colors.on-primary-disabled}"
  button-secondary:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface}"
    typography: "{typography.label-md}"
    rounded: "{rounded.md}"
    padding: 14px 20px
    height: 48px
  button-secondary-hover:
    backgroundColor: "{colors.surface-container-high}"
  button-text-link:
    backgroundColor: transparent
    textColor: "{colors.primary-hover}"
    typography: "{typography.label-md}"
  button-icon:
    backgroundColor: "{colors.surface-container}"
    rounded: "{rounded.full}"
    size: 44px
  button-icon-hover:
    backgroundColor: "{colors.surface-container-high}"
  input-field:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface}"
    typography: "{typography.body-md}"
    rounded: "{rounded.sm}"
    padding: 12px 14px
    height: 48px
  input-field-focused:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface}"
  input-field-error:
    backgroundColor: "{colors.danger-container}"
    textColor: "{colors.on-danger-container}"
  select-field:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface}"
    typography: "{typography.body-md}"
    rounded: "{rounded.sm}"
    padding: 12px 14px
    height: 48px
  file-dropzone:
    backgroundColor: "{colors.surface-container-low}"
    textColor: "{colors.on-surface-variant}"
    rounded: "{rounded.md}"
    padding: "{spacing.lg}"
  file-dropzone-active:
    backgroundColor: "{colors.primary-container}"
    textColor: "{colors.on-primary-container}"
  file-dropzone-rejected:
    backgroundColor: "{colors.danger-container}"
    textColor: "{colors.on-danger-container}"
  case-card:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.lg}"
    padding: "{spacing.md}"
  document-row:
    backgroundColor: "{colors.surface-container-low}"
    rounded: "{rounded.sm}"
    padding: "{spacing.sm}"
  requirement-item-done:
    backgroundColor: "{colors.success-container}"
    textColor: "{colors.on-success-container}"
    rounded: "{rounded.sm}"
    padding: "{spacing.sm}"
  requirement-item-pending:
    backgroundColor: "{colors.surface-container}"
    textColor: "{colors.on-surface-variant}"
    rounded: "{rounded.sm}"
    padding: "{spacing.sm}"
  stepper-step-done:
    backgroundColor: "{colors.success}"
    textColor: "{colors.on-success}"
    rounded: "{rounded.full}"
    size: 28px
  stepper-step-active:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.full}"
    size: 28px
  stepper-step-pending:
    backgroundColor: "{colors.surface-container-high}"
    textColor: "{colors.on-surface-variant}"
    rounded: "{rounded.full}"
    size: 28px
  progress-bar-track:
    backgroundColor: "{colors.surface-container-high}"
    rounded: "{rounded.full}"
    height: 8px
  progress-bar-fill:
    backgroundColor: "{colors.primary}"
    rounded: "{rounded.full}"
  badge-pending:
    backgroundColor: "{colors.surface-container-high}"
    textColor: "{colors.on-surface-variant}"
    typography: "{typography.label-caps}"
    rounded: "{rounded.full}"
    padding: 4px 10px
  badge-in-progress:
    backgroundColor: "{colors.info-container}"
    textColor: "{colors.on-info-container}"
    typography: "{typography.label-caps}"
    rounded: "{rounded.full}"
    padding: 4px 10px
  badge-under-review:
    backgroundColor: "{colors.warning-container}"
    textColor: "{colors.on-warning-container}"
    typography: "{typography.label-caps}"
    rounded: "{rounded.full}"
    padding: 4px 10px
  badge-approved:
    backgroundColor: "{colors.success-container}"
    textColor: "{colors.on-success-container}"
    typography: "{typography.label-caps}"
    rounded: "{rounded.full}"
    padding: 4px 10px
  badge-rejected:
    backgroundColor: "{colors.danger-container}"
    textColor: "{colors.on-danger-container}"
    typography: "{typography.label-caps}"
    rounded: "{rounded.full}"
    padding: 4px 10px
  badge-external-source:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    typography: "{typography.label-caps}"
    rounded: "{rounded.sm}"
    padding: 4px 10px
  toast-success:
    backgroundColor: "{colors.success-container}"
    textColor: "{colors.on-success-container}"
    rounded: "{rounded.md}"
    padding: "{spacing.sm}"
  toast-error:
    backgroundColor: "{colors.danger-container}"
    textColor: "{colors.on-danger-container}"
    rounded: "{rounded.md}"
    padding: "{spacing.sm}"
  toast-info:
    backgroundColor: "{colors.info-container}"
    textColor: "{colors.on-info-container}"
    rounded: "{rounded.md}"
    padding: "{spacing.sm}"
  dialog-surface:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.lg}"
    padding: "{spacing.lg}"
  dialog-overlay:
    backgroundColor: "rgba(36, 28, 22, 0.45)"
  nav-mobile-bar:
    backgroundColor: "{colors.surface}"
    height: 64px
  nav-mobile-item-active:
    textColor: "{colors.primary}"
  nav-mobile-item:
    textColor: "{colors.on-surface-variant}"
  sidebar-desktop:
    backgroundColor: "{colors.surface-container-low}"
    width: 240px
  sidebar-item-active:
    backgroundColor: "{colors.primary-container}"
    textColor: "{colors.on-primary-container}"
    rounded: "{rounded.sm}"
    padding: 10px 12px
  sidebar-item:
    backgroundColor: transparent
    textColor: "{colors.on-surface-variant}"
    rounded: "{rounded.sm}"
    padding: 10px 12px
  table-row:
    backgroundColor: "{colors.surface}"
    padding: "{spacing.sm}"
  table-row-hover:
    backgroundColor: "{colors.surface-container-low}"
  table-header:
    backgroundColor: "{colors.surface-container-low}"
    textColor: "{colors.on-surface-variant}"
    typography: "{typography.label-caps}"
  divider:
    backgroundColor: "{colors.outline-variant}"
    height: 1px
  skeleton:
    backgroundColor: "{colors.surface-container-high}"
    rounded: "{rounded.sm}"
  empty-state:
    backgroundColor: transparent
    textColor: "{colors.on-surface-variant}"
    typography: "{typography.body-md}"
    padding: "{spacing.xxl}"
  case-card-dark:
    backgroundColor: "{colors.surface-dark}"
    rounded: "{rounded.lg}"
    padding: "{spacing.md}"
  document-row-dark:
    backgroundColor: "{colors.surface-container-dark}"
    rounded: "{rounded.sm}"
    padding: "{spacing.sm}"
  divider-dark:
    backgroundColor: "{colors.outline-dark}"
    height: 1px
  nav-mobile-bar-dark:
    backgroundColor: "{colors.surface-dark}"
    height: 64px
  page:
    backgroundColor: "{colors.background}"
    textColor: "{colors.on-surface}"
  page-dark:
    backgroundColor: "{colors.background-dark}"
    textColor: "{colors.on-surface-dark}"
  nav-mobile-item-dark:
    textColor: "{colors.on-surface-variant-dark}"
  sidebar-desktop-dark:
    backgroundColor: "{colors.surface-container-dark}"
    width: 240px
  table-row-dark:
    backgroundColor: "{colors.surface-dark}"
    padding: "{spacing.sm}"
---

## Overview

PILO no es una web gubernamental fría ni un SaaS pulido al extremo de la
elegancia. Es un punto medio deliberado: **cálido, humano, directo** —
un sistema que le simplifica la vida a alguien que llega con un trámite
pendiente, sin caer en la distancia de un producto premium. La
personalidad se apoya en un único color de marca (terracota) sobre
superficies neutras cálidas; todo lo demás — verde, ámbar, rojo, azul —
es semántica de estado que refleja literalmente los `enum` del backend
(`ProcedureCase.status`, `Document.status`), no decoración libre.

**Referencia de tono:** el punto de partida es el registro de **Cal.com**
— blanco/hueso limpio, un único color de acción casi monocromático,
tarjetas suaves sin bordes duros, tipografía display con carácter propio
pero contenida — combinado con el trato "tarjeta suave con dato real
adentro" de **Notion** para los `case-card`. A diferencia de Cal.com,
PILO SÍ tiene un vocabulario de color de estado rico (porque el dominio
lo exige: un expediente puede estar aprobado, rechazado o en revisión, y
eso tiene que leerse de un vistazo), y a diferencia de Notion, PILO NO
usa una paleta decorativa amplia de tintes pastel — el color siempre
significa algo del backend, nunca es libre elección de categorización
del usuario.

**Características clave:**
- Blanco/hueso cálido (`{colors.background}` #FAF6F1) con tarjetas en
  blanco puro (`{colors.surface}` #FFFFFF) — nunca gris frío de fondo.
- Terracota (`{colors.primary}` #B5551E) reservado estrictamente a
  acción: `button-primary`, el paso activo del stepper, focus rings.
- Badges de estado siempre en su versión "container" (fondo suave +
  texto saturado), nunca sólidos — ver tabla de mapeo en Iconografía.
- Sombras mínimas y de baja opacidad (ver Elevación) — el sistema nunca
  usa sombras duras ni glassmorphism.
- Radio de esquina jerárquico: `sm` (8px) botones/inputs, `lg` (16px)
  tarjetas, `full` badges/stepper/avatares.
- Dark mode real (no inversión automática) — cada rol de color tiene su
  par `-dark` explícito.

## Colors

El sistema sigue un patrón de **roles tonales** (base / on-base /
container / on-container) en vez de colores planos, porque cada color
necesita funcionar en al menos dos contextos: fondo sólido (botón,
badge lleno) y superficie suave con texto encima (chip, tarjeta de
estado). Esto es también lo que permite que el sistema escale a dark
mode sin inventar reglas nuevas — cada rol tiene su reemplazo `-dark`
directo.

### Marca y acción
- **Primary** (`{colors.primary}` #B5551E): único color de marca. Vive
  en `button-primary`, el paso activo del stepper, y el borde de focus
  de inputs. Nunca es fondo de página ni de tarjeta grande. Hover
  `{colors.primary-hover}` #9E481D, active/press `{colors.primary-active}`
  #8A3F19, disabled `{colors.primary-disabled}` #E8DED2.
- **Secondary** (`{colors.secondary}` #6B6259): taupe cálido para texto
  secundario, metadatos, navegación inactiva, y el badge de fuente
  externa (BOE / datos.gob.es).

### Semántica de estado (mapea 1:1 al backend)
- **Success** → `ProcedureCase.status = APPROVED`, `Document.status = VALIDATED`.
- **Warning** → `ProcedureCase.status = UNDER_REVIEW` con deadline
  próximo, o requisito pendiente cerca de vencer.
- **Danger** → `ProcedureCase.status = REJECTED`, `Document.status = REJECTED`.
- **Info** → `ProcedureCase.status = IN_PROGRESS`, `Document.status = PROCESSING`,
  y referencias externas (BOE/datos.gob.es) cuando no son el badge
  `external-source` dedicado.
Cada uno existe en versión sólida (fondo saturado + texto blanco, solo
para botones/toasts de alta prioridad) y versión `container` (fondo
suave + texto saturado, para badges — el 95% de los usos).

### Superficies
Cinco niveles (`surface` → `surface-container-highest`) permiten apilar
tarjetas dentro de tarjetas (ej. un `document-row` dentro de un
`case-card`) sin bordes — la jerarquía se lee por tono, no por líneas.
`outline` y `outline-variant` son los únicos dos grises reservados para
bordes reales (inputs, `divider`) — nunca para texto.

### Dark mode
No es una inversión automática. `background-dark` (#1C1712) es un
marrón-carbón cálido — nunca negro puro ni gris-azulado frío — para
mantener la misma sensación humana en ambos temas. Cada componente que
lo necesita tiene su entrada `-dark` explícita en `components:` (ej.
`case-card` → `case-card-dark`).

## Typography

| Token | Tamaño | Peso | Line-height | Tracking | Uso |
|---|---|---|---|---|---|
| `{typography.display}` | 36px | 700 | 1.15 | -0.01em | Título de sección, encabezado de página principal |
| `{typography.headline-lg}` | 24px | 600 | 1.25 | — | Título de `case-card` expandida, encabezado de modal |
| `{typography.headline-md}` | 20px | 600 | 1.3 | — | Subtítulos de sección |
| `{typography.title-md}` | 17px | 600 | 1.35 | — | Título de card, nombre de trámite |
| `{typography.title-sm}` | 15px | 600 | 1.35 | — | Título de fila (`document-row`, `table-row`) |
| `{typography.body-lg}` | 17px | 400 | 1.55 | — | Texto introductorio, descripciones largas |
| `{typography.body-md}` | 16px | 400 | 1.55 | — | Texto de cuerpo por defecto, inputs |
| `{typography.body-sm}` | 14px | 400 | 1.5 | — | Texto secundario, ayuda de formulario |
| `{typography.label-md}` | 14px | 600 | 1.3 | — | Botones, labels de input |
| `{typography.label-caps}` | 11px | 700 | 1.0 | 0.06em | Badges de estado (mayúsculas) |
| `{typography.code}` | 13px | 500 | 1.4 | — | `case_number`, referencias externas, IDs copiables |

Dos familias humanistas sin serifas: **Plus Jakarta Sans** (`display`
hasta `title-sm`) para titulares — carácter cálido sin ser decorativa —
e **Inter** (`body-*` hasta `label-caps`) para cuerpo y formularios —
máxima legibilidad en listas de expedientes y requisitos. **JetBrains
Mono** solo para `{typography.code}`: números de expediente y
referencias externas se leen y copian mejor en monoespaciada. Pesos
entre 400 y 700 únicamente — se evita deliberadamente el 300, asociado
a la "elegancia fintech" que este sistema busca no transmitir. Nunca
mezclar una cuarta familia.

## Layout

Mobile-first real: el diseño parte de un viewport de 360–390px y escala
hacia arriba con los breakpoints por defecto de Tailwind (`sm` 640,
`md` 768, `lg` 1024, `xl` 1280), nunca al revés.

### Espaciado
Base 4/8px: `{spacing.xxs}` 4px · `{spacing.xs}` 8px · `{spacing.sm}`
12px · `{spacing.md}` 16px · `{spacing.lg}` 24px · `{spacing.xl}` 32px
· `{spacing.xxl}` 48px · `{spacing.section}` 64px.

### Grid y contenedor
- Mobile: una columna, ancho 100% con padding horizontal `{spacing.md}`.
- `lg+`: aparece `sidebar-desktop` (240px fijo) + contenido con
  `max-width` ~960px centrado en el área restante.
- Listas de `case-card`: 1 columna en mobile, 2 en `md`, puede migrar a
  `table-row` en `lg+` para vistas ADMIN/REVIEWER con muchos casos.
- Padding interno de `case-card`: `{spacing.md}` (16px). Padding de
  `dialog-surface`: `{spacing.lg}` (24px).

### Filosofía de whitespace
Generoso pero no editorial — PILO no es una landing de marketing, es
una herramienta de trabajo. El espaciado entre secciones usa
`{spacing.section}` (64px, no 96px como un sitio de marketing) porque
el usuario necesita escanear información operativa rápido, no
"respirar" entre bloques decorativos.

## Elevation & Depth

| Nivel | Valor CSS | Uso |
|---|---|---|
| Flat | sin sombra | `page`, `nav-mobile-bar`, `sidebar-desktop`, filas de tabla |
| Card | `{elevation.card}` | `case-card`, `dialog-surface` en reposo |
| Elevated | `{elevation.elevated}` | Card en hover/drag (ej. `file-dropzone-active`), dropdown abierto |
| Overlay | `{elevation.overlay}` | `dialog-surface` sobre `dialog-overlay`, popover |

En dark mode las sombras casi no se perciben sobre fondos oscuros, así
que la elevación se refuerza subiendo un nivel de superficie
(`surface-container-dark` → `surface-container-high-dark`) además de
aplicar `{elevation.card-dark}` / `{elevation.elevated-dark}`. El
`dialog-overlay` usa `rgba(36, 28, 22, 0.45)` en claro y
`rgba(0, 0, 0, 0.6)` en oscuro.

## Shapes

| Token | Valor | Uso |
|---|---|---|
| `{rounded.xs}` | 4px | Casi sin uso — detalles de badge internos |
| `{rounded.sm}` | 8px | `input-field`, `document-row`, `sidebar-item` |
| `{rounded.md}` | 12px | `button-primary`/`button-secondary`, `file-dropzone`, `dialog-surface` chico |
| `{rounded.lg}` | 16px | `case-card`, `dialog-surface` |
| `{rounded.xl}` | 24px | Reservado para contenedores hero/onboarding, uso escaso |
| `{rounded.full}` | 9999px | `badge-*`, `stepper-step-*`, `button-icon`, `progress-bar` |

Esquinas redondeadas moderadas: suficiente para sentirse amigable, no
tanto como para leerse infantil. Los badges y el stepper usan `full`
para leerse como "píldoras" de estado, visualmente distintos de los
botones rectangulares de acción.

## Components

### Botones e inputs
`button-primary` es la única acción sólida en terracota (subir
documento, avanzar expediente, confirmar). `button-secondary` (fondo
neutro, sin borde visible salvo `outline-variant` heredado del
`input-field` vecino) es para acciones de segundo orden ("Ver
detalles", "Cancelar"). `button-text-link` solo para navegación
inline dentro de párrafos. `input-field` y `file-dropzone` comparten
radio `sm`/`md` para leerse como una sola unidad de formulario.
`file-dropzone-active` (drag-over) es el único momento donde terracota
aparece como fondo grande — comunica "esto va a convertirse en tuyo
cuando sueltes". `file-dropzone-rejected` (tipo/tamaño de archivo
inválido) usa `danger-container`, nunca el `danger` sólido.

### Expedientes y documentos
`case-card` es el contenedor raíz de un `ProcedureCase`: número de
expediente en `{typography.code}`, tipo de trámite, `progress-bar`,
badge de estado. `document-row` vive dentro de la tarjeta o en lista
independiente, con su propio badge según `Document.status`
(`UPLOADED`→`badge-pending`, `PROCESSING`→`badge-in-progress`,
`VALIDATED`→`badge-approved`, `REJECTED`→`badge-rejected`).
`requirement-item-done`/`-pending` representan cada `Requirement` — el
ítem nunca desaparece de la lista, solo cambia de superficie.

### Workflow
`stepper-step-done`/`-active`/`-pending` representan cada
`WorkflowTask`. Vertical en mobile (un paso por fila con descripción a
la derecha); horizontal en `lg+` cuando hay 4 pasos o menos, vertical
si hay más. `progress-bar-fill` sobre `progress-bar-track` resume
`progress_percentage` en la cabecera de `case-card`.

### Estados y feedback
Los `badge-*` son la pieza más repetida del sistema — deben verse
idénticos en cualquier contexto. `toast-success`/`-error`/`-info` son
notificaciones efímeras (Sonner, ver Librerías); nunca usan la versión
sólida del color. `empty-state` (sin sombra, texto `on-surface-variant`,
padding `xxl`) se usa cuando una lista de expedientes/documentos está
vacía — siempre acompañado de un ícono de `{icons}` y una acción
primaria si aplica. `skeleton` reemplaza contenido mientras carga
(nunca un spinner de página completa salvo en la carga inicial de
autenticación).

### Navegación
`nav-mobile-bar` (64px, fija abajo) por defecto en mobile: Expedientes,
Documentos, Notificaciones, Perfil — con íconos de `{icons.nav-*}`.
`nav-mobile-item-active` usa `{colors.primary}`; el resto,
`{colors.on-surface-variant}`. A partir de `lg`, `sidebar-desktop`
(240px) reemplaza la barra inferior; `sidebar-item-active` usa
`primary-container` como fondo, no el terracota sólido.

### Vistas administrativas
`table-row`/`table-row-hover`/`table-header` solo en `lg+` para
ADMIN/REVIEWER gestionando muchos expedientes o tipos de trámite. En
mobile esa misma información siempre se degrada a `case-card`, nunca a
tabla con scroll horizontal.

### Diálogos y overlays
`dialog-surface` sobre `dialog-overlay` (Radix Dialog, ver Librerías)
para revisión de documento, confirmación de rechazo, detalle de
requisito. Radio `lg`, sombra `{elevation.overlay}`, nunca fullscreen
en mobile salvo formularios largos (creación de expediente).

## Do's and Don'ts

### Hacer
- Usar terracota solo para acción/interacción — nunca fondo de página
  ni de tarjeta grande.
- Badges de estado siempre en versión `container` — la sólida es solo
  para botones y toasts.
- Degradar tablas a `case-card` apiladas por debajo de `lg` — nunca
  scroll horizontal en mobile.
- Mantener el mismo tono cálido en dark mode (marrón-carbón, no negro
  ni gris azulado frío).
- Usar `{typography.code}` para cualquier identificador copiable
  (número de expediente, referencia externa).
- Acompañar cada badge de estado con su ícono correspondiente de
  `{icons.status-*}} — nunca solo color, por accesibilidad (daltonismo).

### No hacer
- No introducir gradientes, glassmorphism ni sombras duras — solo los
  tres niveles de `elevation` definidos arriba.
- No usar azul o verde como color de marca — reservados a `info` y
  `success`.
- No usar pesos tipográficos por debajo de 400.
- No mezclar una cuarta familia tipográfica.
- No construir un Dialog, Toast, Dropdown o Tooltip desde cero — usar
  los primitivos Radix vía shadcn/ui (ver Librerías); reinventar estos
  componentes rompe accesibilidad de teclado gratis.
- No usar un ícono fuera de `lucide-react` en ningún punto del producto.
- No animar nada por encima de `{motion.page}` (250ms) — si algo
  necesita más tiempo, se corta o se reemplaza por un `skeleton`.

## Iconography

Librería única: **lucide-react** — trazo humanista, esquinas
levemente redondeadas, coherente con `rounded.sm`/`rounded.md` del
resto del sistema. Grosor de trazo fijo `{icons.strokeWidth}` (1.75) en
todos los tamaños. Tamaños: `{icons.size-sm}` 16px (dentro de badges),
`{icons.size-md}` 20px (botones, filas), `{icons.size-lg}` 24px
(encabezados, estados vacíos). Nunca mezclar con Font Awesome,
Heroicons ni SVGs custom.

| Contexto | Ícono | Componente donde aparece |
|---|---|---|
| Estado pendiente | `Clock` | `badge-pending` |
| Estado en progreso | `Loader2` (animado, spin) | `badge-in-progress` |
| Estado en revisión | `AlertTriangle` | `badge-under-review` |
| Estado aprobado | `CheckCircle2` | `badge-approved` |
| Estado rechazado | `XCircle` | `badge-rejected` |
| Subir documento | `UploadCloud` | `file-dropzone` |
| Documento | `FileText` | `document-row`, nav |
| Expediente | `FolderOpen` | `case-card`, nav |
| Notificación | `Bell` | `nav-mobile-item` (notificaciones) |
| Fuente externa | `Landmark` | `badge-external-source` |
| Deadline | `CalendarClock` | header de `case-card` con fecha próxima |
| Auditoría | `History` | vista de `AuditEvent` |
| Perfil | `User` | nav |

`Loader2` es el único ícono con animación (`spin`, `{motion.content}`
por vuelta) — el resto es siempre estático.

## Motion

Transiciones cortas y mecánicas — nada rebota, nada hace overshoot.
Un cambio de estado debe sentirse como un interruptor de luz, no como
una puerta cerrándose despacio.

- Feedback de interacción (hover, press, toggle de badge): `{motion.feedback}`
  (120ms), siempre `{motion.easing}`.
- Transiciones de contenido (acordeón de requisito, dropdown, toast
  entrando/saliendo): `{motion.content}` (200ms).
- Transiciones de página/panel completo (abrir `dialog-surface`,
  navegación entre secciones): `{motion.page}` (250ms) — techo del
  sistema, nada dura más.
- Respetar `prefers-reduced-motion`: todas las duraciones colapsan a 0ms.
- El `progress-bar-fill` es la única animación "continua" permitida
  (transición de ancho al cambiar `progress_percentage`), con
  `{motion.content}`.

## Libraries

Elegidas para minimizar ambigüedad de implementación y bundle size —
un agente de código no debe evaluar alternativas, solo instalar e
implementar con las que siguen:

| Necesidad | Librería | Por qué esta y no otra |
|---|---|---|
| Primitivos de UI accesibles (Dialog, DropdownMenu, Tabs, Tooltip, Popover, Toast) | **shadcn/ui** sobre **Radix UI** | Se copian como código propio (no es una dependencia de runtime pesada), integra directo con las variables CSS de este documento vía Tailwind, y da accesibilidad de teclado/foco gratis — crítico en un sistema de trámites donde REVIEWER/ADMIN operan mucho por teclado |
| Íconos | **lucide-react** | Trazo humanista consistente con la personalidad "cálido, sin elegancia"; es el set por defecto de shadcn, cero fricción de integración |
| Formularios | **react-hook-form** + **zod** + `@hookform/resolvers` | Ya definido en el stack de PILO (`22. Resumen de Stack`); valida en el cliente con el mismo esquema mental que las `validation_rules` JSONB del backend |
| Subida de archivos | **react-dropzone** | Encaja directo con el flujo de presigned URL a S3 descrito en el documento técnico — maneja drag-and-drop, validación de MIME/tamaño antes de pedir la URL firmada, y expone los estados que mapean a `file-dropzone-active`/`-rejected` |
| Notificaciones toast | **sonner** | Librería estándar del ecosistema shadcn, minimalista, sin animaciones excesivas — encaja con la política de motion del sistema |
| Fechas y deadlines | **date-fns** | Liviana (tree-shakeable), suficiente para countdown de `deadline_at` y formateo de fechas de documentos; evita el peso de moment.js/dayjs con plugins |
| Fetching y cache de datos | **TanStack Query** | Ya definido en el stack; maneja el polling necesario para `Document.status` mientras el pipeline async de IA procesa |
| Utilidad de clases | **clsx** + **tailwind-merge** (patrón `cn()`) | Estándar de facto junto a shadcn/ui, evita conflictos de clases Tailwind condicionales |
| Animación | **Ninguna librería adicional** — solo `transition` de Tailwind + las utilidades de Radix/shadcn | Framer Motion queda deliberadamente fuera: el sistema de motion de PILO es corto y mecánico (120–250ms), no necesita una librería de física de resortes |
| Gráficos (solo dashboard ADMIN, opcional) | **Recharts** | Si se construye un dashboard de métricas de trámites, es la opción con mejor integración React + Tailwind; no se usa en ninguna vista de USER |
| Fuentes | **`next/font/google`** | Self-hosted, cero layout shift, expone `--font-jakarta`/`--font-inter`/`--font-mono` como variables CSS que mapean directo a `typography.fontFamily` de este documento |

### Explícitamente fuera de alcance
- **Framer Motion**, **GSAP**: contradicen la política de motion corto
  y mecánico.
- **Font Awesome**, **Heroicons**, **react-icons**: solo `lucide-react`.
- **Material UI**, **Ant Design**, **Chakra**: PILO usa primitivos
  Radix sin sistema de theming propio — este documento ES el sistema
  de theming.
- **Moment.js**: reemplazado por `date-fns`.

## Next.js Integration

- **Fuentes:** cargar Plus Jakarta Sans, Inter y JetBrains Mono con
  `next/font/google` en `app/layout.tsx`, cada una expuesta como
  variable CSS (`--font-jakarta`, `--font-inter`, `--font-mono`) sobre
  el `<html>` raíz. Los tokens `fontFamily` de este documento deben
  referenciar esas variables, nunca un string de Google Fonts directo.
- **Tokens → Tailwind:** las variables de `colors`, `rounded` y
  `spacing` de este documento deben vivir como `@theme` de Tailwind v4
  en `app/globals.css` (`--color-primary`, `--radius-md`,
  `--spacing-md`, etc.) — un componente nuevo nunca escribe un hex o
  un px suelto, siempre referencia la variable.
- **Dark mode:** estrategia por clase (`@custom-variant dark` en
  Tailwind v4) controlada con `next-themes`. Cuando `<html>` tiene la
  clase `dark`, los componentes resuelven a su entrada `-dark` en
  `components:` (ej. `case-card` → `case-card-dark`) — nunca un filtro
  CSS de inversión.
- **Server vs Client:** los tokens son CSS estático y viven bien en
  Server Components. Client Components solo donde hay estado real:
  toggle de tema, `file-dropzone` (drag events), `dialog-surface`
  (Radix), polling de `Document.status` vía TanStack Query.
- **Estructura sugerida:** `components/ui/` para los primitivos
  shadcn sin modificar; `components/pilo/` para los componentes de
  dominio de este documento (`CaseCard`, `DocumentRow`,
  `WorkflowStepper`, `StatusBadge`) que sí implementan directamente
  las entradas de `components:` de arriba.

## Responsive Behavior

### Breakpoints (Tailwind defaults)

| Nombre | Ancho | Cambios clave |
|---|---|---|
| Mobile | < 640px | `nav-mobile-bar` fija abajo; una columna; `case-card` apiladas; stepper vertical |
| Tablet | 640–1023px | Una columna de contenido con más padding lateral; grids de 2 columnas en catálogos |
| Desktop | ≥ 1024px (`lg`) | Aparece `sidebar-desktop`; listas ADMIN/REVIEWER pueden usar `table-row`; stepper puede ir horizontal si hay ≤4 pasos |
| Wide | ≥ 1280px (`xl`) | Contenido centrado con `max-width`, sin cambios estructurales |

### Touch targets
- `button-primary`/`button-secondary`: 48px de alto mínimo.
- `input-field`/`select-field`/`file-dropzone` (zona clickeable): 48px
  de alto mínimo.
- `button-icon`: 44px, círculo completo.
- Filas de `nav-mobile-bar`: área táctil completa de 64px de alto, no
  solo el ícono.

### Estrategia de colapso
- `sidebar-desktop` ↔ `nav-mobile-bar`: se reemplazan por completo en
  `lg`, nunca coexisten.
- `table-row` (admin) colapsa a `case-card` por debajo de `lg` —
  mismos datos, presentación distinta, nunca scroll horizontal.
- El stepper pasa de vertical (mobile) a horizontal (desktop, ≤4
  pasos) o se mantiene vertical si hay más de 4.
- `dialog-surface` es modal centrado en `md+`; en mobile ocupa toda la
  pantalla solo si el formulario tiene más de un paso (ej. creación de
  expediente), si no, se mantiene como bottom-sheet centrado.

## Iteration Guide

1. Trabajar un componente a la vez, referenciando su clave YAML directa
   (`{component.case-card}`, `{component.badge-approved}`).
2. Las variantes de estado (`-hover`, `-active`, `-disabled`, `-dark`)
   son entradas separadas en `components:` — nunca lógica condicional
   de estilos dentro del componente React.
3. Usar siempre `{token.refs}` en componentes nuevos, nunca hex/px
   inline — así `design.md diff` detecta regresiones reales.
4. Antes de dar por terminado un componente, correr
   `npx @google/design.md lint DESIGN.md` y resolver cualquier
   `contrast-ratio` antes de mergear.
5. Todo componente interactivo nuevo empieza como primitivo Radix
   (vía shadcn/ui) estilizado con los tokens de este documento — nunca
   se construye desde cero un Dialog/Dropdown/Toast/Tooltip.
6. Todo ícono nuevo sale de la tabla en **Iconography**; si el caso de
   uso no está en la tabla, se añade ahí antes de usarse en código.
7. Ningún valor de `transition`/`duration` en código debe superar
   `{motion.page}` (250ms).

## Known Gaps

- Los estados de `input-field` cubren focus y error; falta un estado
  de éxito/validado en línea, pendiente de decidir si aplica al
  formulario de creación de expediente.
- El linter reporta `contrast-ratio` en `button-text-link` porque
  evalúa fondos `transparent` contra negro puro, no contra la
  superficie clara real donde vive el componente. Contra blanco,
  `{colors.primary-hover}` da 6.18:1 — se deja así a propósito; es una
  limitación conocida del linter, no un problema real de accesibilidad.
- `Recharts` está listado como opcional porque todavía no existe una
  vista de dashboard ADMIN definida en el alcance del MVP — si el
  scope crece en esa dirección, este documento debe actualizarse con
  tokens de `chart-*` específicos antes de implementarla.
- No se han definido tokens de impresión (PDF de expediente, resumen
  imprimible) — fuera de alcance del MVP.