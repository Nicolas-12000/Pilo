"use client";

import { usePathname } from "next/navigation";
import type { ReactNode } from "react";
import { MobileNav } from "@/components/layout/mobile-nav";
import { MobileTopBar } from "@/components/layout/mobile-top-bar";
import { Sidebar } from "@/components/layout/sidebar";

const BARE_ROUTES = ["/login"];

export function AppShell({ children }: { children: ReactNode }) {
  const pathname = usePathname();

  if (BARE_ROUTES.some((route) => pathname.startsWith(route))) {
    return <>{children}</>;
  }

  return (
    <div className="flex min-h-dvh">
      <Sidebar />
      <div className="flex min-w-0 flex-1 flex-col">
        <MobileTopBar />
        <div className="flex-1 pb-20 lg:pb-0">{children}</div>
      </div>
      <MobileNav />
    </div>
  );
}
