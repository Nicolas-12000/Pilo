import { getApiUrl } from "@/lib/api/config";
import { authFetch, readApiError } from "@/lib/api/client";
import type { DocumentRecord } from "@/lib/api/types";

export async function listDocuments(caseId: string): Promise<DocumentRecord[]> {
  const response = await authFetch(`${getApiUrl()}/api/v1/cases/${caseId}/documents`, {
    cache: "no-store",
  });

  if (!response.ok) {
    await readApiError(response);
  }

  return (await response.json()) as DocumentRecord[];
}

export async function uploadDocument(
  caseId: string,
  requirementId: string,
  file: File,
): Promise<DocumentRecord> {
  const formData = new FormData();
  formData.append("requirementId", requirementId);
  formData.append("file", file);

  const response = await authFetch(`${getApiUrl()}/api/v1/cases/${caseId}/documents/upload`, {
    method: "POST",
    body: formData,
  });

  if (!response.ok) {
    await readApiError(response);
  }

  return (await response.json()) as DocumentRecord;
}
