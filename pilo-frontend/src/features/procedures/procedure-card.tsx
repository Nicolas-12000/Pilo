import Link from "next/link";
import type { ProcedureTypeSummary } from "@/lib/api/types";

type ProcedureCardProps = {
  procedure: ProcedureTypeSummary;
};

export function ProcedureCard({ procedure }: ProcedureCardProps) {
  return (
    <article className="rounded-lg bg-surface p-4 shadow-card">
      <h2 className="font-title-md text-on-surface">{procedure.title}</h2>
      <p className="mt-2 line-clamp-3 text-body-md text-on-surface-variant">
        {procedure.description}
      </p>
      <dl className="mt-4 flex flex-wrap gap-3 text-body-sm text-on-surface-variant">
        <div>
          <dt className="font-caps text-secondary">Plazo</dt>
          <dd className="mt-1">{procedure.targetDays} días</dd>
        </div>
        <div>
          <dt className="font-caps text-secondary">Requisitos</dt>
          <dd className="mt-1">{procedure.requirementCount}</dd>
        </div>
      </dl>
      <Link
        href={`/tramites/${procedure.id}`}
        className="mt-5 inline-flex h-11 items-center rounded-md bg-primary px-4 font-label text-on-primary hover:bg-primary-hover"
      >
        Ver trámite
      </Link>
    </article>
  );
}
