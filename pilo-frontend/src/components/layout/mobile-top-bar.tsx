"use client";

import { Brand } from "@/components/layout/brand";
import { ThemeToggle } from "@/components/layout/theme-toggle";
import { UserMenu } from "@/components/layout/user-menu";

export function MobileTopBar() {
  return (
    <header className="sticky top-0 z-40 border-b border-outline-variant bg-surface/95 backdrop-blur lg:hidden">
      <div className="flex h-16 items-center justify-between px-md">
        <Brand />
        <div className="flex items-center gap-2">
          <ThemeToggle />
          <UserMenu />
        </div>
      </div>
    </header>
  );
}
