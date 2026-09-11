"use client";

import { useQuery } from "@tanstack/react-query";
import { ClipboardCheck, TriangleAlert } from "lucide-react";
import Link from "next/link";
import { PageHeader } from "@/components/pilo/page-header";
import { StatusBadge } from "@/components/pilo/status-badge";
import { Card } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { SkeletonList } from "@/components/ui/skeleton";
import { listPendingReviewCases } from "@/lib/api/cases";
import { ApiError } from "@/lib/api/client";
import { caseKeys } from "@/lib/api/queries";
import { formatDate } from "@/lib/cases/dates";
import { caseStatus } from "@/lib/cases/status";
import { routes } from "@/lib/routes";

export function ReviewQueuePanel() {
  const { data, isPending, error } = useQuery({
    queryKey: caseKeys.pendingReview(),
    queryFn: listPendingReviewCases,
  });

  return (
    <>
      <PageHeader
        eyebrow="Revisión"
        title="Expedientes pendientes"
        description="Casos con documentación validada que esperan la revisión final."
      />

      {isPending ? (
        <div className="mt-xl">
          <SkeletonList count={3} columns={1} />
        </div>
      ) : error ? (
        <div className="mt-xl">
          <EmptyState
            icon={TriangleAlert}
            tone="danger"
            title="No hemos podido cargar la bandeja de revisión"
            description={
              error instanceof ApiError ? error.message : "Inténtalo de nuevo en unos segundos."
            }
          />
        </div>
      ) : !data || data.length === 0 ? (
        <div className="mt-xl">
          <EmptyState
            icon={ClipboardCheck}
            title="No hay expedientes pendientes"
            description="Cuando un ciudadano complete la documentación obligatoria, aparecerá aquí."
          />
        </div>
      ) : (
        <ul className="mt-xl flex flex-col gap-sm">
          {data.map((item) => (
            <li key={item.id}>
              <Link href={routes.case(item.id)}>
                <Card className="transition-[box-shadow,transform] duration-content hover:-translate-y-px hover:shadow-elevated">
                  <div className="flex flex-wrap items-start justify-between gap-md">
                    <div className="min-w-0">
                      <p className="font-code text-secondary">{item.caseNumber}</p>
                      <h2 className="mt-xs font-title-md text-on-surface">{item.procedureTypeTitle}</h2>
                      <p className="mt-xs text-body-sm text-on-surface-variant">
                        {item.applicantFullName} · {item.applicantEmail}
                      </p>
                    </div>
                    <div className="flex flex-col items-end gap-xs">
                      <StatusBadge status={caseStatus[item.status]} />
                      <span className="text-body-sm text-on-surface-variant">
                        Plazo {formatDate(item.deadlineAt)}
                      </span>
                    </div>
                  </div>
                </Card>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </>
  );
}
