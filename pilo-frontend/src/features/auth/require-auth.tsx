"use client";

import { useLayoutEffect, type ReactNode } from "react";
import { useRouter } from "next/navigation";
import { useSession } from "@/lib/auth/use-session";
import { routes } from "@/lib/routes";

type RequireAuthProps = {
  nextPath: string;
  children: ReactNode;
};

export function RequireAuth({ nextPath, children }: RequireAuthProps) {
  const { status } = useSession();
  const router = useRouter();

  useLayoutEffect(() => {
    if (status === "anonymous") {
      router.replace(routes.loginNext(nextPath));
    }
  }, [status, nextPath, router]);

  if (status !== "authenticated") {
    return null;
  }

  return <>{children}</>;
}
