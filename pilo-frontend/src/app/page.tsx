import { FileText, FolderOpen, Landmark, Sparkles } from "lucide-react";
import { PageContainer } from "@/components/layout/page-container";
import { ButtonLink } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { routes } from "@/lib/routes";

const steps = [
  {
    icon: Landmark,
    title: "Elige el trámite",
    description:
      "Consulta el catálogo con los requisitos y plazos de cada trámite. No necesitas cuenta para explorarlo.",
  },
  {
    icon: FileText,
    title: "Sube los documentos",
    description:
      "La IA extrae los datos de cada documento y el sistema los valida contra las reglas del trámite.",
  },
  {
    icon: FolderOpen,
    title: "Sigue el expediente",
    description:
      "Ves el estado de cada requisito, el flujo de revisión y el plazo restante en un único sitio.",
  },
];

export default function Home() {
  return (
    <PageContainer>
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

      <section className="mt-section">
        <h2 className="font-headline-md text-on-surface">Cómo funciona</h2>
        <div className="mt-md grid gap-md md:grid-cols-3">
          {steps.map((step, index) => (
            <Card key={step.title}>
              <div className="flex items-center justify-between">
                <span className="grid size-10 place-items-center rounded-md bg-surface-container">
                  <step.icon size={20} strokeWidth={1.75} className="text-primary" />
                </span>
                <span className="font-caps text-outline">0{index + 1}</span>
              </div>
              <h3 className="mt-md font-title-md text-on-surface">{step.title}</h3>
              <p className="mt-xs text-body-sm text-on-surface-variant">{step.description}</p>
            </Card>
          ))}
        </div>
      </section>
    </PageContainer>
  );
}
