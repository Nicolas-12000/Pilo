"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { Loader2 } from "lucide-react";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { Button } from "@/components/ui/button";
import { FieldError, Input, Label } from "@/components/ui/field";
import { ApiError, login } from "@/lib/api/auth";
import { DEMO_PASSWORD, demoAccounts } from "@/lib/auth/demo-accounts";
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
    setValue,
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

  function fillDemo(email: string) {
    setValue("email", email, { shouldValidate: true });
    setValue("password", DEMO_PASSWORD, { shouldValidate: true });
    setFormError(null);
  }

  return (
    <form className="flex flex-col gap-sm" onSubmit={handleSubmit(onSubmit)} noValidate>
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
          className="rounded-sm bg-danger-container px-3.5 py-2.5 text-body-sm text-on-danger-container"
        >
          {formError}
        </p>
      ) : null}

      <Button type="submit" disabled={isSubmitting} className="mt-xs w-full">
        {isSubmitting ? (
          <>
            <Loader2 size={20} strokeWidth={1.75} className="animate-spin" />
            Entrando…
          </>
        ) : (
          "Entrar"
        )}
      </Button>

      <div className="pt-sm">
        <p className="font-caps text-on-surface-variant">Demo</p>
        <p className="mt-xs text-body-sm text-on-surface-variant">
          Contraseña: <span className="font-code text-on-surface">{DEMO_PASSWORD}</span>
        </p>
        <div className="mt-sm grid grid-cols-3 gap-xs">
          {demoAccounts.map((account) => (
            <button
              key={account.email}
              type="button"
              onClick={() => fillDemo(account.email)}
              className="group rounded-sm bg-surface-container px-xs py-sm text-center ring-1 ring-outline-variant transition-colors duration-feedback hover:bg-primary-container hover:ring-transparent"
            >
              <span className="block font-label text-on-surface group-hover:text-on-primary-container">
                {account.role}
              </span>
              <span className="mt-xs block truncate font-code text-on-surface-variant group-hover:text-on-primary-container">
                {account.email}
              </span>
            </button>
          ))}
        </div>
      </div>
    </form>
  );
}
