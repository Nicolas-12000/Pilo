import { PageContainer } from "@/components/layout/page-container";
import { RequireAuth } from "@/features/auth/require-auth";
import { ReviewQueuePanel } from "@/features/cases/review-queue-panel";
import { routes } from "@/lib/routes";

export default function ReviewPage() {
  return (
    <PageContainer>
      <RequireAuth nextPath={routes.review}>
        <ReviewQueuePanel />
      </RequireAuth>
    </PageContainer>
  );
}
