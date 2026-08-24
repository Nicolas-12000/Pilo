import { PageContainer } from "@/components/layout/page-container";
import { ProcedureEditor } from "@/features/admin/procedure-editor";
import { RequireAdmin } from "@/features/auth/require-admin";
import { routes } from "@/lib/routes";

export default function AdminProcedureNewPage() {
  return (
    <PageContainer>
      <RequireAdmin nextPath={routes.adminProcedureNew}>
        <ProcedureEditor />
      </RequireAdmin>
    </PageContainer>
  );
}
