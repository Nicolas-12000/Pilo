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
    <main className="flex-1 px-4 py-10">
      <div className="mx-auto max-w-5xl">
        <p className="font-caps text-secondary">Catálogo</p>
        <h1 className="mt-2 font-headline-lg text-on-surface">Trámites disponibles</h1>
        <p className="mt-3 max-w-2xl text-body-lg text-on-surface-variant">
          Consulta los trámites configurados, sus plazos y requisitos. Puedes navegar libremente
          y decidir más tarde si quieres iniciar uno.
        </p>

        {loadError ? (
          <p className="mt-10 rounded-lg bg-danger-container px-4 py-5 text-body-md text-on-danger-container">
            No hemos podido cargar el catálogo. Comprueba que el backend esté en marcha en{" "}
            <code className="font-mono text-sm">localhost:8080</code>.
          </p>
        ) : procedures.length === 0 ? (
          <p className="mt-10 rounded-lg bg-surface-container px-4 py-5 text-body-md text-on-surface-variant">
            Todavía no hay trámites publicados. Arranca el backend con el perfil{" "}
            <code className="font-mono text-sm">local</code> para cargar datos de ejemplo.
          </p>
        ) : (
          <div className="mt-10 grid gap-4 md:grid-cols-2">
            {procedures.map((procedure) => (
              <ProcedureCard key={procedure.id} procedure={procedure} />
            ))}
          </div>
        )}
      </div>
    </main>
  );
}
