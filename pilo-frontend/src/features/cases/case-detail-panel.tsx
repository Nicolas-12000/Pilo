"use client";

import { useQuery } from "@tanstack/react-query";
import { CalendarClock, History, Loader2, TriangleAlert } from "lucide-react";
import { AuditTimeline } from "@/components/pilo/audit-timeline";
import { PageHeader } from "@/components/pilo/page-header";
import { RequirementItem } from "@/components/pilo/requirement-item";
import { StatusBadge } from "@/components/pilo/status-badge";
import { WorkflowStepper } from "@/components/pilo/workflow-stepper";
import { Card, CardTitle } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { Progress } from "@/components/ui/progress";
import { Skeleton, SkeletonList } from "@/components/ui/skeleton";
import { ApiError } from "@/lib/api/client";
import { caseKeys, fetchCaseBundle, hasWorkInProgress } from "@/lib/api/queries";
import type { DocumentRecord } from "@/lib/api/types";
import { describeDeadline, formatDate } from "@/lib/cases/dates";
import { caseStatus } from "@/lib/cases/status";
import { ExternalReferences } from "@/features/cases/external-references";
import { routes } from "@/lib/routes";
import { cn } from "@/lib/utils/cn";

function documentsByRequirement(documents: DocumentRecord[]) {
  return documents.reduce<Record<string, DocumentRecord[]>>((accumulator, document) => {
    const bucket = accumulator[document.requirementId] ?? [];
    bucket.push(document);
    accumulator[document.requirementId] = bucket;
    return accumulator;
  }, {});
}

export function CaseDetailPanel({ caseId }: { caseId: string }) {
  const { data, isPending, error, refetch, isFetching } = useQuery({
    queryKey: caseKeys.bundle(caseId),
    queryFn: () => fetchCaseBundle(caseId),
    refetchInterval: (query) => (hasWorkInProgress(query.state.data) ? 2500 : false),
  });

  if (isPending) {
    return (
      <>
        <Skeleton className="h-4 w-32" />
        <Skeleton className="mt-lg h-9 w-2/3" />
        <div className="mt-xl">
          <SkeletonList count={2} />
        </div>
      </>
    );
  }

  if (error || !data) {
    return (
      <EmptyState
        icon={TriangleAlert}
        tone="danger"
        title="No hemos podido cargar el expediente"
        description={
          error instanceof ApiError ? error.message : "Comprueba tu sesión e inténtalo de nuevo."
        }
      />
    );
  }

  const { detail, workflow, documents } = data;
  const deadline = describeDeadline(detail.deadlineAt);
  const grouped = documentsByRequirement(documents);
  const processing = hasWorkInProgress(data);

  return (
    <>
      <PageHeader
        backHref={routes.myCases}
        backLabel="Mis expedientes"
        title={detail.procedureTypeTitle}
        description={detail.procedureTypeDescription}
        actions={<StatusBadge status={caseStatus[detail.status]} />}
      />

      <p className="mt-sm font-code text-secondary">{detail.caseNumber}</p>

      <div className="mt-xl grid gap-lg lg:grid-cols-[1fr_296px] lg:items-start">
        <div className="flex flex-col gap-lg">
          <Card>
            <div className="flex flex-wrap items-center justify-between gap-sm">
              <CardTitle>Requisitos y documentos</CardTitle>
              {processing ? (
                <span className="inline-flex items-center gap-1.5 text-body-sm text-on-surface-variant">
                  <Loader2 size={16} strokeWidth={1.75} className="animate-spin" />
                  Analizando con IA…
                </span>
              ) : isFetching ? (
                <span className="text-body-sm text-on-surface-variant">Actualizando…</span>
              ) : null}
            </div>
            <p className="mt-1 text-body-sm text-on-surface-variant">
              Sube un documento por requisito. La IA extrae los datos y el sistema valida cada
              documento contra las reglas del trámite.
            </p>

            <ul className="mt-md flex flex-col gap-sm">
              {detail.requirements.map((requirement) => (
                <RequirementItem
                  key={requirement.id}
                  caseId={caseId}
                  requirement={requirement}
                  documents={grouped[requirement.id] ?? []}
                  onUploaded={() => void refetch()}
                />
              ))}
            </ul>
          </Card>

          <ExternalReferences defaultQuery={detail.procedureTypeTitle} />
        </div>

        <aside className="flex flex-col gap-lg">
          <Card>
            <CardTitle>Progreso</CardTitle>
            <div className="mt-sm flex items-baseline justify-between">
              <span className="font-display text-on-surface">{detail.progressPercentage}%</span>
              <span className="text-body-sm text-on-surface-variant">completado</span>
            </div>
            <Progress value={detail.progressPercentage} className="mt-sm" />

            <dl className="mt-md flex flex-col gap-sm border-t border-outline-variant pt-md text-body-sm">
              <div className="flex items-center justify-between gap-sm">
                <dt className="inline-flex items-center gap-1.5 text-on-surface-variant">
                  <CalendarClock size={16} strokeWidth={1.75} />
                  Plazo
                </dt>
                <dd
                  className={cn(
                    "text-right font-label",
                    deadline.overdue
                      ? "text-danger"
                      : deadline.urgent
                        ? "text-warning"
                        : "text-on-surface",
                  )}
                >
                  {formatDate(detail.deadlineAt)}
                </dd>
              </div>
              <div className="flex items-center justify-between gap-sm">
                <dt className="text-on-surface-variant">Restante</dt>
                <dd className="text-right font-label text-on-surface">{deadline.label}</dd>
              </div>
              <div className="flex items-center justify-between gap-sm">
                <dt className="text-on-surface-variant">Iniciado</dt>
                <dd className="text-right font-label text-on-surface">
                  {formatDate(detail.createdAt)}
                </dd>
              </div>
            </dl>
          </Card>

          <Card>
            <CardTitle>Flujo de trabajo</CardTitle>
            <div className="mt-md">
              <WorkflowStepper tasks={workflow.tasks} />
            </div>
          </Card>

          <Card>
            <div className="flex items-center gap-xs">
              <History size={20} strokeWidth={1.75} className="text-secondary" />
              <CardTitle>Actividad</CardTitle>
            </div>
            <div className="mt-md">
              <AuditTimeline events={detail.recentAuditEvents} />
            </div>
          </Card>
        </aside>
      </div>
    </>
  );
}
