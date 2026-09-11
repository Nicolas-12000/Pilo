import { getApiUrl } from "@/lib/api/config";
import { authFetch, readApiError } from "@/lib/api/client";
import type { DocumentRecord } from "@/lib/api/types";

type PresignedUploadResponse = {
  uploadUrl: string;
  method: string;
  documentId: string | null;
  headers: Record<string, string>;
  note: string;
};

export async function listDocuments(caseId: string): Promise<DocumentRecord[]> {
  const response = await authFetch(`${getApiUrl()}/api/v1/cases/${caseId}/documents`, {
    cache: "no-store",
  });

  if (!response.ok) {
    await readApiError(response);
  }

  return (await response.json()) as DocumentRecord[];
}

async function requestPresignedUpload(
  caseId: string,
  requirementId: string,
  file: File,
): Promise<PresignedUploadResponse> {
  const response = await authFetch(`${getApiUrl()}/api/v1/cases/${caseId}/documents/presigned-url`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      requirementId,
      fileName: file.name,
      mimeType: file.type || "application/octet-stream",
      fileSize: file.size,
    }),
  });

  if (!response.ok) {
    await readApiError(response);
  }

  return (await response.json()) as PresignedUploadResponse;
}

export async function uploadDocument(
  caseId: string,
  requirementId: string,
  file: File,
): Promise<DocumentRecord> {
  const presigned = await requestPresignedUpload(caseId, requirementId, file);

  if (presigned.method === "PUT") {
    const uploadResponse = await fetch(presigned.uploadUrl, {
      method: "PUT",
      headers: {
        ...presigned.headers,
        "Content-Type": file.type || "application/octet-stream",
      },
      body: file,
    });

    if (!uploadResponse.ok) {
      throw new Error("No se ha podido subir el archivo al almacenamiento.");
    }

    return {
      id: presigned.documentId ?? "pending",
      requirementId,
      requirementCode: "",
      fileName: file.name,
      mimeType: file.type || "application/octet-stream",
      fileSize: file.size,
      status: "PENDING_UPLOAD",
      extraction: null,
      validationFailures: [],
      processingFailureReason: null,
    };
  }

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
