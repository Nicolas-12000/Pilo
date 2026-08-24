"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { Loader2 } from "lucide-react";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { Button } from "@/components/ui/button";
import { FieldError, Input, Label } from "@/components/ui/field";
import { ApiError, login } from "@/lib/api/auth";
import { persistSession } from "@/lib/auth/session";
import { loginSchema, type LoginValues } from "@/lib/auth/login-schema";

type LoginFormProps = {
  onSuccess?: () => void;
};

export function LoginForm({ onSuccess }: LoginFormProps) {
  const [formError, setFormError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
  });

  async function onSubmit(values: LoginValues) {
    setFormError(null);
    try {
      const session = await login(values);
      persistSession(session);
      onSuccess?.();
    } catch (error) {
      if (error instanceof ApiError) {
        setFormError(error.message);
        return;
      }
      setFormError("No se ha podido iniciar sesión.");
    }
  }

  return (
    <form className="flex flex-col gap-md" onSubmit={handleSubmit(onSubmit)} noValidate>
      <label className="flex flex-col gap-xs">
        <Label>Correo electrónico</Label>
        <Input
          type="email"
          autoComplete="email"
          aria-invalid={errors.email ? true : undefined}
          {...register("email")}
        />
        {errors.email ? <FieldError>{errors.email.message}</FieldError> : null}
      </label>

      <label className="flex flex-col gap-xs">
        <Label>Contraseña</Label>
        <Input
          type="password"
          autoComplete="current-password"
          aria-invalid={errors.password ? true : undefined}
          {...register("password")}
        />
        {errors.password ? <FieldError>{errors.password.message}</FieldError> : null}
      </label>

      {formError ? (
        <p
          role="alert"
          className="rounded-sm bg-danger-container px-3.5 py-3 text-body-sm text-on-danger-container"
        >
          {formError}
        </p>
      ) : null}

      <Button type="submit" disabled={isSubmitting} className="mt-xs">
        {isSubmitting ? (
          <>
            <Loader2 size={20} strokeWidth={1.75} className="animate-spin" />
            Entrando…
          </>
        ) : (
          "Entrar"
        )}
      </Button>
    </form>
  );
}
