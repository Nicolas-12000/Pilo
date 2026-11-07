import type { Metadata } from "next";
import { Loader2 } from "lucide-react";
import { Suspense } from "react";
import { LoginPanel } from "@/features/auth/login-panel";
import { privatePageMetadata } from "@/lib/seo";

export const metadata: Metadata = privatePageMetadata("Iniciar sesión");

export default function LoginPage() {
  return (
    <Suspense
      fallback={
        <div className="flex h-dvh items-center justify-center px-md text-on-surface-variant">
          <Loader2 size={24} strokeWidth={1.75} className="animate-spin" />
        </div>
      }
    >
      <LoginPanel />
    </Suspense>
  );
}
