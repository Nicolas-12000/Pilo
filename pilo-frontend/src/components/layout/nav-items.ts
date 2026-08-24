import { FolderOpen, Home, Landmark, Settings2 } from "lucide-react";
import type { LucideIcon } from "lucide-react";
import { routes } from "@/lib/routes";

export type NavItem = {
  href: string;
  label: string;
  icon: LucideIcon;
  requiresAuth?: boolean;
  requiresAdmin?: boolean;
};

export const navItems: NavItem[] = [
  { href: routes.home, label: "Inicio", icon: Home },
  { href: routes.procedures, label: "Trámites", icon: Landmark },
  { href: routes.myCases, label: "Expedientes", icon: FolderOpen, requiresAuth: true },
  {
    href: routes.adminProcedures,
    label: "Administración",
    icon: Settings2,
    requiresAuth: true,
    requiresAdmin: true,
  },
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
