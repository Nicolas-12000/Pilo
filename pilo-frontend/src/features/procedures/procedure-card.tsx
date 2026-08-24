import { CalendarClock, ChevronRight, FileText } from "lucide-react";
import Link from "next/link";
import type { ProcedureTypeSummary } from "@/lib/api/types";
import { routes } from "@/lib/routes";

export function ProcedureCard({ procedure }: { procedure: ProcedureTypeSummary }) {
  return (
    <Link
      href={routes.procedure(procedure.id)}
      className="group flex flex-col rounded-lg bg-surface p-md shadow-card ring-1 ring-transparent transition-shadow duration-content hover:shadow-elevated hover:ring-outline-variant md:p-lg"
    >
      <h2 className="font-title-md text-on-surface">{procedure.title}</h2>
      <p className="mt-xs line-clamp-3 text-body-sm text-on-surface-variant">
        {procedure.description}
      </p>

      <div className="mt-md flex flex-wrap items-center gap-x-md gap-y-xs text-body-sm text-on-surface-variant">
        <span className="inline-flex items-center gap-1.5">
          <CalendarClock size={16} strokeWidth={1.75} />
          {procedure.targetDays} días
        </span>
        <span className="inline-flex items-center gap-1.5">
          <FileText size={16} strokeWidth={1.75} />
          {procedure.requirementCount}{" "}
          {procedure.requirementCount === 1 ? "requisito" : "requisitos"}
        </span>
      </div>

      <span className="mt-md inline-flex items-center gap-1 font-label text-primary-hover">
        Ver trámite
        <ChevronRight
          size={16}
          strokeWidth={1.75}
          className="transition-transform duration-feedback group-hover:translate-x-0.5"
        />
      </span>
    </Link>
  );
}
