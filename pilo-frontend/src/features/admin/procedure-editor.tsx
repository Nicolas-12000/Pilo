"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Loader2, Plus, Trash2 } from "lucide-react";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";
import { PageHeader } from "@/components/pilo/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { FieldError, Input, Label, Textarea } from "@/components/ui/field";
import {
  createAdminProcedure,
  createAdminRequirement,
  deleteAdminRequirement,
  getAdminProcedure,
  updateAdminProcedure,
  updateAdminRequirement,
} from "@/lib/api/admin-procedures";
import { ApiError } from "@/lib/api/client";
import { adminProcedureKeys, procedureKeys } from "@/lib/api/queries";
import type { AdminRequirement } from "@/lib/api/types";
import { routes } from "@/lib/routes";

const procedureSchema = z.object({
  title: z.string().trim().min(3, "Introduce un título."),
  description: z.string().trim().min(10, "Describe el trámite con más detalle."),
  targetDays: z.coerce.number().int().min(1).max(365),
});

const requirementSchema = z.object({
  code: z
    .string()
    .trim()
    .min(2, "Introduce un código.")
    .max(64)
    .transform((value) => value.toUpperCase()),
  name: z.string().trim().min(2, "Introduce un nombre."),
  description: z.string().trim().min(5, "Describe el requisito."),
  mandatory: z.boolean(),
  expectedDocumentType: z.string().trim().min(2, "Indica el tipo de documento esperado."),
  requireFutureExpiration: z.boolean(),
  minConfidence: z.coerce.number().min(0).max(1).optional().or(z.literal("")),
});

type ProcedureEditorProps = {
  procedureId?: string;
};

