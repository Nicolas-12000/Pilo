export const DEMO_PASSWORD = "Password123!";

/**
 * Demo account shortcuts are for local/dev exploration only. Gated by an explicit env flag
 * (rather than just NODE_ENV) so a misconfigured "production-like" preview/staging deploy
 * doesn't silently show shared demo credentials — it must be turned on on purpose.
 */
export const isDemoLoginEnabled = process.env.NEXT_PUBLIC_ENABLE_DEMO_LOGIN === "true";

export const demoAccounts = [
  {
    role: "Ciudadano",
    email: "user@pilo.test",
    description: "Abrir expedientes y subir documentos",
  },
  {
    role: "Revisor",
    email: "reviewer@pilo.test",
    description: "Revisar y resolver trámites",
  },
  {
    role: "Admin",
    email: "admin@pilo.test",
    description: "Configurar tipos de trámite",
  },
] as const;
