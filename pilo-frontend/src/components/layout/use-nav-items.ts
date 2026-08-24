"use client";

import { navItems } from "@/components/layout/nav-items";
import { useSession } from "@/lib/auth/use-session";
import { routes } from "@/lib/routes";

/** Auth-aware hrefs: protected items go to login first, so the page never mounts for guests. */
export function useNavItems() {
  const { status } = useSession();

  return navItems.map((item) => {
    if (item.requiresAuth && status !== "authenticated") {
      return { ...item, href: routes.loginNext(item.href) };
    }
    return item;
  });
}
