import type { Metadata } from "next";
import { PageContainer } from "@/components/layout/page-container";
import { privatePageMetadata } from "@/lib/seo";
import { RequireAuth } from "@/features/auth/require-auth";
import { MyCasesPanel } from "@/features/cases/my-cases-panel";
import { routes } from "@/lib/routes";

export const metadata: Metadata = privatePageMetadata("Mis expedientes");

export default function MyCasesPage() {
  return (
    <PageContainer>
      <RequireAuth nextPath={routes.myCases}>
        <MyCasesPanel />
      </RequireAuth>
    </PageContainer>
  );
}
