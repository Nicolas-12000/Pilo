import { getApiUrl } from "@/lib/api/config";
import type { ApiErrorBody, ProcedureTypeDetail, ProcedureTypeSummary } from "@/lib/api/types";

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly code?: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

async function readApiError(response: Response) {
  const body = (await response.json().catch(() => null)) as ApiErrorBody | null;
  throw new ApiError(body?.message ?? "No se ha podido completar la petición.", response.status, body?.error);
}

// Public, non-user-specific catalog: safe to let Next's fetch cache serve it for a short
// window across requests/users, cutting load on the backend without risking stale per-user data.
export async function listProcedureTypes(): Promise<ProcedureTypeSummary[]> {
  const response = await fetch(`${getApiUrl()}/api/v1/procedures/types`, {
    next: { revalidate: 60 },
  });

  if (!response.ok) {
    await readApiError(response);
  }

  return (await response.json()) as ProcedureTypeSummary[];
}

export async function getProcedureType(id: string): Promise<ProcedureTypeDetail> {
  const response = await fetch(`${getApiUrl()}/api/v1/procedures/types/${id}`, {
    next: { revalidate: 60 },
  });

  if (!response.ok) {
    await readApiError(response);
  }

  return (await response.json()) as ProcedureTypeDetail;
}
