import { getApiUrl } from "@/lib/api/config";
import { authFetch, readApiError } from "@/lib/api/client";
import type { CaseDetail, Workflow } from "@/lib/api/types";

export async function getWorkflow(caseId: string): Promise<Workflow> {
  const response = await authFetch(`${getApiUrl()}/api/v1/cases/${caseId}/workflow`, {
    cache: "no-store",
  });

  if (!response.ok) {
    await readApiError(response);
  }

  return (await response.json()) as Workflow;
}

export type FinalReviewDecision = "APPROVED" | "REJECTED";

export async function completeFinalReview(
  caseId: string,
  decision: FinalReviewDecision,
  comment?: string,
): Promise<CaseDetail> {
  const response = await authFetch(`${getApiUrl()}/api/v1/cases/${caseId}/workflow/final-review`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ decision, comment: comment ?? "" }),
  });

  if (!response.ok) {
    await readApiError(response);
  }

  return (await response.json()) as CaseDetail;
}
