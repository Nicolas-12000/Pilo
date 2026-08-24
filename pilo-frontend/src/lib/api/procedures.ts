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

export async function listProcedureTypes(): Promise<ProcedureTypeSummary[]> {
  const response = await fetch(`${getApiUrl()}/api/v1/procedures/types`, {
    cache: "no-store",
  });

  if (!response.ok) {
    await readApiError(response);
  }

  return (await response.json()) as ProcedureTypeSummary[];
}

export async function getProcedureType(id: string): Promise<ProcedureTypeDetail> {
  const response = await fetch(`${getApiUrl()}/api/v1/procedures/types/${id}`, {
    cache: "no-store",
  });

  if (!response.ok) {
    await readApiError(response);
  }

  return (await response.json()) as ProcedureTypeDetail;
}