export function ProcedureEditor({ procedureId }: ProcedureEditorProps) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const isEditing = Boolean(procedureId);

  const detailQuery = useQuery({
    queryKey: adminProcedureKeys.detail(procedureId ?? "new"),
    queryFn: () => getAdminProcedure(procedureId!),
    enabled: isEditing,
  });

  const procedureForm = useForm({
    resolver: zodResolver(procedureSchema),
    values: detailQuery.data
      ? {
          title: detailQuery.data.title,
          description: detailQuery.data.description,
          targetDays: detailQuery.data.targetDays,
        }
      : {
          title: "",
          description: "",
          targetDays: 15,
        },
  });

  const requirementForm = useForm({
    resolver: zodResolver(requirementSchema),
    defaultValues: {
      code: "",
      name: "",
      description: "",
      mandatory: true,
      expectedDocumentType: "",
      requireFutureExpiration: false,
      minConfidence: "",
    },
  });

  const [editingRequirement, setEditingRequirement] = useState<AdminRequirement | null>(null);

  const saveProcedure = useMutation({
    mutationFn: async (values: z.infer<typeof procedureSchema>) => {
      const payload = {
        title: values.title,
        description: values.description,
        targetDays: values.targetDays,
      };
      if (isEditing && procedureId) {
        return updateAdminProcedure(procedureId, payload);
      }
      return createAdminProcedure(payload);
    },
    onSuccess: (result) => {
      void queryClient.invalidateQueries({ queryKey: adminProcedureKeys.all });
      void queryClient.invalidateQueries({ queryKey: procedureKeys.all });
      toast.success(isEditing ? "Trámite actualizado" : "Trámite creado");
      if (!isEditing) {
        router.replace(routes.adminProcedure(result.id));
      }
    },
    onError: (cause) => {
      toast.error(cause instanceof ApiError ? cause.message : "No se ha podido guardar el trámite.");
    },
  });

  const saveRequirement = useMutation({
    mutationFn: async (values: z.infer<typeof requirementSchema>) => {
      if (!procedureId) {
        throw new Error("MISSING_PROCEDURE");
      }
      const payload = {
        name: values.name,
        description: values.description,
        mandatory: values.mandatory,
        validationRules: {
          expectedDocumentType: values.expectedDocumentType,
          requireFutureExpiration: values.requireFutureExpiration,
          minConfidence: values.minConfidence === "" ? null : values.minConfidence,
        },
      };

      if (editingRequirement) {
        return updateAdminRequirement(procedureId, editingRequirement.id, payload);
      }

      return createAdminRequirement(procedureId, {
        code: values.code,
        ...payload,
      });
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: adminProcedureKeys.detail(procedureId!) });
      void queryClient.invalidateQueries({ queryKey: procedureKeys.all });
      requirementForm.reset();
      setEditingRequirement(null);
      toast.success(editingRequirement ? "Requisito actualizado" : "Requisito añadido");
    },
    onError: (cause) => {
      toast.error(cause instanceof ApiError ? cause.message : "No se ha podido guardar el requisito.");
    },
  });

  const removeRequirement = useMutation({
    mutationFn: (requirementId: string) => deleteAdminRequirement(procedureId!, requirementId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: adminProcedureKeys.detail(procedureId!) });
      void queryClient.invalidateQueries({ queryKey: procedureKeys.all });
      toast.success("Requisito eliminado");
    },
    onError: (cause) => {
      toast.error(cause instanceof ApiError ? cause.message : "No se ha podido eliminar el requisito.");
    },
  });

  function startEditRequirement(requirement: AdminRequirement) {
    setEditingRequirement(requirement);
    requirementForm.reset({
      code: requirement.code,
      name: requirement.name,
      description: requirement.description,
      mandatory: requirement.mandatory,
      expectedDocumentType: requirement.validationRules.expectedDocumentType,
      requireFutureExpiration: requirement.validationRules.requireFutureExpiration,
      minConfidence: requirement.validationRules.minConfidence ?? "",
    });
  }

  return (
    <>
      <PageHeader
        eyebrow={isEditing ? "Editar trámite" : "Nuevo trámite"}
        title={isEditing ? detailQuery.data?.title ?? "Trámite" : "Configurar trámite"}
        description={
          isEditing
            ? "Actualiza la ficha pública y gestiona los requisitos documentales."
            : "Define la ficha pública. Después podrás añadir requisitos."
        }
      />

      <form
        className="mt-xl grid gap-md rounded-lg bg-surface-container p-md md:p-lg"
        onSubmit={procedureForm.handleSubmit((values) => saveProcedure.mutate(values))}
      >
        <label className="grid gap-xs">
          <Label>Título</Label>
          <Input id="title" {...procedureForm.register("title")} />
          {procedureForm.formState.errors.title ? (
            <FieldError>{procedureForm.formState.errors.title.message}</FieldError>
          ) : null}
        </label>

        <label className="grid gap-xs">
          <Label>Descripción</Label>
          <Textarea id="description" {...procedureForm.register("description")} />
          {procedureForm.formState.errors.description ? (
            <FieldError>{procedureForm.formState.errors.description.message}</FieldError>
          ) : null}
        </label>

        <label className="grid gap-xs md:max-w-xs">
          <Label>Plazo objetivo (días)</Label>
          <Input id="targetDays" type="number" min={1} {...procedureForm.register("targetDays")} />
          {procedureForm.formState.errors.targetDays ? (
            <FieldError>{procedureForm.formState.errors.targetDays.message}</FieldError>
          ) : null}
        </label>

        <div>
          <Button type="submit" disabled={saveProcedure.isPending}>
            {saveProcedure.isPending ? (
              <>
                <Loader2 size={18} className="animate-spin" />
                Guardando…
              </>
            ) : isEditing ? (
              "Guardar cambios"
            ) : (
              "Crear trámite"
            )}
          </Button>
        </div>
      </form>

      {isEditing && detailQuery.data ? (
        <section className="mt-xl grid gap-lg">
          <div>
            <h2 className="font-title-md text-on-surface">Requisitos</h2>
            <p className="mt-1 text-body-sm text-on-surface-variant">
              Cada requisito define un documento esperado y las reglas que Spring Boot aplicará tras la
              extracción con IA.
            </p>
            {detailQuery.data.hasCases ? (
              <p className="mt-sm text-body-sm text-warning">
                Este trámite ya tiene expedientes. Evita eliminar requisitos en uso.
              </p>
            ) : null}
          </div>

          {detailQuery.data.requirements.length > 0 ? (
            <ul className="flex flex-col gap-sm">
              {detailQuery.data.requirements.map((requirement) => (
                <li
                  key={requirement.id}
                  className="rounded-lg bg-surface-container p-md ring-1 ring-outline-variant"
                >
                  <div className="flex flex-wrap items-start justify-between gap-sm">
                    <div>
                      <div className="flex flex-wrap items-center gap-xs">
                        <h3 className="font-title-sm text-on-surface">{requirement.name}</h3>
                        <Badge tone="external">{requirement.code}</Badge>
                        {!requirement.mandatory ? <Badge tone="external">Opcional</Badge> : null}
                      </div>
                      <p className="mt-1 text-body-sm text-on-surface-variant">
                        {requirement.description}
                      </p>
                      <p className="mt-sm text-body-sm text-on-surface-variant">
                        Tipo esperado: {requirement.validationRules.expectedDocumentType}
                        {requirement.validationRules.requireFutureExpiration
                          ? " · caducidad futura"
                          : ""}
                      </p>
                    </div>
                    <div className="flex gap-xs">
                      <Button type="button" variant="secondary" onClick={() => startEditRequirement(requirement)}>
                        Editar
                      </Button>
                      <Button
                        type="button"
                        variant="ghost"
                        onClick={() => removeRequirement.mutate(requirement.id)}
                        disabled={removeRequirement.isPending}
                      >
                        <Trash2 size={16} strokeWidth={1.75} />
                      </Button>
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          ) : null}

          <form
            className="grid gap-md rounded-lg bg-surface-container-high p-md md:p-lg"
            onSubmit={requirementForm.handleSubmit((values) => saveRequirement.mutate(values))}
          >
            <h3 className="font-title-sm text-on-surface">
              {editingRequirement ? "Editar requisito" : "Añadir requisito"}
            </h3>

            {!editingRequirement ? (
              <label className="grid gap-xs md:max-w-sm">
                <Label>Código</Label>
                <Input id="code" placeholder="RENTAL_CONTRACT" {...requirementForm.register("code")} />
                {requirementForm.formState.errors.code ? (
                  <FieldError>{requirementForm.formState.errors.code.message}</FieldError>
                ) : null}
              </label>
            ) : null}

            <div className="grid gap-md md:grid-cols-2">
              <label className="grid gap-xs">
                <Label>Nombre</Label>
                <Input id="name" {...requirementForm.register("name")} />
              </label>
              <label className="grid gap-xs">
                <Label>Tipo de documento esperado</Label>
                <Input id="expectedDocumentType" {...requirementForm.register("expectedDocumentType")} />
              </label>
            </div>

            <label className="grid gap-xs">
              <Label>Descripción</Label>
              <Textarea id="requirementDescription" {...requirementForm.register("description")} />
            </label>

            <div className="flex flex-wrap gap-md">
              <label className="inline-flex items-center gap-2 text-body-sm text-on-surface">
                <input type="checkbox" {...requirementForm.register("mandatory")} />
                Obligatorio
              </label>
              <label className="inline-flex items-center gap-2 text-body-sm text-on-surface">
                <input type="checkbox" {...requirementForm.register("requireFutureExpiration")} />
                Exigir caducidad futura
              </label>
            </div>

            <label className="grid gap-xs md:max-w-xs">
              <Label>Confianza mínima (0–1, opcional)</Label>
              <Input
                id="minConfidence"
                type="number"
                step="0.01"
                min={0}
                max={1}
                {...requirementForm.register("minConfidence")}
              />
            </label>

            <div className="flex flex-wrap gap-sm">
              <Button type="submit" disabled={saveRequirement.isPending}>
                {saveRequirement.isPending ? (
                  <>
                    <Loader2 size={18} className="animate-spin" />
                    Guardando…
                  </>
                ) : editingRequirement ? (
                  "Actualizar requisito"
                ) : (
                  <>
                    <Plus size={18} strokeWidth={1.75} />
                    Añadir requisito
                  </>
                )}
              </Button>
              {editingRequirement ? (
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() => {
                    setEditingRequirement(null);
                    requirementForm.reset();
                  }}
                >
                  Cancelar edición
                </Button>
              ) : null}
            </div>
          </form>
        </section>
      ) : null}
    </>
  );
}
