import { FolderOpen, Home, Landmark } from "lucide-react";
import type { LucideIcon } from "lucide-react";
import { routes } from "@/lib/routes";

export type NavItem = {
  href: string;
  label: string;
  icon: LucideIcon;
};

export const navItems: NavItem[] = [
  { href: routes.home, label: "Inicio", icon: Home },
  { href: routes.procedures, label: "Trámites", icon: Landmark },
  { href: routes.myCases, label: "Expedientes", icon: FolderOpen },
];

export function isActivePath(pathname: string, href: string) {
  if (href === routes.home) {
    return pathname === routes.home;
  }
  if (href === routes.myCases) {
    return pathname.startsWith(routes.myCases) || pathname.startsWith("/cases");
  }
  return pathname.startsWith(href);
}
