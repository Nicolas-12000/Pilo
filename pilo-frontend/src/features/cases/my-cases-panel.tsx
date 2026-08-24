"use client";

import { useQuery } from "@tanstack/react-query";
import { FolderOpen, Plus, TriangleAlert } from "lucide-react";
import { StaggerItem } from "@/components/motion/stagger-item";
import { CaseCard } from "@/components/pilo/case-card";
import { PageHeader } from "@/components/pilo/page-header";
import { ButtonLink } from "@/components/ui/button";
import { EmptyState } from "@/components/ui/empty-state";
import { SkeletonList } from "@/components/ui/skeleton";
import { listMyCases } from "@/lib/api/cases";
import { ApiError } from "@/lib/api/client";
import { caseKeys } from "@/lib/api/queries";
import { routes } from "@/lib/routes";

export function MyCasesPanel() {
  const { data, isPending, error } = useQuery({
    queryKey: caseKeys.list(),
    queryFn: listMyCases,
  });

  return (
    <>
      <PageHeader
        eyebrow="Expedientes"
        title="Mis expedientes"
        description="Consulta el progreso de tus trámites, sube documentos y revisa la actividad registrada."
        actions={
          <ButtonLink href={routes.procedures} size="sm">
            <Plus size={20} strokeWidth={1.75} />
            Nuevo trámite
          </ButtonLink>
        }
      />

      <div className="mt-xl">
        {isPending ? (
          <SkeletonList count={3} columns={2} />
        ) : error ? (
          <EmptyState
            icon={TriangleAlert}
            tone="danger"
            title="No hemos podido cargar tus expedientes"
            description={
              error instanceof ApiError ? error.message : "Inténtalo de nuevo en unos segundos."
            }
          />
        ) : data && data.length > 0 ? (
          <div className="grid gap-md md:grid-cols-2">
            {data.map((caseItem, index) => (
              <StaggerItem key={caseItem.id} index={index}>
                <CaseCard caseItem={caseItem} />
              </StaggerItem>
            ))}
          </div>
        ) : (
          <EmptyState
            icon={FolderOpen}
            title="Aún no tienes expedientes"
            description="Explora el catálogo de trámites y abre tu primer expediente. Te guiaremos requisito a requisito."
            action={<ButtonLink href={routes.procedures}>Ver catálogo de trámites</ButtonLink>}
          />
        )}
      </div>
    </>
  );
}
