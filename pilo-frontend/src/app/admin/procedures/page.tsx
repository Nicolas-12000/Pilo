import { PageContainer } from "@/components/layout/page-container";
import { AdminProceduresPanel } from "@/features/admin/admin-procedures-panel";
import { RequireAdmin } from "@/features/auth/require-admin";
import { routes } from "@/lib/routes";

export default function AdminProceduresPage() {
  return (
    <PageContainer>
      <RequireAdmin nextPath={routes.adminProcedures}>
        <AdminProceduresPanel />
      </RequireAdmin>
    </PageContainer>
  );
}
