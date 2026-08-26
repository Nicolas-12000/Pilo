"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Check,
  FolderOpen,
  KeyRound,
  Loader2,
  LogOut,
  Pencil,
  Settings2,
  TriangleAlert,
  X,
} from "lucide-react";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { PageHeader } from "@/components/pilo/page-header";
import { ThemeToggle } from "@/components/layout/theme-toggle";
import { Button, ButtonLink } from "@/components/ui/button";
import { Card, CardDescription, CardTitle, SectionHeading } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { FieldError, Input, Label } from "@/components/ui/field";
import { SkeletonList } from "@/components/ui/skeleton";
import { changePassword, fetchCurrentUser, updateProfile } from "@/lib/api/auth";
import { ApiError } from "@/lib/api/client";
import { authKeys } from "@/lib/api/queries";
import type { Role } from "@/lib/api/types";
import { useSession } from "@/lib/auth/use-session";
import { updateStoredUser } from "@/lib/auth/session";
import { routes } from "@/lib/routes";
import {
  changePasswordSchema,
  profileNameSchema,
  type ChangePasswordValues,
  type ProfileNameValues,
} from "./profile-schema";

const roleLabels: Record<Role, string> = {
  USER: "Ciudadano",
  REVIEWER: "Revisor",
  ADMIN: "Administración",
};

const ICON = 18;

function initials(fullName: string) {
  return fullName
    .split(" ")
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join("");
}

function FieldRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="min-w-0">
      <p className="text-body-sm text-on-surface-variant">{label}</p>
      <p className="mt-xxs wrap-break-word font-label text-on-surface">{value}</p>
    </div>
  );
}

