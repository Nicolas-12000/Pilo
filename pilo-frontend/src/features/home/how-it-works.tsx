import { FileText, FolderOpen, Landmark } from "lucide-react";
import type { LucideIcon } from "lucide-react";
import { StaggerItem } from "@/components/motion/stagger-item";
import { cn } from "@/lib/utils/cn";

const steps: {
  icon: LucideIcon;
  title: string;
  description: string;
}[] = [
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

export function HowItWorks() {
  return (
    <section className="mt-section" aria-labelledby="how-it-works-heading">
      <p className="font-caps text-primary">El proceso</p>
      <h2 id="how-it-works-heading" className="mt-xs font-headline-md text-on-surface">
        Cómo funciona
      </h2>
      <p className="mt-xs max-w-2xl text-body-md text-on-surface-variant">
        Tres pasos, el mismo expediente. Empiezas por el catálogo y terminas con el plazo y la
        revisión a la vista.
      </p>

      <ol className="relative mt-lg grid gap-md md:grid-cols-3">
        <span
          aria-hidden
          className="pointer-events-none absolute top-11 right-[16%] left-[16%] hidden h-px bg-outline-variant md:block"
        />

        {steps.map((step, index) => (
          <li key={step.title} className="h-full">
            <StaggerItem index={index} className="h-full">
              <article
                className={cn(
                  "elevation-card group relative flex h-full flex-col rounded-lg p-md",
                  "transition-[box-shadow,transform,outline-color] duration-content",
                  "hover:-translate-y-px hover:shadow-elevated hover:outline-outline",
                  "md:p-lg",
                )}
              >
                <div className="flex items-center justify-between">
                  <span
                    className={cn(
                      "grid size-10 place-items-center rounded-md bg-primary-container text-on-primary-container",
                      "transition-transform duration-feedback group-hover:scale-[1.04]",
                    )}
                  >
                    <step.icon size={20} strokeWidth={1.75} />
                  </span>
                  <span className="font-caps text-on-surface-variant transition-colors duration-feedback group-hover:text-primary">
                    0{index + 1}
                  </span>
                </div>
                <h3 className="mt-md font-title-md text-on-surface">{step.title}</h3>
                <p className="mt-xs text-body-sm text-on-surface-variant">{step.description}</p>
              </article>
            </StaggerItem>
          </li>
        ))}
      </ol>
    </section>
  );
}
