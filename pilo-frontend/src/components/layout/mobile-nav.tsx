"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { isActivePath } from "@/components/layout/nav-items";
import { useNavItems } from "@/components/layout/use-nav-items";
import { cn } from "@/lib/utils/cn";

export function MobileNav() {
  const pathname = usePathname();
  const items = useNavItems();

  const mobileItems = items.filter((item) => !item.hideOnMobile);

  return (
    <nav
      aria-label="Navegación principal"
      className="fixed inset-x-0 bottom-0 z-40 border-t border-outline-variant bg-surface pb-[env(safe-area-inset-bottom)] lg:hidden"
    >
      <ul className="mx-auto flex max-w-lg pl-10">
        {mobileItems.map((item) => {
          const active = isActivePath(pathname, item.href);
          return (
            <li key={item.label} className="min-w-0 flex-1">
              <Link
                href={item.href}
                aria-current={active ? "page" : undefined}
                className={cn(
                  "relative flex h-14 flex-col items-center justify-center gap-0.5 px-0.5 transition-colors duration-feedback",
                  active ? "text-primary" : "text-on-surface-variant",
                )}
              >
                {active ? (
                  <span
                    aria-hidden
                    className="absolute inset-x-2 top-0 h-0.5 rounded-full bg-primary transition-opacity duration-feedback"
                  />
                ) : null}
                <item.icon size={20} strokeWidth={1.75} className="shrink-0" />
                <span className="max-w-full truncate font-nav-label">{item.label}</span>
              </Link>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}
