import { PageContainer } from "@/components/layout/page-container";
import { RequireAuth } from "@/features/auth/require-auth";
import { CaseDetailPanel } from "@/features/cases/case-detail-panel";
import { routes } from "@/lib/routes";

export const dynamic = "force-dynamic";

type CaseDetailPageProps = {
  params: Promise<{ id: string }>;
};

export default async function CaseDetailPage({ params }: CaseDetailPageProps) {
  const { id } = await params;

  return (
    <PageContainer>
      <RequireAuth nextPath={routes.case(id)}>
        <CaseDetailPanel caseId={id} />
      </RequireAuth>
    </PageContainer>
  );
}
