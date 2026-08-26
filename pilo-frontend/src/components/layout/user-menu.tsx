"use client";

import { FolderOpen, LogOut, Settings2, User as UserIcon } from "lucide-react";
import { useRouter } from "next/navigation";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { ButtonLink } from "@/components/ui/button";
import { useSession } from "@/lib/auth/use-session";
import { routes } from "@/lib/routes";
import { cn } from "@/lib/utils/cn";

function initials(fullName: string) {
  return fullName
    .split(" ")
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join("");
}

const roleLabels: Record<string, string> = {
  USER: "Ciudadano",
  REVIEWER: "Revisor",
  ADMIN: "Administración",
};

export function UserMenu({ className }: { className?: string }) {
  const { user, status, signOut } = useSession();
  const router = useRouter();

  if (status === "loading") {
    return <div className={cn("size-11 animate-pulse rounded-full bg-surface-container-high", className)} />;
  }

  if (!user) {
    return (
      <ButtonLink href={routes.login} size="sm" className={className}>
        Iniciar sesión
      </ButtonLink>
    );
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger
        className={cn(
          "grid size-11 shrink-0 place-items-center rounded-full bg-primary-container font-label text-on-primary-container transition-colors duration-feedback hover:bg-primary-container/80",
          className,
        )}
        aria-label="Abrir menú de cuenta"
      >
        {initials(user.fullName)}
      </DropdownMenuTrigger>
      <DropdownMenuContent>
        <DropdownMenuLabel>{roleLabels[user.role] ?? user.role}</DropdownMenuLabel>
        <div className="px-3 pb-2">
          <p className="font-title-sm text-on-surface">{user.fullName}</p>
          <p className="mt-0.5 text-body-sm text-on-surface-variant">{user.email}</p>
        </div>
        <DropdownMenuSeparator />
        <DropdownMenuItem onSelect={() => router.push(routes.profile)}>
          <UserIcon size={20} strokeWidth={1.75} />
          Mi perfil
        </DropdownMenuItem>
        <DropdownMenuItem onSelect={() => router.push(routes.myCases)}>
          <FolderOpen size={20} strokeWidth={1.75} />
          Mis expedientes
        </DropdownMenuItem>
        {user.role === "ADMIN" ? (
          <DropdownMenuItem onSelect={() => router.push(routes.adminProcedures)}>
            <Settings2 size={20} strokeWidth={1.75} />
            Administración
          </DropdownMenuItem>
        ) : null}
        <DropdownMenuItem
          onSelect={() => {
            signOut();
            router.push("/");
          }}
        >
          <LogOut size={20} strokeWidth={1.75} />
          Cerrar sesión
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
