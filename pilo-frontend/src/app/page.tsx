import { Sparkles } from "lucide-react";
import { PageContainer } from "@/components/layout/page-container";
import { PrefetchCatalog } from "@/components/pilo/prefetch-catalog";
import { ButtonLink } from "@/components/ui/button";
import { AccessPaths } from "@/features/home/access-paths";
import { HowItWorks } from "@/features/home/how-it-works";
import { routes } from "@/lib/routes";

export default function Home() {
  return (
    <PageContainer>
      <PrefetchCatalog />
      <section>
        <p className="inline-flex items-center gap-1.5 rounded-full bg-primary-container px-2.5 py-1 font-caps text-on-primary-container">
          <Sparkles size={16} strokeWidth={1.75} />
          Trámites con apoyo de IA
        </p>
        <h1 className="mt-md max-w-3xl font-display text-on-surface">
          Trámites administrativos, de extremo a extremo.
        </h1>
        <p className="mt-md max-w-2xl text-body-lg text-on-surface-variant">
          Explora el catálogo, revisa requisitos y plazos sin crear cuenta. Solo necesitarás
          iniciar sesión cuando quieras abrir un expediente.
        </p>
        <div className="mt-lg flex flex-wrap gap-sm">
          <ButtonLink href={routes.procedures}>Ver trámites</ButtonLink>
          <ButtonLink href={routes.login} variant="secondary">
            Iniciar sesión
          </ButtonLink>
        </div>
      </section>

      <AccessPaths />
      <HowItWorks />
    </PageContainer>
  );
}
