"use client";

import { useLayoutEffect, type ReactNode } from "react";
import { useRouter } from "next/navigation";
import { useSession } from "@/lib/auth/use-session";
import { routes } from "@/lib/routes";

type RequireAdminProps = {
  nextPath: string;
  children: ReactNode;
};

export function RequireAdmin({ nextPath, children }: RequireAdminProps) {
  const { user, status } = useSession();
  const router = useRouter();

  useLayoutEffect(() => {
    if (status === "anonymous") {
      router.replace(routes.loginNext(nextPath));
      return;
    }
    if (status === "authenticated" && user?.role !== "ADMIN") {
      router.replace(routes.home);
    }
  }, [status, user?.role, nextPath, router]);

  if (status !== "authenticated" || user?.role !== "ADMIN") {
    return null;
  }

  return <>{children}</>;
}
