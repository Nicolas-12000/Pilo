import type { Metadata } from "next";
import { homeMetadata } from "@/lib/seo";
import { PageContainer } from "@/components/layout/page-container";
import { ButtonLink } from "@/components/ui/button";
import { FeaturedProcedures } from "@/features/home/featured-procedures";
import { HowItWorks } from "@/features/home/how-it-works";
import { routes } from "@/lib/routes";

export const metadata: Metadata = homeMetadata();

export default function Home() {
  return (
    <PageContainer>
      <section>
        <h1 className="max-w-3xl font-display text-on-surface">
          Tu trámite, de la solicitud a la resolución.
        </h1>
        <p className="mt-md max-w-2xl text-body-lg text-on-surface-variant">
          Consulta requisitos y plazos sin crear cuenta. Cuando quieras empezar, abres un
          expediente, subes los documentos y ves en qué punto está cada uno.
        </p>
        <div className="mt-lg flex flex-wrap gap-sm">
          <ButtonLink href={routes.procedures}>Ver trámites</ButtonLink>
          <ButtonLink href={routes.login} variant="secondary">
            Iniciar sesión
          </ButtonLink>
        </div>
      </section>

      <FeaturedProcedures />
      <HowItWorks />
    </PageContainer>
  );
}