export function ProfilePanel() {
  const { user: storedUser, signOut } = useSession();
  const router = useRouter();
  const queryClient = useQueryClient();
  const [isEditingName, setIsEditingName] = useState(false);
  const [isChangingPassword, setIsChangingPassword] = useState(false);

  const { data, isPending, error } = useQuery({
    queryKey: authKeys.me(),
    queryFn: fetchCurrentUser,
    initialData: storedUser ?? undefined,
  });

  const nameForm = useForm<ProfileNameValues>({
    resolver: zodResolver(profileNameSchema),
    values: { fullName: data?.fullName ?? "" },
  });

  const passwordForm = useForm<ChangePasswordValues>({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: { currentPassword: "", newPassword: "", confirmPassword: "" },
  });

  const saveName = useMutation({
    mutationFn: (values: ProfileNameValues) => updateProfile(values.fullName),
    onSuccess: (user) => {
      queryClient.setQueryData(authKeys.me(), user);
      updateStoredUser(user);
      setIsEditingName(false);
      toast.success("Nombre actualizado.");
    },
    onError: (cause) => {
      toast.error(cause instanceof ApiError ? cause.message : "No se ha podido actualizar el nombre.");
    },
  });

  const savePassword = useMutation({
    mutationFn: (values: ChangePasswordValues) =>
      changePassword(values.currentPassword, values.newPassword),
    onSuccess: () => {
      passwordForm.reset();
      setIsChangingPassword(false);
      toast.success("Contraseña actualizada.");
    },
    onError: (cause) => {
      toast.error(
        cause instanceof ApiError ? cause.message : "No se ha podido actualizar la contraseña.",
      );
    },
  });

  const handleSignOut = () => {
    signOut();
    router.push(routes.home);
  };

  return (
    <>
      <PageHeader
        eyebrow="Cuenta"
        title="Mi perfil"
        description="Consulta y gestiona los datos de tu cuenta."
      />

      {isPending ? (
        <div className="mt-xl">
          <SkeletonList count={2} columns={1} />
        </div>
      ) : error ? (
        <div className="mt-xl">
          <EmptyState
            icon={TriangleAlert}
            tone="danger"
            title="No hemos podido cargar tu perfil"
            description={
              error instanceof ApiError ? error.message : "Inténtalo de nuevo en unos segundos."
            }
          />
        </div>
      ) : data ? (
        <div className="mt-xl flex flex-col gap-xl">
          <Card className="lg:p-xl">
            <div className="flex flex-col items-center gap-lg text-center sm:flex-row sm:items-center sm:text-left">
              <div className="grid size-20 shrink-0 place-items-center rounded-full bg-primary-container font-headline-md text-on-primary-container">
                {initials(data.fullName)}
              </div>

              <div className="min-w-0">
                <CardTitle className="font-headline-md">{data.fullName}</CardTitle>
                <CardDescription className="mt-xxs break-all">{data.email}</CardDescription>
                <span className="mt-sm inline-flex items-center rounded-full bg-secondary-container px-3 py-1 font-caps text-on-secondary-container">
                  {roleLabels[data.role]}
                </span>
              </div>

              <div className="flex w-full flex-col gap-xs sm:ml-auto sm:w-auto sm:shrink-0">
                <ButtonLink href={routes.myCases} variant="secondary" size="sm" className="sm:w-52">
                  <FolderOpen size={ICON} strokeWidth={1.75} />
                  Mis expedientes
                </ButtonLink>
                {data.role === "ADMIN" ? (
                  <ButtonLink
                    href={routes.adminProcedures}
                    variant="secondary"
                    size="sm"
                    className="sm:w-52"
                  >
                    <Settings2 size={ICON} strokeWidth={1.75} />
                    Administración
                  </ButtonLink>
                ) : null}
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={handleSignOut}
                  className="text-danger hover:bg-danger-container/40 sm:w-52"
                >
                  <LogOut size={ICON} strokeWidth={1.75} />
                  Cerrar sesión
                </Button>
              </div>
            </div>
          </Card>

          <div className="grid gap-lg lg:grid-cols-2 lg:items-start">
            <section className="flex flex-col gap-md">
              <SectionHeading>Datos personales</SectionHeading>
              <Card>
                {isEditingName ? (
                  <form
                    className="flex flex-col gap-md"
                    onSubmit={nameForm.handleSubmit((values) => saveName.mutate(values))}
                  >
                    <label className="flex flex-col gap-xs">
                      <Label>Nombre completo</Label>
                      <Input
                        autoFocus
                        aria-invalid={nameForm.formState.errors.fullName ? true : undefined}
                        {...nameForm.register("fullName")}
                      />
                      {nameForm.formState.errors.fullName ? (
                        <FieldError>{nameForm.formState.errors.fullName.message}</FieldError>
                      ) : null}
                    </label>
                    <div className="flex flex-wrap gap-sm">
                      <Button type="submit" size="sm" disabled={saveName.isPending}>
                        {saveName.isPending ? (
                          <Loader2 size={ICON} strokeWidth={1.75} className="animate-spin" />
                        ) : (
                          <Check size={ICON} strokeWidth={1.75} />
                        )}
                        Guardar
                      </Button>
                      <Button
                        type="button"
                        variant="secondary"
                        size="sm"
                        onClick={() => {
                          setIsEditingName(false);
                          nameForm.reset({ fullName: data.fullName });
                        }}
                        disabled={saveName.isPending}
                      >
                        <X size={ICON} strokeWidth={1.75} />
                        Cancelar
                      </Button>
                    </div>
                  </form>
                ) : (
                  <div className="flex flex-wrap items-start justify-between gap-md">
                    <FieldRow label="Nombre completo" value={data.fullName} />
                    <Button variant="secondary" size="sm" onClick={() => setIsEditingName(true)}>
                      <Pencil size={ICON} strokeWidth={1.75} />
                      Editar
                    </Button>
                  </div>
                )}
              </Card>
            </section>

            <section className="flex flex-col gap-md">
              <SectionHeading>Seguridad</SectionHeading>
              <Card>
                {isChangingPassword ? (
                  <form
                    className="flex flex-col gap-md"
                    onSubmit={passwordForm.handleSubmit((values) => savePassword.mutate(values))}
                  >
                    <label className="flex flex-col gap-xs">
                      <Label>Contraseña actual</Label>
                      <Input
                        type="password"
                        autoComplete="current-password"
                        autoFocus
                        aria-invalid={
                          passwordForm.formState.errors.currentPassword ? true : undefined
                        }
                        {...passwordForm.register("currentPassword")}
                      />
                      {passwordForm.formState.errors.currentPassword ? (
                        <FieldError>
                          {passwordForm.formState.errors.currentPassword.message}
                        </FieldError>
                      ) : null}
                    </label>
                    <label className="flex flex-col gap-xs">
                      <Label>Nueva contraseña</Label>
                      <Input
                        type="password"
                        autoComplete="new-password"
                        aria-invalid={passwordForm.formState.errors.newPassword ? true : undefined}
                        {...passwordForm.register("newPassword")}
                      />
                      {passwordForm.formState.errors.newPassword ? (
                        <FieldError>{passwordForm.formState.errors.newPassword.message}</FieldError>
                      ) : null}
                    </label>
                    <label className="flex flex-col gap-xs">
                      <Label>Confirmar nueva contraseña</Label>
                      <Input
                        type="password"
                        autoComplete="new-password"
                        aria-invalid={
                          passwordForm.formState.errors.confirmPassword ? true : undefined
                        }
                        {...passwordForm.register("confirmPassword")}
                      />
                      {passwordForm.formState.errors.confirmPassword ? (
                        <FieldError>
                          {passwordForm.formState.errors.confirmPassword.message}
                        </FieldError>
                      ) : null}
                    </label>
                    <div className="flex flex-wrap gap-sm">
                      <Button type="submit" size="sm" disabled={savePassword.isPending}>
                        {savePassword.isPending ? (
                          <Loader2 size={ICON} strokeWidth={1.75} className="animate-spin" />
                        ) : (
                          <Check size={ICON} strokeWidth={1.75} />
                        )}
                        Actualizar
                      </Button>
                      <Button
                        type="button"
                        variant="secondary"
                        size="sm"
                        onClick={() => {
                          setIsChangingPassword(false);
                          passwordForm.reset();
                        }}
                        disabled={savePassword.isPending}
                      >
                        <X size={ICON} strokeWidth={1.75} />
                        Cancelar
                      </Button>
                    </div>
                  </form>
                ) : (
                  <div className="flex flex-wrap items-start justify-between gap-md">
                    <FieldRow label="Contraseña" value="••••••••" />
                    <Button
                      variant="secondary"
                      size="sm"
                      onClick={() => setIsChangingPassword(true)}
                    >
                      <KeyRound size={ICON} strokeWidth={1.75} />
                      Cambiar
                    </Button>
                  </div>
                )}
              </Card>
            </section>

            <section className="flex flex-col gap-md lg:col-span-2">
              <SectionHeading>Preferencias</SectionHeading>
              <Card>
                <div className="flex flex-wrap items-center justify-between gap-md">
                  <div className="min-w-0">
                    <p className="font-label text-on-surface">Tema de la aplicación</p>
                    <p className="mt-xxs text-body-sm text-on-surface-variant">
                      Cambia entre modo claro y oscuro.
                    </p>
                  </div>
                  <ThemeToggle />
                </div>
              </Card>
            </section>
          </div>
        </div>
      ) : null}
    </>
  );
}
