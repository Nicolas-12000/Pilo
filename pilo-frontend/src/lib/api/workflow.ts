import { getApiUrl } from "@/lib/api/config";
import { authFetch, readApiError } from "@/lib/api/client";
import type { Workflow } from "@/lib/api/types";

export async function getWorkflow(caseId: string): Promise<Workflow> {
  const response = await authFetch(`${getApiUrl()}/api/v1/cases/${caseId}/workflow`, {
    cache: "no-store",
  });

  if (!response.ok) {
    await readApiError(response);
  }

  return (await response.json()) as Workflow;
}
