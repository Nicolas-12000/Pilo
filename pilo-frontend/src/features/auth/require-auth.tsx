"use client";

import { Loader2 } from "lucide-react";
import { useRouter } from "next/navigation";
import { useEffect, type ReactNode } from "react";
import { useSession } from "@/lib/auth/use-session";

type RequireAuthProps = {
  nextPath: string;
  children: ReactNode;
};

export function RequireAuth({ nextPath, children }: RequireAuthProps) {
  const { status } = useSession();
  const router = useRouter();

  useEffect(() => {
    if (status === "anonymous") {
      router.replace(`/login?next=${encodeURIComponent(nextPath)}`);
    }
  }, [status, nextPath, router]);

  if (status !== "authenticated") {
    return (
      <div className="flex min-h-64 items-center justify-center gap-2 text-on-surface-variant">
        <Loader2 size={20} strokeWidth={1.75} className="animate-spin" />
        <span className="text-body-sm">Comprobando tu sesión…</span>
      </div>
    );
  }

  return <>{children}</>;
}
