"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useState } from "react";
import { useForm } from "react-hook-form";
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
    <form className="flex flex-col gap-4" onSubmit={handleSubmit(onSubmit)} noValidate>
      <label className="flex flex-col gap-2">
        <span className="font-label text-on-surface">Correo electrónico</span>
        <input
          type="email"
          autoComplete="email"
          className="h-12 rounded-sm bg-surface px-3.5 text-body-md text-on-surface outline-none ring-1 ring-outline-variant focus:ring-2 focus:ring-primary"
          {...register("email")}
        />
        {errors.email ? (
          <span className="text-body-sm text-danger">{errors.email.message}</span>
        ) : null}
      </label>

      <label className="flex flex-col gap-2">
        <span className="font-label text-on-surface">Contraseña</span>
        <input
          type="password"
          autoComplete="current-password"
          className="h-12 rounded-sm bg-surface px-3.5 text-body-md text-on-surface outline-none ring-1 ring-outline-variant focus:ring-2 focus:ring-primary"
          {...register("password")}
        />
        {errors.password ? (
          <span className="text-body-sm text-danger">{errors.password.message}</span>
        ) : null}
      </label>

      {formError ? (
        <p className="rounded-sm bg-danger-container px-3.5 py-3 text-body-sm text-on-danger-container">
          {formError}
        </p>
      ) : null}

      <button
        type="submit"
        disabled={isSubmitting}
        className="h-12 rounded-md bg-primary px-5 font-label text-on-primary transition-colors duration-feedback enabled:hover:bg-primary-hover enabled:active:bg-primary-active disabled:bg-primary-disabled disabled:text-on-primary-disabled"
      >
        {isSubmitting ? "Entrando…" : "Entrar"}
      </button>
    </form>
  );
}
