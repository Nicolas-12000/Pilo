import { getApiUrl } from "@/lib/api/config";
import { authFetch, readApiError } from "@/lib/api/client";
import type { CaseDetail, CaseSummary } from "@/lib/api/types";

export async function createCase(procedureTypeId: string): Promise<CaseDetail> {
  const response = await authFetch(`${getApiUrl()}/api/v1/cases`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ procedureTypeId }),
  });

  if (!response.ok) {
    await readApiError(response);
  }

  return (await response.json()) as CaseDetail;
}

export async function listMyCases(): Promise<CaseSummary[]> {
  const response = await authFetch(`${getApiUrl()}/api/v1/cases`, { cache: "no-store" });

  if (!response.ok) {
    await readApiError(response);
  }

  return (await response.json()) as CaseSummary[];
}

export async function getCaseDetail(caseId: string): Promise<CaseDetail> {
  const response = await authFetch(`${getApiUrl()}/api/v1/cases/${caseId}`, { cache: "no-store" });

  if (!response.ok) {
    await readApiError(response);
  }

  return (await response.json()) as CaseDetail;
}
