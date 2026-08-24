import { CalendarClock, ChevronRight } from "lucide-react";
import Link from "next/link";
import { StatusBadge } from "@/components/pilo/status-badge";
import { Progress } from "@/components/ui/progress";
import { describeDeadline, formatDate } from "@/lib/cases/dates";
import { caseStatus } from "@/lib/cases/status";
import type { CaseSummary } from "@/lib/api/types";
import { routes } from "@/lib/routes";
import { cn } from "@/lib/utils/cn";

export function CaseCard({ caseItem }: { caseItem: CaseSummary }) {
  const deadline = describeDeadline(caseItem.deadlineAt);

  return (
    <Link
      href={routes.case(caseItem.id)}
      className="group block rounded-lg bg-surface p-md shadow-card ring-1 ring-transparent transition-shadow duration-content hover:shadow-elevated hover:ring-outline-variant md:p-lg"
    >
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="font-code text-secondary">{caseItem.caseNumber}</p>
          <h3 className="mt-1.5 font-title-md text-on-surface">{caseItem.procedureTypeTitle}</h3>
        </div>
        <StatusBadge status={caseStatus[caseItem.status]} />
      </div>

      <div className="mt-md">
        <div className="flex items-center justify-between text-body-sm text-on-surface-variant">
          <span>Progreso</span>
          <span className="font-label text-on-surface">{caseItem.progressPercentage}%</span>
        </div>
        <Progress value={caseItem.progressPercentage} className="mt-xs" />
      </div>

      <div className="mt-md flex flex-wrap items-center gap-x-lg gap-y-xs text-body-sm text-on-surface-variant">
        <span className="inline-flex items-center gap-1.5">
          <CalendarClock size={16} strokeWidth={1.75} className="shrink-0" />
          <span
            className={cn(
              deadline.overdue && "text-danger",
              deadline.urgent && "text-warning",
            )}
          >
            {deadline.label}
          </span>
        </span>
        <span>Iniciado el {formatDate(caseItem.createdAt)}</span>
        <span className="ml-auto inline-flex items-center gap-1 font-label text-primary-hover">
          Ver detalle
          <ChevronRight
            size={16}
            strokeWidth={1.75}
            className="transition-transform duration-feedback group-hover:translate-x-0.5"
          />
        </span>
      </div>
    </Link>
  );
}
