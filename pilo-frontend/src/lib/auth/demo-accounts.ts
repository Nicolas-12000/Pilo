export const DEMO_PASSWORD = "Password123!";

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
