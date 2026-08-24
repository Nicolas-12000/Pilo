"use client";

import { ArrowLeft } from "lucide-react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Brand } from "@/components/layout/brand";
import { LoginForm } from "@/features/auth/login-form";
import { routes } from "@/lib/routes";

export function LoginPanel() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const nextPath = searchParams.get("next");

  return (
    <main className="flex flex-1 flex-col items-center justify-center px-md py-xl">
      <div className="w-full max-w-md">
        <Link
          href={routes.home}
          className="inline-flex items-center gap-1.5 font-label text-primary-hover transition-colors duration-feedback hover:text-primary"
        >
          <ArrowLeft size={16} strokeWidth={1.75} />
          Volver al inicio
        </Link>

        <section className="mt-lg rounded-lg bg-surface p-lg shadow-card">
          <Brand />
          <h1 className="mt-lg font-headline-lg text-on-surface">Inicia sesión</h1>
          <p className="mt-xs text-body-md text-on-surface-variant">
            Entra solo cuando quieras iniciar un trámite o consultar tus expedientes.
          </p>
          <div className="mt-lg">
            <LoginForm
              onSuccess={() => {
                router.push(nextPath && nextPath.startsWith("/") ? nextPath : routes.myCases);
              }}
            />
          </div>
        </section>
      </div>
    </main>
  );
}
