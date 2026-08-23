import Link from "next/link";
import { notFound } from "next/navigation";
import { RequirementList } from "@/features/procedures/requirement-list";
import { StartProcedureButton } from "@/features/procedures/start-procedure-button";
import { ApiError, getProcedureType } from "@/lib/api/procedures";

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
    <main className="flex-1 px-4 py-10">
      <div className="mx-auto max-w-3xl">
        <Link href="/tramites" className="font-label text-primary-hover hover:text-primary">
          ← Volver al catálogo
        </Link>

        <p className="mt-6 font-caps text-secondary">Trámite</p>
        <h1 className="mt-2 font-headline-lg text-on-surface">{procedure.title}</h1>
        <p className="mt-4 text-body-lg text-on-surface-variant">{procedure.description}</p>

        <dl className="mt-6 flex flex-wrap gap-6 text-body-md">
          <div>
            <dt className="font-caps text-secondary">Plazo objetivo</dt>
            <dd className="mt-1 text-on-surface">{procedure.targetDays} días</dd>
          </div>
          <div>
            <dt className="font-caps text-secondary">Requisitos</dt>
            <dd className="mt-1 text-on-surface">{procedure.requirementCount}</dd>
          </div>
        </dl>

        <section className="mt-10">
          <h2 className="font-title-md text-on-surface">Requisitos</h2>
          <div className="mt-4">
            <RequirementList requirements={procedure.requirements} />
          </div>
        </section>

        <section className="mt-10">
          <StartProcedureButton procedureId={procedure.id} procedureTitle={procedure.title} />
        </section>
      </div>
    </main>
  );
}
