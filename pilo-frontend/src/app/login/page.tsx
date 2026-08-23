"use client";

import { useRouter } from "next/navigation";
import { LoginForm } from "@/features/auth/login-form";

export default function LoginPage() {
  const router = useRouter();

  return (
    <main className="flex flex-1 justify-center px-4 py-12">
      <section className="w-full max-w-md rounded-lg bg-surface p-6 shadow-card">
        <p className="font-caps text-secondary">PILO</p>
        <h1 className="mt-3 font-headline-lg text-on-surface">Inicia sesión</h1>
        <p className="mt-2 text-body-md text-on-surface-variant">
          Entra para consultar tus expedientes y continuar el trámite.
        </p>
        <div className="mt-8">
          <LoginForm onSuccess={() => router.push("/")} />
        </div>
      </section>
    </main>
  );
}
