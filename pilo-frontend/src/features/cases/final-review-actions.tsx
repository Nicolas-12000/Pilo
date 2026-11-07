"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { AlertTriangle, CheckCircle2, Loader2, XCircle } from "lucide-react";
import { useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card, CardDescription, CardTitle } from "@/components/ui/card";
import { FieldError, Label, Textarea } from "@/components/ui/field";
import { completeFinalReview } from "@/lib/api/workflow";
import { ApiError } from "@/lib/api/client";
import { caseKeys } from "@/lib/api/queries";
import type { CaseDetail, DocumentRecord } from "@/lib/api/types";
import { caseHasMandatoryAiRejection, caseReviewHadAiDisagreement } from "@/lib/cases/ai-review";
import { cn } from "@/lib/utils/cn";

type FinalReviewActionsProps = {
  caseId: string;
  detail: CaseDetail;
  documents: DocumentRecord[];
  canReview: boolean;
  onReviewed: () => void;
};

export function FinalReviewActions({
  caseId,
  detail,
  documents,
  canReview,
  onReviewed,
}: FinalReviewActionsProps) {
  const queryClient = useQueryClient();
  const [comment, setComment] = useState("");

  const review = useMutation({
    mutationFn: (decision: "APPROVED" | "REJECTED") =>
      completeFinalReview(caseId, decision, comment.trim() || undefined),
    onSuccess: (updated, decision) => {
      queryClient.setQueryData(caseKeys.bundle(caseId), (current: unknown) => {
        if (!current || typeof current !== "object") {
          return current;
        }
        return { ...current, detail: updated };
      });
      toast.success(decision === "APPROVED" ? "Expediente aprobado." : "Expediente rechazado.");
      setComment("");
      onReviewed();
    },
    onError: (cause) => {
      toast.error(cause instanceof ApiError ? cause.message : "No se ha podido completar la revisión.");
    },
  });

  const aiRejectedMandatory = caseHasMandatoryAiRejection(detail, documents);
  const approvedDespiteAi =
    detail.status === "APPROVED" &&
    (aiRejectedMandatory || caseReviewHadAiDisagreement(detail));

  if (detail.status === "APPROVED" || detail.status === "REJECTED") {
    return (
      <Card
        className={cn(
          "border-outline-variant bg-surface-container",
          approvedDespiteAi && "border-warning",
        )}
      >
        <CardTitle>Resolución del expediente</CardTitle>
        <CardDescription className="mt-1">
          {detail.status === "APPROVED"
            ? "Este expediente ha sido aprobado en la revisión final."
            : "Este expediente ha sido rechazado en la revisión final."}
        </CardDescription>
        {approvedDespiteAi ? (
          <div
            role="status"
            className="mt-md flex items-start gap-sm rounded-lg bg-warning-container p-md text-on-warning-container"
          >
            <AlertTriangle size={20} strokeWidth={1.75} className="mt-0.5 shrink-0" />
            <p className="text-body-small">
              La revisión humana <strong>no coincide</strong> con la validación automática en al menos un
              documento obligatorio. La aprobación queda registrada con esta discrepancia.
            </p>
          </div>
        ) : null}
      </Card>
    );
  }

  if (!canReview || detail.status !== "UNDER_REVIEW") {
    return null;
  }

  return (
    <Card className="border-outline-variant bg-surface-container">
      <CardTitle>Revisión final</CardTitle>
      <CardDescription className="mt-1">
        Abre cada archivo, contrasta los datos extraídos con lo esperado y emite la resolución. La IA
        orienta; la decisión final es humana.
      </CardDescription>

      {detail.guidanceReviewer?.trim() ? (
        <p className="mt-md rounded-sm bg-surface-container-high p-sm text-body-sm text-on-surface">
          {detail.guidanceReviewer}
        </p>
      ) : null}

      {aiRejectedMandatory ? (
        <div
          role="alert"
          className="mt-md flex items-start gap-sm rounded-lg bg-warning-container p-md text-on-warning-container"
        >
          <AlertTriangle size={20} strokeWidth={1.75} className="mt-0.5 shrink-0" />
          <div className="text-body-small">
            <p className="font-label">La validación automática rechazó al menos un documento obligatorio.</p>
            <p className="mt-xs">
              Puedes aprobar igualmente si, revisando el archivo, consideras que es válido (falso negativo
              de la IA). Al aprobar, se registrará la discrepancia con el sistema automático.
            </p>
          </div>
        </div>
      ) : null}

      <label className="mt-md flex flex-col gap-xs">
        <Label>Comentario (opcional)</Label>
        <Textarea
          value={comment}
          onChange={(event) => setComment(event.target.value)}
          rows={3}
          maxLength={500}
          placeholder="Motivo o observaciones de la revisión…"
        />
        {comment.length > 450 ? (
          <FieldError>{500 - comment.length} caracteres restantes.</FieldError>
        ) : null}
      </label>

      <div className="mt-md flex flex-wrap gap-sm">
        <Button
          onClick={() => {
            if (
              aiRejectedMandatory &&
              !window.confirm(
                "Vas a aprobar el expediente aunque la IA o las reglas automáticas rechazaron documentos obligatorios. ¿Confirmas la decisión?",
              )
            ) {
              return;
            }
            review.mutate("APPROVED");
          }}
          disabled={review.isPending}
        >
          {review.isPending ? (
            <Loader2 size={18} strokeWidth={1.75} className="animate-spin" />
          ) : (
            <CheckCircle2 size={18} strokeWidth={1.75} />
          )}
          Aprobar expediente
        </Button>
        <Button
          variant="secondary"
          onClick={() => review.mutate("REJECTED")}
          disabled={review.isPending}
          className="text-danger"
        >
          {review.isPending ? (
            <Loader2 size={18} strokeWidth={1.75} className="animate-spin" />
          ) : (
            <XCircle size={18} strokeWidth={1.75} />
          )}
          Rechazar expediente
        </Button>
      </div>
    </Card>
  );
}
