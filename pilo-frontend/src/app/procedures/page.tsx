import { Landmark, TriangleAlert } from "lucide-react";
import { PageContainer } from "@/components/layout/page-container";
import { PageHeader } from "@/components/pilo/page-header";
import { EmptyState } from "@/components/ui/empty-state";
import { ProcedureCard } from "@/features/procedures/procedure-card";
import { ApiError, listProcedureTypes } from "@/lib/api/procedures";

export const dynamic = "force-dynamic";

export default async function ProceduresPage() {
  let procedures: Awaited<ReturnType<typeof listProcedureTypes>> = [];
  let loadError = false;

  try {
    procedures = await listProcedureTypes();
  } catch (error) {
    if (!(error instanceof ApiError) || error.status >= 500) {
      loadError = true;
    }
  }

  return (
    <PageContainer>
      <PageHeader
        eyebrow="Catálogo"
        title="Trámites disponibles"
        description="Consulta los trámites configurados, sus plazos y requisitos. Puedes navegar libremente y decidir más tarde si quieres iniciar uno."
      />

      <div className="mt-xl">
        {loadError ? (
          <EmptyState
            icon={TriangleAlert}
            tone="danger"
            title="No hemos podido cargar el catálogo"
            description="Comprueba que el backend esté en marcha en localhost:8080."
          />
        ) : procedures.length === 0 ? (
          <EmptyState
            icon={Landmark}
            title="Todavía no hay trámites publicados"
            description="Arranca el backend con el perfil local para cargar los datos de ejemplo."
          />
        ) : (
          <div className="grid gap-md md:grid-cols-2">
            {procedures.map((procedure) => (
              <ProcedureCard key={procedure.id} procedure={procedure} />
            ))}
          </div>
        )}
      </div>
    </PageContainer>
  );
}
