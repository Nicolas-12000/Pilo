# PILO — Frontend

Next.js (App Router), TypeScript, UI en español. Habla con el API PILO vía `NEXT_PUBLIC_API_URL`.

Arquitectura completa: [monorepo PILO](https://github.com/Nicolas-12000/Pilo).

## Run locally

Node 22, pnpm 11. API en `http://localhost:8080`.

```bash
cp .env.example .env.local
pnpm install
pnpm dev
```

App: `http://localhost:3000`

```bash
pnpm lint && pnpm test && pnpm build
```
