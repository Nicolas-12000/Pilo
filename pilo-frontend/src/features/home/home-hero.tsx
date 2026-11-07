"use client";

import { ButtonLink } from "@/components/ui/button";
import type { User } from "@/lib/api/types";
import { useSession } from "@/lib/auth/use-session";
import { routes } from "@/lib/routes";

function firstName(fullName: string) {
  const name = fullName.trim().split(/\s+/)[0];
  return name || null;
}

function copyFor(user: User | null) {
  if (!user) {
    return {
      title: "Bienvenido. Aquí el trámite tiene principio y fin.",
      body: "Consulta requisitos y plazos sin cuenta. Cuando quieras, abre un expediente, sube los documentos y sigue hasta la resolución.",
      primary: { href: routes.procedures, label: "Ver trámites" },
      secondary: { href: routes.login, label: "Iniciar sesión" },
    };
  }

  const greeting = firstName(user.fullName);
  const hello = greeting ? `Hola, ${greeting}.` : "Hola.";

  if (user.role === "REVIEWER") {
    return {
      title: hello,
      body: "Contrasta los documentos con lo extraído y resuelve los expedientes que están en revisión.",
      primary: { href: routes.review, label: "Ir a revisión" },
      secondary: { href: routes.procedures, label: "Ver trámites" },
    };
  }

  if (user.role === "ADMIN") {
    return {
      title: hello,
      body: "El catálogo y los expedientes están en el mismo sitio. Empieza por lo que tengas entre manos.",
      primary: { href: routes.adminProcedures, label: "Gestionar trámites" },
      secondary: { href: routes.procedures, label: "Ver catálogo" },
    };
  }

  return {
    title: hello,
    body: "Sigue un expediente abierto o elige un trámite nuevo. Los documentos y el plazo están en un único sitio.",
    primary: { href: routes.myCases, label: "Mis expedientes" },
    secondary: { href: routes.procedures, label: "Ver trámites" },
  };
}

export function HomeHero() {
  const { user, status } = useSession();
  const copy = copyFor(status === "authenticated" ? user : null);

  return (
    <section>
      <h1 className="max-w-3xl font-display text-on-surface">{copy.title}</h1>
      <p className="mt-md max-w-2xl text-body-lg text-on-surface-variant">{copy.body}</p>
      <div className="mt-lg flex flex-wrap gap-sm">
        <ButtonLink href={copy.primary.href}>{copy.primary.label}</ButtonLink>
        {status === "loading" ? (
          <span className="inline-flex h-12 w-36 rounded-md bg-surface-container" aria-hidden />
        ) : (
          <ButtonLink href={copy.secondary.href} variant="secondary">
            {copy.secondary.label}
          </ButtonLink>
        )}
      </div>
    </section>
  );
}
