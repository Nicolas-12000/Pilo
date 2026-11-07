import type { Metadata } from "next";
import { PageContainer } from "@/components/layout/page-container";
import { privatePageMetadata } from "@/lib/seo";
import { RequireAuth } from "@/features/auth/require-auth";
import { ProfilePanel } from "@/features/profile/profile-panel";
import { routes } from "@/lib/routes";

export const metadata: Metadata = privatePageMetadata("Perfil");

export default function ProfilePage() {
  return (
    <PageContainer>
      <RequireAuth nextPath={routes.profile}>
        <ProfilePanel />
      </RequireAuth>
    </PageContainer>
  );
}
