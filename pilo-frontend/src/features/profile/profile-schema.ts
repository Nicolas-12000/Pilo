import { z } from "zod";

export const profileNameSchema = z.object({
  fullName: z.string().trim().min(2, "Introduce un nombre válido.").max(120),
});

export type ProfileNameValues = z.infer<typeof profileNameSchema>;

export const changePasswordSchema = z
  .object({
    currentPassword: z.string().min(1, "Introduce tu contraseña actual."),
    newPassword: z.string().min(8, "La nueva contraseña debe tener al menos 8 caracteres."),
    confirmPassword: z.string().min(1, "Confirma la nueva contraseña."),
  })
  .refine((values) => values.newPassword === values.confirmPassword, {
    message: "Las contraseñas no coinciden.",
    path: ["confirmPassword"],
  });

export type ChangePasswordValues = z.infer<typeof changePasswordSchema>;
