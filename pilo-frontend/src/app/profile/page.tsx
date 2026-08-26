import { PageContainer } from "@/components/layout/page-container";
import { RequireAuth } from "@/features/auth/require-auth";
import { ProfilePanel } from "@/features/profile/profile-panel";
import { routes } from "@/lib/routes";

export default function ProfilePage() {
  return (
    <PageContainer>
      <RequireAuth nextPath={routes.profile}>
        <ProfilePanel />
      </RequireAuth>
    </PageContainer>
  );
}
