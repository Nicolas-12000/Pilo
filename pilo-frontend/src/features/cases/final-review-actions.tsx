"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { CheckCircle2, Loader2, XCircle } from "lucide-react";
import { useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card, CardDescription, CardTitle } from "@/components/ui/card";
import { FieldError, Label, Textarea } from "@/components/ui/field";
import { completeFinalReview } from "@/lib/api/workflow";
import { ApiError } from "@/lib/api/client";
import { caseKeys } from "@/lib/api/queries";
import type { CaseDetail } from "@/lib/api/types";

type FinalReviewActionsProps = {
  caseId: string;
  detail: CaseDetail;
  canReview: boolean;
  onReviewed: () => void;
};

export function FinalReviewActions({
  caseId,
  detail,
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

  if (detail.status === "APPROVED" || detail.status === "REJECTED") {
    return (
      <Card className="border-outline-variant bg-surface-container">
        <CardTitle>Resolución del expediente</CardTitle>
        <CardDescription className="mt-1">
          {detail.status === "APPROVED"
            ? "Este expediente ha sido aprobado en la revisión final."
            : "Este expediente ha sido rechazado en la revisión final."}
        </CardDescription>
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
        Todos los documentos obligatorios están validados. Emite la resolución del expediente.
      </CardDescription>

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
          onClick={() => review.mutate("APPROVED")}
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
