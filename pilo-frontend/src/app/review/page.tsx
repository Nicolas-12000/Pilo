import type { Metadata } from "next";
import { PageContainer } from "@/components/layout/page-container";
import { privatePageMetadata } from "@/lib/seo";
import { RequireAuth } from "@/features/auth/require-auth";
import { ReviewQueuePanel } from "@/features/cases/review-queue-panel";
import { routes } from "@/lib/routes";

export const metadata: Metadata = privatePageMetadata("Revisión");

export default function ReviewPage() {
  return (
    <PageContainer>
      <RequireAuth nextPath={routes.review}>
        <ReviewQueuePanel />
      </RequireAuth>
    </PageContainer>
  );
}
