import { FileCheck2, FolderSearch } from "lucide-react";

const paths = [
  {
    icon: FolderSearch,
    title: "Sin cuenta",
    description: "Consulta el catálogo, los requisitos y los plazos. No hace falta registrarse para mirar.",
  },
  {
    icon: FileCheck2,
    title: "Con cuenta",
    description: "Abre un expediente, sube documentos y sigue la validación y la revisión en un solo sitio.",
  },
];

export function AccessPaths() {
  return (
    <section className="mt-xl grid gap-md md:grid-cols-2" aria-label="Cómo usar PILO">
      {paths.map((path) => (
        <article key={path.title} className="elevation-card flex gap-md rounded-lg p-md md:p-lg">
          <span className="grid size-10 shrink-0 place-items-center rounded-md bg-primary-container text-on-primary-container">
            <path.icon size={20} strokeWidth={1.75} />
          </span>
          <div>
            <h2 className="font-title-md text-on-surface">{path.title}</h2>
            <p className="mt-xs text-body-sm text-on-surface-variant">{path.description}</p>
          </div>
        </article>
      ))}
    </section>
  );
}
