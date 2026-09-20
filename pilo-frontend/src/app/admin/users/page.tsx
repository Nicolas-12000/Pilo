import { PageContainer } from "@/components/layout/page-container";
import { AdminUsersPanel } from "@/features/admin/admin-users-panel";
import { RequireAdmin } from "@/features/auth/require-admin";
import { routes } from "@/lib/routes";

export default function AdminUsersPage() {
  return (
    <PageContainer>
      <RequireAdmin nextPath={routes.adminUsers}>
        <AdminUsersPanel />
      </RequireAdmin>
    </PageContainer>
  );
}
