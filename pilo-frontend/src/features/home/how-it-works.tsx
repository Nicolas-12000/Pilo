import { StaggerItem } from "@/components/motion/stagger-item";

const steps = [
  {
    title: "Elige el trámite",
    description: "Requisitos y plazos de cada trámite, sin cuenta si solo quieres mirar.",
  },
  {
    title: "Sube los documentos",
    description:
      "Leemos cada documento y comprobamos que cumple las reglas del trámite antes de que llegue a revisión.",
  },
  {
    title: "Sigue el expediente",
    description:
      "Ves el estado de cada requisito, quién lo está revisando y el plazo restante en un único sitio.",
  },
];

export function HowItWorks() {
  return (
    <section className="mt-section" aria-labelledby="how-it-works-heading">
      <h2 id="how-it-works-heading" className="font-headline-md text-on-surface">
        Cómo funciona
      </h2>

      <ol className="mt-lg grid gap-lg md:grid-cols-3">
        {steps.map((step, index) => (
          <li key={step.title}>
            <StaggerItem index={index}>
              <span
                aria-hidden
                className="inline-flex size-7 items-center justify-center rounded-full bg-surface-container-high font-label text-on-surface-variant"
              >
                {index + 1}
              </span>
              <h3 className="mt-xs font-title-md text-on-surface">{step.title}</h3>
              <p className="mt-xs text-body-sm text-on-surface-variant">{step.description}</p>
            </StaggerItem>
          </li>
        ))}
      </ol>
    </section>
  );
}
