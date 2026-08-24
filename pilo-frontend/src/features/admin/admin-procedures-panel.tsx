"use client";

import { useQuery } from "@tanstack/react-query";
import { Plus, Settings2, TriangleAlert } from "lucide-react";
import Link from "next/link";
import { PageHeader } from "@/components/pilo/page-header";
import { ButtonLink } from "@/components/ui/button";
import { EmptyState } from "@/components/ui/empty-state";
import { SkeletonList } from "@/components/ui/skeleton";
import { listAdminProcedures } from "@/lib/api/admin-procedures";
import { ApiError } from "@/lib/api/client";
import { adminProcedureKeys } from "@/lib/api/queries";
import { routes } from "@/lib/routes";

export function AdminProceduresPanel() {
  const { data, isPending, error } = useQuery({
    queryKey: adminProcedureKeys.list(),
    queryFn: listAdminProcedures,
  });

  return (
    <>
      <PageHeader
        eyebrow="Administración"
        title="Catálogo de trámites"
        description="Configura tipos de trámite, requisitos documentales y reglas de validación deterministas."
        actions={
          <ButtonLink href={routes.adminProcedureNew}>
            <Plus size={18} strokeWidth={1.75} />
            Nuevo trámite
          </ButtonLink>
        }
      />

      <div className="mt-xl">
        {isPending ? (
          <SkeletonList count={2} columns={1} />
        ) : error ? (
          <EmptyState
            icon={TriangleAlert}
            tone="danger"
            title="No hemos podido cargar el catálogo"
            description={
              error instanceof ApiError && error.status === 403
                ? "Tu cuenta no tiene permisos de administrador."
                : "Comprueba que el backend esté en marcha."
            }
          />
        ) : data && data.length > 0 ? (
          <ul className="flex flex-col gap-sm">
            {data.map((procedure) => (
              <li key={procedure.id}>
                <Link
                  href={routes.adminProcedure(procedure.id)}
                  className="elevation-card flex flex-wrap items-center justify-between gap-md rounded-lg p-md transition-[box-shadow,transform] duration-content hover:-translate-y-px hover:shadow-elevated md:p-lg"
                >
                  <div className="min-w-0">
                    <h2 className="font-title-md text-on-surface">{procedure.title}</h2>
                    <p className="mt-1 line-clamp-2 text-body-sm text-on-surface-variant">
                      {procedure.description}
                    </p>
                    <p className="mt-sm text-body-sm text-on-surface-variant">
                      {procedure.targetDays} días · {procedure.requirementCount} requisitos
                    </p>
                  </div>
                  <span className="inline-flex items-center gap-1 font-label text-primary-hover">
                    <Settings2 size={16} strokeWidth={1.75} />
                    Editar
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        ) : (
          <EmptyState
            icon={Settings2}
            title="Todavía no hay trámites"
            description="Crea el primero para publicarlo en el catálogo público."
            action={<ButtonLink href={routes.adminProcedureNew}>Crear trámite</ButtonLink>}
          />
        )}
      </div>
    </>
  );
}
