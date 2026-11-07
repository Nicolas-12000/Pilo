"use client";

import { useQuery } from "@tanstack/react-query";
import { Landmark, TriangleAlert } from "lucide-react";
import { StaggerItem } from "@/components/motion/stagger-item";
import { PageHeader } from "@/components/pilo/page-header";
import { EmptyState } from "@/components/ui/empty-state";
import { SkeletonList } from "@/components/ui/skeleton";
import { ProcedureCard } from "@/features/procedures/procedure-card";
import { ApiError, listProcedureTypes } from "@/lib/api/procedures";
import { procedureKeys } from "@/lib/api/queries";
import type { ProcedureTypeSummary } from "@/lib/api/types";

type ProceduresPanelProps = {
  /** Server-fetched catalog (ISR); avoids an empty first paint when the API is up. */
  initialProcedures?: ProcedureTypeSummary[];
};

export function ProceduresPanel({ initialProcedures }: ProceduresPanelProps) {
  const { data, isPending, error } = useQuery({
    queryKey: procedureKeys.list(),
    queryFn: listProcedureTypes,
    staleTime: 60_000,
    initialData: initialProcedures,
  });

  return (
    <>
      <PageHeader
        eyebrow="Catálogo"
        title="Trámites disponibles"
        description="Consulta los trámites configurados, sus plazos y requisitos. Puedes navegar libremente y decidir más tarde si quieres iniciar uno."
      />

      <div className="mt-xl">
        {isPending ? (
          <SkeletonList count={2} columns={2} />
        ) : error ? (
          <EmptyState
            icon={TriangleAlert}
            tone="danger"
            title="No hemos podido cargar el catálogo"
            description={
              error instanceof ApiError && error.status < 500
                ? error.message
                : "El servicio no responde ahora mismo. Inténtalo de nuevo en unos segundos."
            }
          />
        ) : data && data.length > 0 ? (
          <div className="grid gap-md md:grid-cols-2">
            {data.map((procedure, index) => (
              <StaggerItem key={procedure.id} index={index}>
                <ProcedureCard procedure={procedure} />
              </StaggerItem>
            ))}
          </div>
        ) : (
          <EmptyState
            icon={Landmark}
            title="Todavía no hay trámites publicados"
            description="Cuando la administración publique uno, aparecerá aquí."
          />
        )}
      </div>
    </>
  );
}
