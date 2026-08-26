"use client";

import { ArrowLeft, FileText, FolderOpen, ShieldCheck } from "lucide-react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Brand } from "@/components/layout/brand";
import { ThemeToggle } from "@/components/layout/theme-toggle";
import { LoginForm } from "@/features/auth/login-form";
import { isSafeRedirectPath, routes } from "@/lib/routes";

const afterLogin = [
  {
    icon: FolderOpen,
    title: "Tus expedientes",
    description: "Sigue el trámite requisito a requisito.",
  },
  {
    icon: FileText,
    title: "Documentos con IA",
    description: "Extrae datos y valida contra las reglas.",
  },
  {
    icon: ShieldCheck,
    title: "Revisión y plazos",
    description: "Flujo de revisión y fecha límite a la vista.",
  },
];

/**
 * Precomputed once: a gentle two-period wave used as the panel divider.
 * Values are percentages in a 0-100 viewBox so the SVG stretches to any
 * card height without distortion mattering (it's purely decorative).
 */
const WAVE_PATH = (() => {
  const points = 48;
  const amplitude = 9;
  const periods = 2.4;
  let d = "M100,0";
  for (let i = 0; i <= points; i++) {
    const y = (i * 100) / points;
    const x = 74 + amplitude * Math.sin((y / 100) * periods * Math.PI * 2);
    d += ` L${x.toFixed(2)},${y.toFixed(2)}`;
  }
  return d + " L100,100 Z";
})();

export function LoginPanel() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const nextPath = searchParams.get("next");

  return (
    <main className="flex min-h-dvh flex-col bg-surface-container-low px-md py-md lg:px-xl lg:py-lg">
      <div className="flex items-center justify-between">
        <Link
          href={routes.home}
          className="inline-flex items-center gap-1.5 font-label text-primary-hover transition-colors duration-feedback hover:text-primary"
        >
          <ArrowLeft size={16} strokeWidth={1.75} />
          Volver al inicio
        </Link>
        <ThemeToggle />
      </div>

      <div className="flex flex-1 items-center justify-center py-lg">
        <div className="animate-enter w-full max-w-4xl">
          <div className="relative grid overflow-hidden rounded-xl shadow-overlay lg:grid-cols-[1fr_1.15fr]">
            <section className="hidden flex-col justify-between bg-surface px-xl py-xxl lg:flex">
              <Brand />

              <div className="my-auto py-xl">
                <h1 className="max-w-sm font-headline-lg text-on-surface">
                  Entra para abrir o consultar un expediente.
                </h1>
                <p className="mt-sm max-w-sm text-body-md text-on-surface-variant">
                  El catálogo es público. La cuenta solo hace falta cuando el trámite pasa a ser
                  tuyo.
                </p>

                <ul className="mt-xl flex flex-col gap-lg">
                  {afterLogin.map((item) => (
                    <li key={item.title} className="flex items-start gap-md">
                      <span className="grid size-10 shrink-0 place-items-center rounded-md bg-primary-container text-on-primary-container">
                        <item.icon size={20} strokeWidth={1.75} />
                      </span>
                      <div>
                        <p className="font-title-sm text-on-surface">{item.title}</p>
                        <p className="mt-xxs text-body-sm text-on-surface-variant">
                          {item.description}
                        </p>
                      </div>
                    </li>
                  ))}
                </ul>
              </div>

              <p className="border-t border-outline-variant pt-lg text-body-sm text-on-surface-variant">
                Trámites oficiales, seguimiento claro y sin filas.
              </p>
            </section>

            <div
              aria-hidden
              className="pointer-events-none absolute inset-y-0 left-[41.5%] hidden w-20 -translate-x-1/2 lg:block"
            >
              <svg
                viewBox="0 0 100 100"
                preserveAspectRatio="none"
                className="h-full w-full fill-primary"
              >
                <path d={WAVE_PATH} />
              </svg>
            </div>

            <section className="relative flex justify-center bg-primary px-md py-xl lg:px-xl">
              <div className="w-full max-w-sm self-center">
                <div className="elevation-card rounded-lg p-lg">
                  <div className="mb-md lg:hidden">
                    <Brand />
                  </div>
                  <h2 className="font-headline-lg text-on-surface">Inicia sesión</h2>
                  <p className="mt-xs text-body-sm text-on-surface-variant">
                    Entra cuando quieras iniciar un trámite o consultar tus expedientes.
                  </p>
                  <div className="mt-lg">
                    <LoginForm
                      onSuccess={() => {
                        router.push(isSafeRedirectPath(nextPath) ? nextPath : routes.myCases);
                      }}
                    />
                  </div>
                </div>
              </div>
            </section>
          </div>
        </div>
      </div>
    </main>
  );
}
