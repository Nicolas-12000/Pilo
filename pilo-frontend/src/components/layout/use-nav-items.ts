"use client";

import { navItems } from "@/components/layout/nav-items";
import { useSession } from "@/lib/auth/use-session";
import { routes } from "@/lib/routes";

/** Auth-aware hrefs: protected items go to login first, so the page never mounts for guests. */
export function useNavItems() {
  const { user, status } = useSession();

  return navItems
    .filter((item) => !item.requiresAdmin || user?.role === "ADMIN")
    .filter((item) => !item.requiresReviewer || user?.role === "REVIEWER" || user?.role === "ADMIN")
    .map((item) => {
      if (item.requiresAuth && status !== "authenticated") {
        return { ...item, href: routes.loginNext(item.href) };
      }
      return item;
    });
}
