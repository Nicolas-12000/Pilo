import { z } from "zod";

export const loginSchema = z.object({
  email: z.email("Introduce un correo válido."),
  password: z.string().min(1, "La contraseña es obligatoria."),
});

export type LoginValues = z.infer<typeof loginSchema>;
