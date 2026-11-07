"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { TriangleAlert, Users } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/pilo/page-header";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/ui/empty-state";
import { SkeletonList } from "@/components/ui/skeleton";
import { listAdminUsers, setAdminUserEnabled } from "@/lib/api/admin-users";
import { ApiError } from "@/lib/api/client";
import { adminUserKeys } from "@/lib/api/queries";
import { useSession } from "@/lib/auth/use-session";

export function AdminUsersPanel() {
  const queryClient = useQueryClient();
  const { user: sessionUser } = useSession();
  const { data, isPending, error } = useQuery({
    queryKey: adminUserKeys.list(),
    queryFn: listAdminUsers,
  });

  const toggle = useMutation({
    mutationFn: ({ id, enabled }: { id: string; enabled: boolean }) =>
      setAdminUserEnabled(id, enabled),
    onSuccess: (updated) => {
      void queryClient.invalidateQueries({ queryKey: adminUserKeys.list() });
      toast.success(updated.enabled ? "Cuenta reactivada." : "Cuenta desactivada. El token deja de valer.");
    },
    onError: (cause) => {
      toast.error(cause instanceof ApiError ? cause.message : "No se ha podido actualizar el usuario.");
    },
  });

  return (
    <>
      <PageHeader
        eyebrow="Administración"
        title="Usuarios"
        description="Desactiva una cuenta si hay un acceso comprometido. No hace falta rotar el JWT de toda la app."
      />

      <div className="mt-xl">
        {isPending ? (
          <SkeletonList count={3} columns={1} />
        ) : error ? (
          <EmptyState
            icon={TriangleAlert}
            tone="danger"
            title="No hemos podido cargar los usuarios"
            description={
              error instanceof ApiError && error.status === 403
                ? "Tu cuenta no tiene permisos de administrador."
                : "El servicio no responde ahora mismo. Inténtalo de nuevo en unos segundos."
            }
          />
        ) : data && data.length > 0 ? (
          <ul className="flex flex-col gap-sm">
            {data.map((account) => {
              const isSelf = account.id === sessionUser?.id;
              return (
                <li
                  key={account.id}
                  className="elevation-card flex flex-wrap items-center justify-between gap-md rounded-lg p-md md:p-lg"
                >
                  <div className="min-w-0">
                    <h2 className="font-title-md text-on-surface">{account.fullName}</h2>
                    <p className="mt-1 text-body-sm text-on-surface-variant">
                      {account.email} · {account.role}
                      {account.enabled ? "" : " · desactivada"}
                    </p>
                  </div>
                  <Button
                    type="button"
                    size="sm"
                    variant={account.enabled ? "danger" : "secondary"}
                    disabled={isSelf || toggle.isPending}
                    onClick={() => toggle.mutate({ id: account.id, enabled: !account.enabled })}
                  >
                    {isSelf ? "Tu cuenta" : account.enabled ? "Desactivar" : "Reactivar"}
                  </Button>
                </li>
              );
            })}
          </ul>
        ) : (
          <EmptyState icon={Users} title="No hay usuarios" description="Cuando existan cuentas, aparecerán aquí." />
        )}
      </div>
    </>
  );
}
