import type { CaseDetail, DocumentRecord } from "@/lib/api/types";

/** Al menos un documento obligatorio fue rechazado por reglas o extracción automática. */
export function caseHasMandatoryAiRejection(
  detail: Pick<CaseDetail, "requirements">,
  documents: DocumentRecord[],
): boolean {
  const mandatoryIds = new Set(
    detail.requirements.filter((requirement) => requirement.mandatory).map((r) => r.id),
  );
  return documents.some(
    (document) =>
      mandatoryIds.has(document.requirementId) &&
      (document.status === "REJECTED" || document.status === "PROCESSING_FAILED"),
  );
}

export function caseReviewHadAiDisagreement(detail: Pick<CaseDetail, "recentAuditEvents">): boolean {
  return detail.recentAuditEvents.some((event) => {
    if (event.action !== "CASE_REVIEWED" || !event.metadata) {
      return false;
    }
    try {
      const parsed = JSON.parse(event.metadata) as { aiDisagreement?: boolean; decision?: string };
      return parsed.decision === "APPROVED" && parsed.aiDisagreement === true;
    } catch {
      return false;
    }
  });
}
