import { CalendarClock, FileText } from "lucide-react";
import { notFound } from "next/navigation";
import { PageContainer } from "@/components/layout/page-container";
import { PageHeader } from "@/components/pilo/page-header";
import { Card, CardTitle } from "@/components/ui/card";
import { RequirementList } from "@/features/procedures/requirement-list";
import { StartProcedureButton } from "@/features/procedures/start-procedure-button";
import { ApiError, getProcedureType } from "@/lib/api/procedures";
import { routes } from "@/lib/routes";

export const dynamic = "force-dynamic";

type ProcedureDetailPageProps = {
  params: Promise<{ id: string }>;
};

export default async function ProcedureDetailPage({ params }: ProcedureDetailPageProps) {
  const { id } = await params;

  let procedure;
  try {
    procedure = await getProcedureType(id);
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      notFound();
    }
    throw error;
  }

  return (
    <PageContainer>
      <PageHeader
        backHref={routes.procedures}
        backLabel="Volver al catálogo"
        eyebrow="Trámite"
        title={procedure.title}
        description={procedure.description}
      />

      <div className="mt-xl grid gap-lg lg:grid-cols-[1fr_296px] lg:items-start">
        <Card>
          <CardTitle>Requisitos</CardTitle>
          <p className="mt-1 text-body-sm text-on-surface-variant">
            Necesitarás un documento por cada requisito obligatorio.
          </p>
          <div className="mt-md">
            <RequirementList requirements={procedure.requirements} />
          </div>
        </Card>

        <aside className="flex flex-col gap-lg">
          <Card>
            <CardTitle>Resumen</CardTitle>
            <dl className="mt-sm flex flex-col gap-sm text-body-sm">
              <div className="flex items-center justify-between gap-sm">
                <dt className="inline-flex items-center gap-1.5 text-on-surface-variant">
                  <CalendarClock size={16} strokeWidth={1.75} />
                  Plazo objetivo
                </dt>
                <dd className="font-label text-on-surface">{procedure.targetDays} días</dd>
              </div>
              <div className="flex items-center justify-between gap-sm">
                <dt className="inline-flex items-center gap-1.5 text-on-surface-variant">
                  <FileText size={16} strokeWidth={1.75} />
                  Requisitos
                </dt>
                <dd className="font-label text-on-surface">{procedure.requirementCount}</dd>
              </div>
            </dl>
          </Card>

          <StartProcedureButton procedureId={procedure.id} procedureTitle={procedure.title} />
        </aside>
      </div>
    </PageContainer>
  );
}
