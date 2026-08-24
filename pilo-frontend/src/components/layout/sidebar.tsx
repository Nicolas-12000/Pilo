"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Brand } from "@/components/layout/brand";
import { ThemeToggle } from "@/components/layout/theme-toggle";
import { UserMenu } from "@/components/layout/user-menu";
import { isActivePath, navItems } from "@/components/layout/nav-items";
import { cn } from "@/lib/utils/cn";

export function Sidebar() {
  const pathname = usePathname();

  return (
    <aside className="sticky top-0 hidden h-dvh w-60 shrink-0 flex-col border-r border-outline-variant bg-surface-container-low px-sm py-lg lg:flex">
      <Brand className="px-2.5" />

      <nav className="mt-xl flex flex-col gap-1" aria-label="Navegación principal">
        {navItems.map((item) => {
          const active = isActivePath(pathname, item.href);
          return (
            <Link
              key={item.href}
              href={item.href}
              aria-current={active ? "page" : undefined}
              className={cn(
                "flex items-center gap-3 rounded-sm px-3 py-2.5 font-label transition-colors duration-feedback",
                active
                  ? "bg-primary-container text-on-primary-container"
                  : "text-on-surface-variant hover:bg-surface-container hover:text-on-surface",
              )}
            >
              <item.icon size={20} strokeWidth={1.75} className="shrink-0" />
              {item.label}
            </Link>
          );
        })}
      </nav>

      <div className="mt-auto flex items-center justify-between gap-2 border-t border-outline-variant pt-md">
        <UserMenu />
        <ThemeToggle />
      </div>
    </aside>
  );
}
