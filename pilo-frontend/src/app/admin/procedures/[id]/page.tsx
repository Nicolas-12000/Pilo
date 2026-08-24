import { PageContainer } from "@/components/layout/page-container";
import { ProcedureEditor } from "@/features/admin/procedure-editor";
import { RequireAdmin } from "@/features/auth/require-admin";
import { routes } from "@/lib/routes";

type AdminProcedureEditPageProps = {
  params: Promise<{ id: string }>;
};

export default async function AdminProcedureEditPage({ params }: AdminProcedureEditPageProps) {
  const { id } = await params;

  return (
    <PageContainer>
      <RequireAdmin nextPath={routes.adminProcedure(id)}>
        <ProcedureEditor procedureId={id} />
      </RequireAdmin>
    </PageContainer>
  );
}
