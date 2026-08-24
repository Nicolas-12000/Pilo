import { PageContainer } from "@/components/layout/page-container";
import { RequireAuth } from "@/features/auth/require-auth";
import { MyCasesPanel } from "@/features/cases/my-cases-panel";
import { routes } from "@/lib/routes";

export const dynamic = "force-dynamic";

export default function MyCasesPage() {
  return (
    <PageContainer>
      <RequireAuth nextPath={routes.myCases}>
        <MyCasesPanel />
      </RequireAuth>
    </PageContainer>
  );
}
