# Publicar 3 repos desde el monorepo

Desarrollo en **[Pilo](https://github.com/Nicolas-12000/Pilo)** (monorepo). Para la entrega académica (mín. 2 repos; aquí **3**):

| Repo público | Carpeta en monorepo | Contenido |
|--------------|---------------------|-----------|
| `pilo-backend` | `pilo-backend/` | API Spring Boot |
| `pilo-frontend` | `pilo-frontend/` | Next.js |
| `pilo-worker` | `pilo-worker/` | Lambda Python |

El **historial de commits** de cada carpeta se conserva en su repo hijo (`git subtree split`). El monorepo no se modifica.

## 1. Crear repos vacíos en GitHub

### Opción A — Web (sin instalar nada)

En [github.com/new](https://github.com/new), creá **tres** repos **públicos**, **sin** README, **sin** `.gitignore`, **sin** licencia (vacíos):

| Nombre sugerido | Descripción |
|-----------------|-------------|
| `pilo-backend` | PILO — Spring Boot API |
| `pilo-frontend` | PILO — Next.js UI |
| `pilo-worker` | PILO — Lambda worker |

URLs quedarán como `https://github.com/Nicolas-12000/pilo-backend.git`, etc.

### Opción B — GitHub CLI

```bash
sudo apt install gh   # o: sudo snap install gh
gh auth login
gh repo create Nicolas-12000/pilo-backend --public --description "PILO — Spring Boot API"
gh repo create Nicolas-12000/pilo-frontend --public --description "PILO — Next.js UI"
gh repo create Nicolas-12000/pilo-worker --public --description "PILO — Lambda worker"
```

(Ajusta org/usuario si hace falta.)

## 2. Configurar URLs

```bash
cp scripts/subtree-remotes.example.env scripts/subtree-remotes.env
# editar las tres URLs
```

## 3. Publicar

**Antes:** `git status` limpio (commit o stash de cambios locales).

```bash
chmod +x scripts/publish-subtree-repos.sh
./scripts/publish-subtree-repos.sh          # push a los 3 remotos
./scripts/publish-subtree-repos.sh --dry-run   # solo muestra comandos
```

El script:

- Registra remotos `pilo-backend`, `pilo-frontend`, `pilo-worker`
- Crea ramas locales `publish/pilo-backend`, etc.
- Hace push a `main` de cada repo hijo

## 4. Qué decir en la defensa

> Desarrollamos en monorepo para alinear API y UI; los repos de entrega se generan con **git subtree split**, que mantiene el historial de cada componente.

## 5. Actualizar después de más commits en el monorepo

Mismo comando: `./scripts/publish-subtree-repos.sh` (vuelve a hacer split y push).

## Notas

- CI del monorepo (`.github/workflows/ci.yml`) **no** viaja al split; cada repo hijo solo trae lo de su carpeta. Si el profe pide Actions en cada repo, añade un workflow dentro de `pilo-backend/`, `pilo-frontend/` o `pilo-worker/` en el monorepo y repite el publish.
- `docker-compose.ec2.yml` vive en la raíz del monorepo; no está en el repo del backend solo (puedes copiarlo al README del backend o dejarlo en el monorepo).
