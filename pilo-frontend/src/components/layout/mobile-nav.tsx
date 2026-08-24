"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { isActivePath, navItems } from "@/components/layout/nav-items";
import { cn } from "@/lib/utils/cn";

export function MobileNav() {
  const pathname = usePathname();

  return (
    <nav
      aria-label="Navegación principal"
      className="fixed inset-x-0 bottom-0 z-40 border-t border-outline-variant bg-surface lg:hidden"
    >
      <ul className="mx-auto flex max-w-lg">
        {navItems.map((item) => {
          const active = isActivePath(pathname, item.href);
          return (
            <li key={item.href} className="flex-1">
              <Link
                href={item.href}
                aria-current={active ? "page" : undefined}
                className={cn(
                  "flex h-16 flex-col items-center justify-center gap-1 transition-colors duration-feedback",
                  active ? "text-primary" : "text-on-surface-variant",
                )}
              >
                <item.icon size={20} strokeWidth={1.75} />
                <span className="font-nav-label">{item.label}</span>
              </Link>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}
